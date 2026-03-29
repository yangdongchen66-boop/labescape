package com.chrono.engine.service;

import com.chrono.engine.domain.CompanyProgress;
import com.chrono.engine.domain.GameSession;
import com.chrono.engine.dto.AgentSystemEventDTO;
import com.chrono.engine.dto.NarrativeStreamDTO;
import com.chrono.engine.service.impl.AgentOrchestratorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

/**
 * 游戏引擎核心服务 (Game Engine Service)
 * 
 * Chrono-Agents 的心脏，负责协调多智能体协同工作流。
 * 
 * 版本演进：
 * - Phase 1: 使用 Thread.sleep 模拟 LLM 调用
 * - Phase 2+: 接入真实 DeepSeek LLM，通过 AgentOrchestratorService 调用
 * 
 * 架构设计：
 * - 使用 Java 21 虚拟线程处理并发，每个玩家会话独立线程
 * - 通过 SSE (Server-Sent Events) 向前端推送流式事件
 * - Manager -> Supervisor -> Executor 的 Agent 链条由 AgentOrchestratorService 实现
 * 
 * @author Chrono Engine Team
 * @since Phase 1
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameEngineService {

    /**
     * Agent 编排服务（注入真实 LLM 实现）
     */
    private final AgentOrchestratorService agentOrchestrator;

    /**
     * 游戏规则引擎
     */
    private final GameRuleEngine ruleEngine;

    /**
     * 事件系统
     */
    private final EventSystem eventSystem;

    /**
     * 会话存储池
     * Key: sessionId, Value: GameSession
     * 使用 ConcurrentHashMap 保证线程安全
     */
    private final Map<String, GameSession> sessionPool = new ConcurrentHashMap<>();
    
    /**
     * 正在处理的会话集合（防止重复处理）
     */
    private final Set<String> processingSessions = ConcurrentHashMap.newKeySet();
    
    /**
     * 已处理的请求ID集合（防止EventSource重连导致的重复处理）
     */
    private final Set<String> processedRequestIds = ConcurrentHashMap.newKeySet();

    /**
     * 处理玩家动作 - 核心入口方法
     * 
     * 这是前端调用 SSE 接口后触发的核心业务方法。
     * 使用 Java 21 虚拟线程异步执行，避免阻塞 Tomcat 线程池。
     * 
     * @param sessionId 会话ID（前端生成并维护）
     * @param playerInput 玩家输入文本
     * @param requestId 请求ID（用于去重）
     * @param emitter SSE 发射器，用于向前端推送事件
     */
    public void processPlayerAction(String sessionId, String playerInput, String requestId, SseEmitter emitter) {
        // 检查requestId是否已经处理过（防止EventSource重连导致的重复请求）
        if (!processedRequestIds.add(requestId)) {
            log.warn("[GameEngine] 请求 {} 已经处理过，忽略重复请求", requestId);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"type\":\"duplicate\",\"message\":\"请求已处理\"}"));
                emitter.complete();
            } catch (Exception e) {
                // ignore
            }
            return;
        }
        
        // 检查是否正在处理中
        if (!processingSessions.add(sessionId)) {
            log.warn("[GameEngine] 会话 {} 正在处理中，忽略重复请求", sessionId);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"type\":\"error\",\"message\":\"正在处理中，请勿重复提交\"}"));
                emitter.complete();
            } catch (Exception e) {
                // ignore
            }
            return;
        }
        
        // 在虚拟线程中执行
        Thread.startVirtualThread(() -> {
            boolean completed = false;
            try {
                log.info("[GameEngine] 开始处理会话 {} 的玩家动作: {} (requestId: {})", sessionId, playerInput, requestId);

                // ========== Step 0: 获取或创建会话 ==========
                GameSession session = getOrCreateSession(sessionId);
                session.addChatEntry("[PLAYER] " + playerInput);
                
                // 记录状态变化前的值
                int oldRisk = session.getRisk();
                int oldMentorMood = session.getMentorMood();
                int oldPrep = session.getPreparation();
                int oldHp = session.getPlayer().getHp();
                int oldMp = session.getPlayer().getMp();
                int oldDay = session.getCurrentDay();
                
                // ========== Step 1: 解析玩家意图并执行游戏逻辑 ==========
                log.info("[GameEngine] Step 1: 处理游戏动作 - 当前Day: {}", oldDay);
                GameActionResult actionResult = processGameAction(session, playerInput);
                log.info("[GameEngine] Step 1 完成 - 新Day: {}", session.getCurrentDay());
                
                // ========== Step 1.5: Judge (特殊结局判定) ==========
                String judgeResult = agentOrchestrator.runJudge(session, playerInput, emitter);
                String specialEnding = parseSpecialEnding(judgeResult);
                
                if (specialEnding != null) {
                    // 触发了特殊结局，生成结局剧情
                    log.info("[GameEngine] 触发特殊结局: {}", specialEnding);
                    
                    // 让 Executor 生成结局剧情
                    String narrativeHint = extractNarrativeHint(judgeResult);
                    agentOrchestrator.runExecutorForSpecialEnding(
                        session, playerInput, specialEnding, narrativeHint, emitter, "NPC:王导"
                    );
                    
                    // 发送游戏结束事件
                    sendGameEnd(emitter, specialEnding, session);
                    log.info("[GameEngine] 会话 {} 游戏结束: {}", sessionId, specialEnding);
                    
                    if (!completed) {
                        completed = true;
                        emitter.complete();
                    }
                    return; // 直接返回，不继续正常流程
                }
                
                // ========== Step 2: Manager (调度者) - 异步发送事件，不阻塞 ==========
                // 先发送Manager开始事件，然后异步执行
                sendAgentEvent(emitter, "Manager", "解析玩家意图 -> [" + truncate(playerInput, 20) + "]");
                
                // ========== Step 3: Supervisor (命运裁判) - 本地快速执行 ==========
                String difficultyStr = actionResult.getDifficulty() <= 10 ? "easy" :
                                       actionResult.getDifficulty() <= 15 ? "normal" : "hard";
                String checkResult = agentOrchestrator.runSupervisor(
                    session, actionResult.getAttribute(), difficultyStr, emitter
                );

                // ========== Step 4: Executor (角色化身) - 流式输出剧情 ==========
                String speaker = actionResult.getNpcName();
                agentOrchestrator.runExecutor(session, playerInput, checkResult, emitter, speaker);

                // ========== Step 5: 检查事件触发 ==========
                List<com.chrono.engine.domain.GameEvent> events = eventSystem.checkAndTriggerEvents(session);
                for (var event : events) {
                    sendAgentEvent(emitter, "Event", "触发事件: " + event.getTitle());
                }

                // ========== Step 6: 检查游戏结束条件 ==========
                String gameEndResult = checkGameEnd(session);
                if (gameEndResult != null) {
                    // 游戏结束，发送结局
                    sendGameEnd(emitter, gameEndResult, session);
                    log.info("[GameEngine] 会话 {} 游戏结束: {}", sessionId, gameEndResult);
                } else {
                    // ========== Step 7: 发送状态更新 ==========
                    Map<String, Object> stateUpdate = buildStateUpdate(
                        session, oldRisk, oldMentorMood, oldPrep, oldHp, oldMp, actionResult
                    );
                    sendStateUpdate(emitter, stateUpdate);
                }

                log.info("[GameEngine] 会话 {} 处理完成", sessionId);
                if (!completed) {
                    completed = true;
                    emitter.complete();
                }

            } catch (Exception e) {
                log.error("[GameEngine] 会话 {} 处理异常", sessionId, e);
                if (!completed) {
                    completed = true;
                    try {
                        emitter.completeWithError(e);
                    } catch (IllegalStateException ignored) {
                        // 连接已关闭，忽略
                    }
                }
            } finally {
                // 处理完成，移除处理中标志
                processingSessions.remove(sessionId);
            }
        });
    }

    /**
     * 处理游戏动作逻辑
     */
    private GameActionResult processGameAction(GameSession session, String playerInput) {
        GameActionResult result = new GameActionResult();
        String input = playerInput.toLowerCase();
        
        // 确定检定属性
        if (input.contains("刷题") || input.contains("笔试") || input.contains("投递")) {
            result.setAttribute("INT");
            result.setDifficulty(14);
            result.setTimeCost(1);
            
            // 刷题增加准备度
            if (input.contains("刷题")) {
                int newPrep = Math.min(5, session.getPreparation() + 1);
                session.setPreparation(newPrep);
                result.setPrepChange(1);
                
                // 完成子任务
                session.getSideQuests().stream()
                    .filter(q -> q.getId().equals("study_1"))
                    .findFirst()
                    .ifPresent(q -> q.addProgress(1));
            }
            
            // 投递简历推进公司流程
            if (input.contains("投递")) {
                session.getCompanyProgresses().forEach(c -> {
                    if (c.getStage().equals("apply") && !c.isCompleted()) {
                        c.advanceStage();
                        result.setCompanyUpdate(c.getCompanyName() + "->笔试");
                    }
                });
            }
            
        } else if (input.contains("面试")) {
            result.setAttribute("CHA");
            result.setDifficulty(15);
            result.setTimeCost(1);
            
            // 面试推进流程
            session.getCompanyProgresses().forEach(c -> {
                if ((c.getStage().equals("written") || c.getStage().startsWith("interview")) 
                    && !c.isCompleted()) {
                    c.advanceStage();
                    result.setCompanyUpdate(c.getCompanyName() + "->" + c.getStage());
                    
                    // 更新主线任务
                    if (c.getStage().equals("offer")) {
                        session.getMainQuests().put("get_offer", true);
                        result.setQuestCompleted("get_offer");
                    }
                }
            });
            
        } else if (input.contains("说服") || input.contains("沟通") || input.contains("商量")) {
            result.setAttribute("CHA");
            result.setDifficulty(12);
            result.setTimeCost(1);
            
            // 说服改善导师关系
            int newMood = Math.min(100, session.getMentorMood() + 5);
            session.setMentorMood(newMood);
            result.setMoodChange(5);
            result.setRiskChange(-5);
            session.setRisk(Math.max(0, session.getRisk() - 5));
            
            // 检查导师签字任务
            if (newMood >= 0 && session.getMainQuests().getOrDefault("get_offer", false)) {
                session.getMainQuests().put("mentor_approval", true);
                result.setQuestCompleted("mentor_approval");
            }
            
        } else if (input.contains("硬刚") || input.contains("怼") || input.contains("拒绝")) {
            result.setAttribute("STR");
            result.setDifficulty(16);
            result.setTimeCost(1);
            
            // 硬刚增加风险，降低导师关系
            int newRisk = Math.min(100, session.getRisk() + 15);
            int newMood = Math.max(-100, session.getMentorMood() - 10);
            session.setRisk(newRisk);
            session.setMentorMood(newMood);
            result.setRiskChange(15);
            result.setMoodChange(-10);
            
        } else if (input.contains("交流") || input.contains("师弟") || input.contains("师兄")) {
            result.setAttribute("WIS");
            result.setDifficulty(10);
            result.setTimeCost(1);
            
            // 社交改善导师关系，降低风险
            int newMood = Math.min(100, session.getMentorMood() + 5);
            session.setMentorMood(newMood);
            session.setRisk(Math.max(0, session.getRisk() - 5));
            result.setMoodChange(5);
            result.setRiskChange(-5);
            
            // 完成社交任务
            session.getSideQuests().stream()
                .filter(q -> q.getId().equals("social_1"))
                .findFirst()
                .ifPresent(q -> q.addProgress(1));
            
        } else if (input.contains("休息") || input.contains("睡觉") || input.contains("咖啡")) {
            result.setAttribute("CON");
            result.setDifficulty(8);
            result.setTimeCost(1);
            result.setRestAction(true);
            
            // 恢复HP/MP，降低风险
            int newHp = Math.min(session.getPlayer().getMaxHp(), session.getPlayer().getHp() + 20);
            int newMp = Math.min(session.getPlayer().getMaxMp(), session.getPlayer().getMp() + 20);
            session.getPlayer().setHp(newHp);
            session.getPlayer().setMp(newMp);
            session.setRisk(Math.max(0, session.getRisk() - 10));
            result.setHpChange(20);
            result.setMpChange(20);
            result.setRiskChange(-10);
            
        } else {
            // 默认处理
            result.setAttribute("INT");
            result.setDifficulty(12);
            result.setTimeCost(1);
        }
        
        // 推进时间：每次对话消耗1天
        int oldDay = session.getCurrentDay();
        int newDay = oldDay + 1;
        session.setCurrentDay(newDay);
        session.setInGameHours(newDay * 24);
        log.info("[GameEngine] 时间推进: Day {} -> Day {}", oldDay, newDay);
        
        // 计算新阶段
        String newPhase;
        if (newDay <= 2) newPhase = "PREP";
        else if (newDay <= 5) newPhase = "BREAKTHROUGH";
        else newPhase = "FINAL";
        
        if (!newPhase.equals(session.getGamePhase())) {
            session.setGamePhase(newPhase);
            result.setPhaseChanged(true);
        }
        result.setDayChanged(true);
        
        // 确定NPC
        if (input.contains("导师") || input.contains("王导")) {
            result.setNpcName("NPC:王导");
        } else if (input.contains("师弟") || input.contains("师兄")) {
            result.setNpcName("NPC:李师弟");
        } else {
            result.setNpcName("NPC:王导");
        }
        
        return result;
    }

    /**
     * 构建状态更新数据
     */
    private Map<String, Object> buildStateUpdate(
            GameSession session,
            int oldRisk, int oldMood, int oldPrep, int oldHp, int oldMp,
            GameActionResult actionResult) {
        
        Map<String, Object> update = new HashMap<>();
        
        // 当前状态
        update.put("gamePhase", session.getGamePhase());
        update.put("currentDay", session.getCurrentDay());
        update.put("timeBlock", session.getTimeBlock());
        update.put("risk", session.getRisk());
        update.put("mentorMood", session.getMentorMood());
        update.put("preparation", session.getPreparation());
        update.put("hp", session.getPlayer().getHp());
        update.put("mp", session.getPlayer().getMp());
        
        // 变化值
        Map<String, Integer> changes = new HashMap<>();
        changes.put("risk", session.getRisk() - oldRisk);
        changes.put("mentorMood", session.getMentorMood() - oldMood);
        changes.put("preparation", session.getPreparation() - oldPrep);
        changes.put("hp", session.getPlayer().getHp() - oldHp);
        changes.put("mp", session.getPlayer().getMp() - oldMp);
        update.put("changes", changes);
        
        // 公司进度
        List<Map<String, Object>> companies = new ArrayList<>();
        for (CompanyProgress c : session.getCompanyProgresses()) {
            Map<String, Object> company = new HashMap<>();
            company.put("name", c.getCompanyName());
            company.put("stage", c.getStage());
            company.put("hasOffer", c.isHasOffer());
            company.put("isCompleted", c.isCompleted());
            companies.add(company);
        }
        update.put("companies", companies);
        
        // 主线任务
        update.put("mainQuests", session.getMainQuests());
        
        // 子任务
        List<Map<String, Object>> sideQuests = new ArrayList<>();
        for (var q : session.getSideQuests()) {
            Map<String, Object> quest = new HashMap<>();
            quest.put("id", q.getId());
            quest.put("isCompleted", q.isCompleted());
            quest.put("progress", q.getCurrentProgress());
            quest.put("required", q.getRequiredProgress());
            sideQuests.add(quest);
        }
        update.put("sideQuests", sideQuests);
        
        // 动作结果
        if (actionResult.getQuestCompleted() != null) {
            update.put("questCompleted", actionResult.getQuestCompleted());
        }
        if (actionResult.getCompanyUpdate() != null) {
            update.put("companyUpdate", actionResult.getCompanyUpdate());
        }
        
        return update;
    }

    /**
     * 发送状态更新到前端
     */
    private void sendStateUpdate(SseEmitter emitter, Map<String, Object> stateUpdate) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(stateUpdate);
            emitter.send(SseEmitter.event()
                    .name("state-update")
                    .data(json));
            log.info("[GameEngine] >>>>> 发送状态更新 - Day: {}, Risk: {}, Mood: {} <<<<<", 
                stateUpdate.get("currentDay"), stateUpdate.get("risk"), stateUpdate.get("mentorMood"));
        } catch (IllegalStateException | IOException e) {
            log.debug("[GameEngine] 发送状态更新失败，连接已关闭");
        }
    }

    /**
     * 发送 Agent 事件
     */
    private void sendAgentEvent(SseEmitter emitter, String agentRole, String actionDesc) {
        try {
            emitter.send(SseEmitter.event()
                    .name("agent-event")
                    .data(AgentSystemEventDTO.of(agentRole, actionDesc)));
        } catch (IllegalStateException | IOException e) {
            log.debug("[GameEngine] 发送 Agent 事件失败，连接已关闭");
        }
    }

    /**
     * 获取或创建游戏会话
     */
    private GameSession getOrCreateSession(String sessionId) {
        return sessionPool.computeIfAbsent(sessionId, id -> {
            log.info("[GameEngine] 创建新会话: {}", id);
            return GameSession.createNew("艾德里安");
        });
    }

    /**
     * 重置游戏会话
     * 
     * @param sessionId 会话ID
     * @return 是否成功重置
     */
    public boolean resetSession(String sessionId) {
        if (sessionPool.containsKey(sessionId)) {
            sessionPool.remove(sessionId);
            log.info("[GameEngine] 会话已重置: {}", sessionId);
            return true;
        }
        return false;
    }

    /**
     * 解析特殊结局类型
     * @param judgeResult Judge Agent 的返回结果
     * @return 结局类型，如果未触发则返回 null
     */
    private String parseSpecialEnding(String judgeResult) {
        try {
            if (judgeResult.contains("\"triggered\": true") || judgeResult.contains("\"triggered\":true")) {
                // 提取 endingType
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                    "\"endingType\"\\s*:\\s*\"([^\"]+)\""
                );
                java.util.regex.Matcher matcher = pattern.matcher(judgeResult);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        } catch (Exception e) {
            log.warn("[GameEngine] 解析特殊结局失败: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 提取剧情提示
     */
    private String extractNarrativeHint(String judgeResult) {
        try {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "\"narrativeHint\"\\s*:\\s*\"([^\"]+)\""
            );
            java.util.regex.Matcher matcher = pattern.matcher(judgeResult);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            log.warn("[GameEngine] 提取剧情提示失败: {}", e.getMessage());
        }
        return "结束剧情";
    }

    /**
     * 检查游戏结束条件
     * 由大模型根据当前状态判断，同时有一些硬性条件
     * 
     * @return 结局类型，null表示游戏继续
     */
    private String checkGameEnd(GameSession session) {
        // 硬性失败条件：时间耗尽
        if (session.getCurrentDay() > 7) {
            return "TIMEOUT_FAILURE";
        }
        
        // 硬性失败条件：心态崩溃
        if (session.getPlayer().getMp() <= 0) {
            return "MENTAL_BREAKDOWN";
        }
        
        // 硬性失败条件：导师关系彻底破裂且没有Offer
        boolean hasOffer = session.getCompanyProgresses().stream()
                .anyMatch(CompanyProgress::isHasOffer);
        if (session.getMentorMood() <= -80 && !hasOffer) {
            return "MENTOR_RUINED";
        }
        
        // 成功条件：获得Offer且导师态度非负
        if (hasOffer && session.getMentorMood() >= 0) {
            return "PERFECT_ENDING";
        }
        
        // 偷跑结局：有Offer但导师关系破裂
        if (hasOffer && session.getMentorMood() <= -50) {
            return "ESCAPE_ENDING";
        }
        
        // 彩蛋：完美准备度 + 导师态度极好 = 提前成功
        if (session.getPreparation() >= 5 && session.getMentorMood() >= 30 && session.getCurrentDay() <= 5) {
            return "EARLY_SUCCESS";
        }
        
        // 彩蛋：风险100时触发特殊结局
        if (session.getRisk() >= 100) {
            return "RISK_EXPLOSION";
        }
        
        return null; // 游戏继续
    }

    /**
     * 发送游戏结局
     */
    private void sendGameEnd(SseEmitter emitter, String endingType, GameSession session) {
        try {
            Map<String, Object> endData = new HashMap<>();
            endData.put("type", "game-end");
            endData.put("endingType", endingType);
            endData.put("endingTitle", getEndingTitle(endingType));
            endData.put("endingDesc", getEndingDescription(endingType));
            // 判断是胜利还是失败
            boolean isVictory = endingType.equals("PERFECT_ENDING") || 
                               endingType.equals("ESCAPE_ENDING") || 
                               endingType.equals("EARLY_SUCCESS") ||
                               endingType.equals("GENIUS_ENDING");
            endData.put("gameState", isVictory ? "VICTORY" : "GAME_OVER");
            endData.put("finalStats", Map.of(
                "day", session.getCurrentDay(),
                "mentorMood", session.getMentorMood(),
                "risk", session.getRisk(),
                "preparation", session.getPreparation(),
                "hasOffer", session.getCompanyProgresses().stream().anyMatch(CompanyProgress::isHasOffer)
            ));
            
            ObjectMapper mapper = new ObjectMapper();
            emitter.send(SseEmitter.event()
                    .name("game-end")
                    .data(mapper.writeValueAsString(endData)));
        } catch (IllegalStateException | IOException e) {
            // 连接已关闭，忽略
            log.debug("[GameEngine] 发送游戏结局失败，连接已关闭");
        }
    }

    private String getEndingTitle(String endingType) {
        return switch (endingType) {
            case "PERFECT_ENDING" -> "🎉 完美结局";
            case "ESCAPE_ENDING" -> "🏃 偷跑结局";
            case "EARLY_SUCCESS" -> "⭐ 超神表现";
            case "TIMEOUT_FAILURE" -> "⏰ 时间耗尽";
            case "MENTAL_BREAKDOWN" -> "💔 心态崩溃";
            case "MENTOR_RUINED" -> "🔥 关系破裂";
            case "RISK_EXPLOSION" -> "💥 风险爆炸";
            // 特殊结局（彩蛋）
            case "VIOLENCE_ENDING" -> "🚔 警察带走了";
            case "INSULT_ENDING" -> "🚨 被开除了";
            case "GENIUS_ENDING" -> "🧠 天才说服";
            case "QUIT_ENDING" -> "🚪 退学结局";
            case "BRIBE_ENDING" -> "💰 贿赂失败";
            default -> "❓ 未知结局";
        };
    }

    private String getEndingDescription(String endingType) {
        return switch (endingType) {
            case "PERFECT_ENDING" -> "恭喜你！你成功获得了实习Offer，并且导师也同意了你的申请。双喜临门！";
            case "ESCAPE_ENDING" -> "你拿到了Offer，但和导师的关系彻底破裂。虽然可以去实习了，但毕业前景堪忧...";
            case "EARLY_SUCCESS" -> "彩蛋结局！你的出色表现让导师刮目相看，不仅拿到Offer，还获得了导师的强烈推荐！";
            case "TIMEOUT_FAILURE" -> "7天时间过去了，你既没有拿到Offer，也没有处理好导师关系。秋招与你擦肩而过...";
            case "MENTAL_BREAKDOWN" -> "巨大的压力让你彻底崩溃。身体最重要，先休息一下吧。";
            case "MENTOR_RUINED" -> "你和导师的关系彻底破裂，没有Offer，没有支持，研究生生涯陷入危机...";
            case "RISK_EXPLOSION" -> "彩蛋结局！你的高风险行为引发了连锁反应，被导师抓个正着，一切努力付诸东流...";
            // 特殊结局（彩蛋）
            case "VIOLENCE_ENDING" -> "你的极端行为触发了安保警报。几分钟后，警察冲入实验室，把你带走了。这一切...";
            case "INSULT_ENDING" -> "导师被你的话气得浑身发抖，当即打电话给学院。第二天，你收到了开除通知书...";
            case "GENIUS_ENDING" -> "彩蛋结局！你的话让导师眼前一亮，立即同意了你的实习申请。这就是情商和话术的力量！";
            case "QUIT_ENDING" -> "你坚定地告诉导师你不读了，然后头也不回地离开了实验室。这是你的选择。";
            case "BRIBE_ENDING" -> "导师的脸色瞬间变了，他拿起电话打给了学术道德委员会。你的研究生生涯...";
            default -> "游戏结束。";
        };
    }

    /**
     * 截断字符串工具方法
     */
    private String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }

    /**
     * 游戏动作结果
     */
    @lombok.Data
    private static class GameActionResult {
        private String attribute = "INT";
        private int difficulty = 12;
        private int timeCost = 1;
        private String npcName = "NPC:王导";
        private boolean restAction = false;
        
        // 变化值
        private int riskChange = 0;
        private int moodChange = 0;
        private int prepChange = 0;
        private int hpChange = 0;
        private int mpChange = 0;
        
        // 状态变化
        private boolean dayChanged = false;
        private boolean phaseChanged = false;
        
        // 特殊结果
        private String questCompleted = null;
        private String companyUpdate = null;
    }
}
