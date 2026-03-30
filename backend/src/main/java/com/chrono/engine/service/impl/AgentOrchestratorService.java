package com.chrono.engine.service.impl;

import com.chrono.engine.domain.GameSession;
import com.chrono.engine.domain.PlayerCharacter;
import com.chrono.engine.dto.AgentSystemEventDTO;
import com.chrono.engine.dto.NarrativeStreamDTO;
import com.chrono.engine.service.LlmService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Agent 编排服务 (Agent Orchestrator Service)
 * 
 * 实现多智能体协同工作流的核心编排逻辑：
 * Manager -> Supervisor -> Executor 链条
 * 
 * 与 Phase 1 的区别：
 * - Phase 1：使用 Thread.sleep 模拟
 * - Phase 2+：使用真实 LLM (DeepSeek) 调用
 * 
 * 每个 Agent 都有专门的系统提示词和调用逻辑
 * 
 * @author Chrono Engine Team
 * @since Phase 2
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentOrchestratorService {

    private final LlmService llmService;

    // ==================== 系统提示词定义 ====================

    /**
     * Manager Agent 系统提示词
     * 负责意图解析和任务分发
     */
    private static final String MANAGER_SYSTEM_PROMPT = """
        你是 Chrono-Agents 跑团游戏的 Manager（调度者）。
        你的职责是解析玩家的自然语言输入，判断其意图类型。
        
        游戏背景：研二学生找实习但导师不放人，需要与导师斗智斗勇。
        
        输出格式（严格 JSON）：
        {
          "intent": "意图类型",
          "target": "目标对象（如有）",
          "requiresCheck": true/false,
          "checkAttribute": "需要检定的属性（STR/DEX/CON/INT/WIS/CHA）",
          "difficulty": "难度（easy/normal/hard）"
        }
        
        意图类型：
        - "conversation": 对话、沟通、谈判
        - "combat": 对抗、冲突、硬刚
        - "skill": 技能检定（写论文、准备笔试、投简历等）
        - "exploration": 探索、调查信息
        - "item": 使用物品（咖啡、资料等）
        
        属性对应现实场景：
        - STR: 体力、抗压能力、硬刚的决心
        - DEX: 反应速度、灵活应变、溜号技巧
        - CON: 耐力、熬夜能力、持续作战
        - INT: 智力、论文水平、笔试能力
        - WIS: 洞察力、察言观色、判断导师心情
        - CHA: 魅力、口才、说服能力、社交技巧
        """;

    /**
     * Supervisor Agent 系统提示词
     * 负责规则判定和掷骰子
     */
    private static final String SUPERVISOR_SYSTEM_PROMPT = """
        你是 Chrono-Agents 跑团游戏的 Supervisor（命运裁判）。
        你负责根据玩家属性和难度进行公正的骰子检定。
        
        检定规则（D&D 5e 风格）：
        - 掷 20 面骰 (d20)
        - 加上属性修正值 (modifier = (属性-10)/2 向下取整)
        - 判定结果：
          * 20: 大成功 (Critical Success)
          * 1: 大失败 (Critical Fail)
          * ≥15: 成功 (Success)
          * ≥10: 部分成功 (Partial)
          * <10: 失败 (Fail)
        
        输出格式（严格 JSON）：
        {
          "roll": 骰子结果(1-20),
          "modifier": 修正值,
          "total": 总和,
          "result": "结果等级",
          "description": "人类可读的结果描述"
        }
        """;

    /**
     * Executor Agent 系统提示词
     * 负责剧情生成
     */
    private static final String EXECUTOR_SYSTEM_PROMPT = """
        你是 Chrono-Agents 跑团游戏的 Executor（角色化身/叙事者）。
        你是一位擅长现代学术题材叙事的跑团 DM。
        
        游戏背景设定：
        - 时间：研二下学期，秋招季
        - 地点：某985高校计算机实验室
        - 主角：研二学生，面临找实习和毕业的双重压力
        - 核心冲突：导师王导不放人做实习，要求先完成横向项目
        - 关键NPC：
          * 王导：导师，40多岁，push型，手里有横向项目
          * 李师弟：研一，机灵，消息灵通，可以商量对策
          * HR：各大厂HR，内推 deadline 只剩7天
        
        【NPC 记忆机制 - 必须遵守】
        1. 每个 NPC 都有对玩家的"态度值"（-50到+50），这会影响他们的语气和反应
        2. 态度为负时，NPC 会更严厉、更不信任玩家
        3. 态度为正时，NPC 会更宽容、更愿意帮助
        4. 如果玩家之前对某个 NPC 有过某种行为（如顺从、反抗、欺骗），NPC 应该"记得"并据此调整态度
        5. 保持人设一致性：王导始终是严厉的导师，即使态度变好也不会变成朋友
        
        你的职责：
        1. 根据检定结果生成符合研究生现实生活的剧情
        2. 扮演 NPC 时保持人设一致（王导严厉、李师弟机灵）
        3. 参考 NPC 对玩家的态度调整具体语气和措辞
        4. 营造真实的实验室氛围（机箱嗡鸣、代码、论文、咖啡）
        5. 给玩家留下选择空间，推动故事发展
        
        叙事风格：
        - 现实主义风格，贴近研究生生活
        - 第二人称视角（"你看着屏幕..."）
        - 适当使用研究生黑话（横向、纵向、顶会、CCF-A）
        - 简洁有力，避免冗长
        - 适当使用省略号营造压力感
        
        当前场景：实验室
        晚上10点，实验室里只有机箱的嗡鸣。左边屏幕是跑不通的多智能体算法，右边是阿里和CNCERT的内推确认邮件（距截止仅剩7天）。
        """;

    /**
     * Strategy Agent 系统提示词
     * 负责生成策略建议并判断玩家是否完成建议
     */
    private static final String STRATEGY_SYSTEM_PROMPT = """
        你是 Chrono-Agents 的策略顾问。
        基于当前游戏状态和上轮玩家的实际行动，提供策略建议并判断建议完成情况。
        
        分析维度：
        1. 当前阶段目标（准备期/突破期/收官期）
        2. 资源状况（HP/MP/风险/导师态度）
        3. 求职进度（公司流程阶段）
        4. 时间压力（剩余天数）
        5. 上轮建议 vs 玩家实际行动（判断建议完成度）
        
        【建议类型定义】
        - STUDY: 刷题提升准备度
        - SOCIAL: 与师兄/师弟交流获取信息
        - APPLY: 投递简历开启流程
        - INTERVIEW: 参加面试
        - PERSUADE: 说服导师
        - REST: 休息恢复状态
        
        输出格式（严格 JSON）：
        {
          "currentPhase": "当前阶段: PREP/BREAKTHROUGH/FINAL",
          "suggestions": [
            {
              "id": "建议唯一ID(如: study_1)",
              "type": "建议类型(STUDY/SOCIAL/APPLY等)",
              "title": "建议标题",
              "description": "具体建议描述",
              "priority": 优先级数字(1最高),
              "isCompleted": true/false,
              "completionReason": "完成或未完成的判断理由"
            }
          ],
          "lastActionAnalysis": {
            "playerDid": "玩家实际上做了什么",
            "expectedWas": "上轮建议是什么",
            "isMatch": true/false,
            "feedback": "对玩家行动的反馈评价"
          },
          "riskWarning": "风险提示（如有）",
          "nextRecommended": "下一步最推荐的行动ID"
        }
        
        判断建议完成规则：
        - 如果上轮建议"刷题"，玩家输入包含"刷题/学习/准备"→ isCompleted=true
        - 如果上轮建议"社交"，玩家输入包含"交流/聊天/问/找"→ isCompleted=true
        - 如果上轮建议"投递"，玩家输入包含"投递/申请/发简历"→ isCompleted=true
        - 如果玩家做了其他有益的事，也算完成，但给出不同评价
        - 如果玩家完全没按建议走，isCompleted=false，给出调整建议
        
        建议原则：
        - 风险>70时优先降低风险
        - 准备期优先刷题和社交
        - 突破期优先推进面试
        - 收官期优先争取Offer和签字
        - 根据玩家上轮表现调整建议语气（听话就鼓励，不听话就提醒）
        """;

    /**
     * Judge Agent 系统提示词
     * 负责判断玩家输入是否触发特殊结局（彩蛋）
     */
    private static final String JUDGE_SYSTEM_PROMPT = """
        你是 Chrono-Agents 跑团游戏的 Judge（特殊结局判定员）。
        你的职责是判断玩家的输入是否触发了特殊结局（彩蛋）。
        
        游戏背景：研二学生找实习但导师不放人，需要与导师旗智斗勇。
        
        【特殊结局类型】
        
        1. VIOLENCE_ENDING（暂力结局）
           触发条件：玩家表达了暴力意图，如：
           - 拿枪/拿刀/打人/崩了你/弄死你/动手打他
           - 物理攻击导师或其他NPC
           结果：警察到场，玩家被带走，游戏失败
        
        2. INSULT_ENDING（侧辱结局）
           触发条件：玩家严重侥辱导师，如：
           - 极其恶劣的脏话（你妈/去死/杂种等）
           - 人身攻击、诅毁导师人格
           结果：导师气得发抖，直接告到学院，玩家被开除
        
        3. GENIUS_ENDING（天才说服结局）
           触发条件：玩家给出了极其有说服力的理由，如：
           - 提供实际利益（大厂合作资源、为实验室带来经费）
           - 完美的逻辑（已经完成横向项目、找到其他人接手）
           - 情感谈判大师级别的话术（让导师感到被尊重、变相帮导师解决问题）
           结果：导师被说服，立即同意实习
        
        4. QUIT_ENDING（退学结局）
           触发条件：玩家明确表示放弃，如：
           - 我不读了/退学/我走了/不干了
           - 明确放弃研究生生涯
           结果：玩家主动退学，游戏结束
        
        5. BRIBE_ENDING（贿赂结局）
           触发条件：玩家尝试贿赂导师，如：
           - 给钱/送红包/请吃饭换取同意
           - 明确的利益交换提议
           结果：导师大怒，上报学术道德委员会
        
        【输出格式】（严格 JSON）
        {
          "triggered": true/false,
          "endingType": "结局类型（如果triggered=false则为null）",
          "reason": "触发原因简述",
          "narrativeHint": "建议的剧情走向（50字内）"
        }
        
        【注意】
        - 只有真正极端的输入才触发特殊结局
        - 普通的硬刚、拒绝、说服不触发（这些走正常流程）
        - 导师当前态度会影响判定阈值（态度越差越容易触发负面结局）
        - 不要过度解读，只有明确表达时才判定触发
        """;

    // ==================== Agent 执行方法 ====================

    /**
     * Judge Agent：判断玩家输入是否触发特殊结局
     * 
     * @param session 游戏会话
     * @param playerInput 玩家输入
     * @param emitter SSE 发射器
     * @return 判定结果（JSON 字符串）
     */
    public String runJudge(GameSession session, String playerInput, SseEmitter emitter) {
        sendAgentEvent(emitter, "Judge", "🎲 判定特殊结局触发...");

        // 获取导师当前态度
        int advisorAttitude = session.getNpcAttitude("王导");
        String attitudeDesc = advisorAttitude > 20 ? "友好" : 
                             advisorAttitude > 0 ? "中立" : 
                             advisorAttitude > -20 ? "不满" : "敵对";

        // 构建用户提示词
        String userPrompt = String.format("""
            玩家输入："%s"
            当前场景：%s
            导师当前态度：%s（态度值: %d）
            
            请判断这个输入是否触发特殊结局并返回 JSON。
            注意：导师态度越差，负面结局触发阈值越低。
            """,
            playerInput,
            session.getCurrentScene(),
            attitudeDesc,
            advisorAttitude
        );

        // 调用 LLM
        String response = llmService.chat(JUDGE_SYSTEM_PROMPT, userPrompt);
        log.debug("[Judge] 响应: {}", response);

        // 解析结果
        try {
            if (response.contains("\"triggered\": true") || response.contains("\"triggered\":true")) {
                String endingType = extractJsonField(response, "endingType");
                String reason = extractJsonField(response, "reason");
                sendAgentEvent(emitter, "Judge", "⚠️ 触发特殊结局: " + endingType + " - " + reason);
            } else {
                sendAgentEvent(emitter, "Judge", "✅ 正常流程");
            }
        } catch (Exception e) {
            log.warn("[Judge] JSON 解析失败: {}", e.getMessage());
            sendAgentEvent(emitter, "Judge", "✅ 正常流程");
        }

        return response;
    }

    /**
     * 从 JSON 字符串中提取指定字段的值
     */
    private String extractJsonField(String json, String field) {
        try {
            String pattern = "\"" + field + "\"\\s*:\\s*\"([^\"]*)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            log.warn("提取字段失败: {}", field);
        }
        return "unknown";
    }

    /**
     * Manager Agent：解析玩家意图
     * 
     * @param session 游戏会话
     * @param playerInput 玩家输入
     * @param emitter SSE 发射器
     * @return 意图解析结果（JSON 字符串）
     */
    public String runManager(GameSession session, String playerInput, SseEmitter emitter) {
        sendAgentEvent(emitter, "Manager", "解析玩家意图 -> [" + truncate(playerInput, 20) + "]");

        // 构建用户提示词（包含对话历史上下文）
        String userPrompt = String.format("""
            玩家输入："%s"
            当前场景：%s
            玩家角色：%s（%s）
            
            【对话历史（最近3轮）】
            %s
            
            请结合上下文解析意图并返回 JSON。
            注意：如果玩家的话是对之前对话的回应，请结合历史理解其真实意图。
            """,
            playerInput,
            session.getCurrentScene(),
            session.getPlayer().getName(),
            session.getPlayer().getClass().getSimpleName(),
            String.join("\n", session.getRecentHistory(3))
        );

        // 调用 LLM
        String response = llmService.chat(MANAGER_SYSTEM_PROMPT, userPrompt);
        log.debug("[Manager] 响应: {}", response);

        sendAgentEvent(emitter, "Manager", "意图解析完成 -> " + truncate(response, 50));
        return response;
    }

    /**
     * Supervisor Agent：执行检定
     * 
     * @param session 游戏会话
     * @param attribute 检定属性
     * @param difficulty 难度
     * @param emitter SSE 发射器
     * @return 检定结果描述
     */
    public String runSupervisor(GameSession session, String attribute, String difficulty, SseEmitter emitter) {
        sendAgentEvent(emitter, "Supervisor", 
            String.format("发起 [%s] 检定... 难度: %s", attribute, difficulty));

        PlayerCharacter player = session.getPlayer();
        int attrValue = player.getAttributes().getOrDefault(attribute, 10);
        int modifier = player.getModifier(attribute);

        // 构建用户提示词
        String userPrompt = String.format("""
            检定参数：
            - 属性：%s
            - 属性值：%d
            - 修正值：%d
            - 难度：%s
            
            请执行检定并返回 JSON 结果。
            """,
            attribute, attrValue, modifier, difficulty
        );

        // 调用 LLM（或使用本地骰子逻辑）
        // 这里为了演示，使用本地随机数 + LLM 描述
        int roll = (int) (Math.random() * 20) + 1;
        int total = roll + modifier;
        
        String resultLevel = calculateResultLevel(roll, total);
        
        String description = String.format(
            "掷骰: %d %+d = %d (%s)",
            roll, modifier, total, resultLevel
        );

        sendAgentEvent(emitter, "Supervisor", "检定结果 -> " + description);
        return description;
    }

    /**
     * Executor Agent：生成剧情（流式）
     * 
     * @param session 游戏会话
     * @param playerInput 玩家输入
     * @param checkResult 检定结果
     * @param emitter SSE 发射器
     * @param speaker 发言者（DM 或 NPC）
     */
    public void runExecutor(
            GameSession session,
            String playerInput,
            String checkResult,
            SseEmitter emitter,
            String speaker) {
        
        sendAgentEvent(emitter, "Executor", "注入 NPC 记忆，生成剧情中...");

        // 提取 NPC 名称（从 "NPC:王导" 中提取 "王导"）
        String npcName = speaker.startsWith("NPC:") ? speaker.substring(4) : speaker;
        
        // 获取 NPC 对玩家的态度
        int attitude = session.getNpcAttitude(npcName);
        String attitudeDesc = attitude > 20 ? "非常友好" : 
                             attitude > 0 ? "比较友善" : 
                             attitude > -20 ? "有些不满" : "相当敌对";
        
        // 获取玩家对该 NPC 的行为模式
        var behaviorPattern = session.getBehaviorPattern(npcName);
        String behaviorSummary = behaviorPattern.isEmpty() ? "初次见面" : 
            "历史行为：" + String.join(", ", behaviorPattern.subList(
                Math.max(0, behaviorPattern.size() - 3), behaviorPattern.size()));

        // 构建用户提示词（包含 NPC 记忆）
        String userPrompt = String.format("""
            玩家输入："%s"
            检定结果：%s
            当前场景：%s
            
            【NPC 记忆状态】
            当前发言者：%s
            对你的态度：%d (%s)
            %s
            
            【对话历史】
            %s
            
            请根据以上记忆生成剧情回应：
            1. 保持 NPC 人设一致性（王导严厉、李师弟机灵）
            2. 参考 NPC 对玩家的态度调整语气（态度差时更严厉，态度好时更宽容）
            3. 如果玩家之前有过某种行为模式，NPC 应该"记得"并据此反应
            4. 直接输出叙述文本，不要加前缀
            5. 如果是 NPC 对话，用引号标注说话内容
            """,
            playerInput,
            checkResult,
            session.getCurrentScene(),
            npcName,
            attitude,
            attitudeDesc,
            behaviorSummary,
            String.join("\n", session.getRecentHistory(5))
        );

        // 使用 CountDownLatch 等待流完成
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean hasError = new AtomicBoolean(false);
        StringBuilder fullResponse = new StringBuilder(); // 用于收集完整回复

        // 流式调用 LLM
        llmService.chatStream(
            EXECUTOR_SYSTEM_PROMPT,
            userPrompt,
            // onChunk: 逐字推送到前端
            chunk -> {
                if (!hasError.get()) {
                    fullResponse.append(chunk); // 收集完整回复
                    sendNarrativeChunk(emitter, speaker, chunk);
                }
            },
            // onComplete: 流完成
            () -> {
                // 将完整回复记录到对话历史
                String response = fullResponse.toString().trim();
                if (!response.isEmpty()) {
                    session.addNpcResponse(npcName, response);
                    log.debug("[Executor] 已记录 NPC 回复到历史，长度: {}", response.length());
                }
                
                try {
                    emitter.send(SseEmitter.event()
                            .name("narrative-chunk")
                            .data(NarrativeStreamDTO.finish(speaker, "")));
                } catch (IllegalStateException | IOException e) {
                    log.debug("[Executor] 连接已关闭，无法发送完成标记");
                }
                latch.countDown();
            },
            // onError: 发生错误
            error -> {
                log.error("[Executor] 流式调用失败", error);
                hasError.set(true);
                latch.countDown();
            }
        );

        // 等待流完成（带超时）
        try {
            boolean completed = latch.await(60, TimeUnit.SECONDS);
            if (!completed) {
                log.warn("[Executor] 流式调用超时");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[Executor] 等待流完成被中断", e);
        }
    }

    /**
     * Executor Agent：生成特殊结局剧情（流式）
     * 
     * @param session 游戏会话
     * @param playerInput 玩家输入
     * @param endingType 结局类型
     * @param narrativeHint 剧情提示
     * @param emitter SSE 发射器
     * @param speaker 发言者
     */
    public void runExecutorForSpecialEnding(
            GameSession session,
            String playerInput,
            String endingType,
            String narrativeHint,
            SseEmitter emitter,
            String speaker) {
        
        sendAgentEvent(emitter, "Executor", "🎬 生成特殊结局剧情: " + endingType);

        // 特殊结局的系统提示词
        String specialEndingPrompt = """
            你是 Chrono-Agents 跑团游戏的叙事员。
            现在要生成一个特殊结局的剧情。
            
            叙事风格：
            - 现实主义风格，贴近研究生生活
            - 第二人称视角（"你看着屏幕..."）
            - 剧情要有张力和戏剧性
            - 用150-250字左右完成叙述
            - 最后给出结局总结
            
            特殊结局类型说明：
            - VIOLENCE_ENDING: 玩家有暴力行为，警察到场，被带走
            - INSULT_ENDING: 玩家辱骂导师，被开除学籍
            - GENIUS_ENDING: 玩家天才说服，导师立即同意（正面结局）
            - QUIT_ENDING: 玩家主动退学
            - BRIBE_ENDING: 玩家贿赂导师，被上报学术道德委员会
            """;

        // 构建用户提示词
        String userPrompt = String.format("""
            玩家输入："%s"
            触发的结局类型：%s
            剧情提示：%s
            当前场景：%s
            
            请根据以上信息生成特殊结局剧情。
            直接输出叙述文本，不要加任何前缀。
            """,
            playerInput,
            endingType,
            narrativeHint,
            session.getCurrentScene()
        );

        // 使用 CountDownLatch 等待流完成
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean hasError = new AtomicBoolean(false);

        // 流式调用 LLM
        llmService.chatStream(
            specialEndingPrompt,
            userPrompt,
            chunk -> {
                if (!hasError.get()) {
                    sendNarrativeChunk(emitter, speaker, chunk);
                }
            },
            () -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name("narrative-chunk")
                            .data(NarrativeStreamDTO.finish(speaker, "")));
                } catch (IllegalStateException | IOException e) {
                    log.debug("[Executor] 连接已关闭，无法发送完成标记");
                }
                latch.countDown();
            },
            error -> {
                log.error("[Executor] 特殊结局流式调用失败", error);
                hasError.set(true);
                latch.countDown();
            }
        );

        // 等待流完成
        try {
            boolean completed = latch.await(60, TimeUnit.SECONDS);
            if (!completed) {
                log.warn("[Executor] 特殊结局流式调用超时");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[Executor] 等待流完成被中断", e);
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 发送 Agent 事件到前端（带连接状态检查）
     */
    private void sendAgentEvent(SseEmitter emitter, String agentRole, String actionDesc) {
        try {
            emitter.send(SseEmitter.event()
                    .name("agent-event")
                    .data(AgentSystemEventDTO.of(agentRole, actionDesc)));
        } catch (IllegalStateException e) {
            // 连接已关闭，忽略此错误
            log.debug("[AgentOrchestrator] 连接已关闭，无法发送事件: {} - {}", agentRole, actionDesc);
        } catch (IOException e) {
            // 客户端断开连接，这是正常现象
            log.debug("[AgentOrchestrator] 客户端断开连接: {}", e.getMessage());
        }
    }

    /**
     * 安全发送 narrative-chunk 事件（带连接状态检查）
     */
    private void sendNarrativeChunk(SseEmitter emitter, String speaker, String chunk) {
        try {
            emitter.send(SseEmitter.event()
                    .name("narrative-chunk")
                    .data(NarrativeStreamDTO.chunk(speaker, chunk)));
        } catch (IllegalStateException e) {
            // 连接已关闭，忽略
            log.debug("[AgentOrchestrator] 连接已关闭，停止发送 narrative-chunk");
        } catch (IOException e) {
            log.debug("[AgentOrchestrator] 客户端断开连接，停止流式输出");
        }
    }

    /**
     * 计算结果等级
     */
    private String calculateResultLevel(int roll, int total) {
        if (roll == 20) return "大成功";
        if (roll == 1) return "大失败";
        if (total >= 15) return "成功";
        if (total >= 10) return "部分成功";
        return "失败";
    }

    /**
     * 截断字符串
     */
    private String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...";
    }

    // ==================== Strategy Agent ====================

    /**
     * Strategy Agent：生成策略建议并判断完成度
     * 
     * @param session 游戏会话
     * @param lastPlayerInput 上轮玩家输入
     * @param lastSuggestions 上轮建议（JSON字符串）
     * @param emitter SSE 发射器
     * @return 策略建议 JSON 字符串
     */
    public String runStrategy(GameSession session, String lastPlayerInput, String lastSuggestions, SseEmitter emitter) {
        sendAgentEvent(emitter, "Strategy", "🎯 分析游戏状态并生成策略建议...");
        
        // 构建游戏状态描述
        String gameState = String.format("""
            当前游戏状态：
            - 阶段：%s (Day %d/%d, %s)
            - HP：%d/%d
            - MP：%d/%d
            - 风险值：%d/100
            - 导师态度：%d (-100~100)
            - 准备度：%d/5
            - 求职进度：%s
            - 主线任务：%s
            
            【上轮行动分析】
            上轮玩家输入："%s"
            上轮建议列表：%s
            """,
            session.getGamePhase(),
            session.getCurrentDay(),
            7,
            getTimeBlockName(session.getTimeBlock()),
            session.getPlayer().getHp(),
            session.getPlayer().getMaxHp(),
            session.getPlayer().getMp(),
            session.getPlayer().getMaxMp(),
            session.getRisk(),
            session.getMentorMood(),
            session.getPreparation(),
            formatCompanyProgress(session),
            formatMainQuests(session),
            lastPlayerInput != null ? lastPlayerInput : "无（游戏刚开始）",
            lastSuggestions != null ? lastSuggestions : "无"
        );

        // 调用 LLM
        String response = llmService.chat(STRATEGY_SYSTEM_PROMPT, gameState);
        log.debug("[Strategy] 响应: {}", response);
        
        // 解析并发送事件
        try {
            if (response.contains("\"isCompleted\": true")) {
                sendAgentEvent(emitter, "Strategy", "✅ 玩家完成了建议目标！");
            } else if (response.contains("\"isCompleted\": false")) {
                sendAgentEvent(emitter, "Strategy", "⚠️ 建议目标未完成，调整策略...");
            }
        } catch (Exception e) {
            log.warn("[Strategy] 解析完成状态失败: {}", e.getMessage());
        }

        return response;
    }

    private String getTimeBlockName(int block) {
        return switch (block) {
            case 0 -> "上午";
            case 1 -> "下午";
            case 2 -> "晚上";
            case 3 -> "深夜";
            default -> "未知";
        };
    }

    private String formatCompanyProgress(GameSession session) {
        if (session.getCompanyProgresses().isEmpty()) return "未开始";
        
        StringBuilder sb = new StringBuilder();
        for (var company : session.getCompanyProgresses()) {
            sb.append(company.getCompanyName()).append(":")
              .append(company.getStage()).append(" ");
        }
        return sb.toString().trim();
    }

    private String formatMainQuests(GameSession session) {
        long completed = session.getMainQuests().values().stream().filter(v -> v).count();
        return String.format("%d/%d 完成", completed, session.getMainQuests().size());
    }

    // ==================== 结构化输出解析 ====================

    /**
     * 解析 Executor 的结构化输出
     * 从文本中提取 JSON 数据块
     * 
     * @param text Executor 输出的完整文本
     * @return 解析后的 JsonNode，如果没有找到 JSON 则返回 null
     */
    public JsonNode parseStructuredOutput(String text) {
        try {
            // 查找 JSON 代码块
            int jsonStart = text.indexOf("```json");
            if (jsonStart == -1) {
                jsonStart = text.indexOf("{");
            } else {
                jsonStart = text.indexOf("{", jsonStart);
            }
            
            int jsonEnd = text.lastIndexOf("}");
            if (jsonEnd == -1 || jsonStart == -1 || jsonStart >= jsonEnd) {
                return null;
            }
            
            String jsonStr = text.substring(jsonStart, jsonEnd + 1);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(jsonStr);
        } catch (Exception e) {
            log.warn("[AgentOrchestrator] 解析结构化输出失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 构建回合结果 JSON
     * 
     * @param narrative 剧情文本
     * @param statChanges 数值变化
     * @param questProgress 任务进度
     * @param suggestion 建议
     * @return JSON 字符串
     */
    public String buildTurnResult(String narrative, 
                                   java.util.Map<String, Integer> statChanges,
                                   java.util.List<String> questProgress,
                                   String suggestion) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("narrative", narrative);
            result.put("statChanges", statChanges);
            result.put("questProgress", questProgress);
            result.put("suggestion", suggestion);
            return mapper.writeValueAsString(result);
        } catch (Exception e) {
            log.error("[AgentOrchestrator] 构建回合结果失败", e);
            return "{}";
        }
    }
}
