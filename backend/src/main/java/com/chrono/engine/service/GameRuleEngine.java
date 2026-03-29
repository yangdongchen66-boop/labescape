package com.chrono.engine.service;

import com.chrono.engine.domain.GameSession;
import com.chrono.engine.domain.PlayerCharacter;
import com.chrono.engine.domain.SideQuest;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 游戏规则引擎 (Game Rule Engine)
 * 
 * 负责所有游戏数值计算和规则判定
 * 包括检定计算、时间推进、阶段转换等核心逻辑
 * 
 * @author Chrono Engine Team
 * @since Phase 3
 */
@Slf4j
@Service
public class GameRuleEngine {

    private final Random random = new Random();

    // ==================== 检定系统 ====================

    /**
     * 检定结果
     */
    @Data
    @Builder
    public static class CheckResult {
        private int d20;           // 原始骰子结果
        private int modifier;      // 属性修正值
        private int preparation;   // 准备度加成
        private int riskPenalty;   // 风险惩罚
        private int moodModifier;  // 导师态度修正
        private int total;         // 最终总值
        private boolean success;   // 是否成功
        private boolean critical;  // 大成功
        private boolean fumble;    // 大失败
        private int dc;            // 目标DC
    }

    /**
     * 计算检定结果
     * 
     * 完整公式: d20 + 属性修正 + 准备度 - 风险惩罚 + 导师态度修正
     * 
     * @param player 玩家角色
     * @param attribute 检定属性 (STR/DEX/CON/INT/WIS/CHA)
     * @param dc 目标难度
     * @param session 游戏会话
     * @return 检定结果
     */
    public CheckResult calculateCheck(
            PlayerCharacter player,
            String attribute,
            int dc,
            GameSession session) {
        
        int d20 = rollD20();
        int modifier = player.getModifier(attribute);
        int prep = session.getPreparation();
        int riskPenalty = calculateRiskPenalty(session.getRisk());
        int moodModifier = session.getMentorMood() / 10; // 导师态度每10点提供±1修正
        
        int total = d20 + modifier + prep - riskPenalty + moodModifier;
        
        return CheckResult.builder()
                .d20(d20)
                .modifier(modifier)
                .preparation(prep)
                .riskPenalty(riskPenalty)
                .moodModifier(moodModifier)
                .total(total)
                .success(total >= dc)
                .critical(d20 == 20)
                .fumble(d20 == 1)
                .dc(dc)
                .build();
    }

    /**
     * 掷20面骰
     */
    public int rollD20() {
        return random.nextInt(20) + 1;
    }

    /**
     * 计算风险惩罚
     * 
     * risk > 80: -5
     * risk > 50: -2
     * 其他: 0
     */
    private int calculateRiskPenalty(int risk) {
        if (risk > 80) return 5;
        if (risk > 50) return 2;
        return 0;
    }

    // ==================== 时间推进系统 ====================

    /**
     * 时间推进结果
     */
    @Data
    @Builder
    public static class TimeAdvanceResult {
        private int oldDay;
        private int newDay;
        private int oldBlock;
        private int newBlock;
        private boolean dayChanged;
        private boolean phaseChanged;
        private String newPhase;
        private List<String> triggeredEvents;
    }

    /**
     * 推进时间
     * 
     * 一天分为4个时间块：
     * 0: 上午 (高风险)
     * 1: 下午 (可操作)
     * 2: 晚上 (高收益)
     * 3: 深夜 (高风险高收益)
     * 
     * @param session 游戏会话
     * @param blocks 推进的时间块数
     * @return 时间推进结果
     */
    public TimeAdvanceResult advanceTime(GameSession session, int blocks) {
        int oldDay = session.getCurrentDay();
        int oldBlock = session.getTimeBlock();
        
        int totalBlocks = oldBlock + blocks;
        int newDay = oldDay + (totalBlocks / 4);
        int newBlock = totalBlocks % 4;
        
        // 限制在7天内
        if (newDay > 7) {
            newDay = 7;
            newBlock = 3;
        }
        
        session.setCurrentDay(newDay);
        session.setTimeBlock(newBlock);
        session.setInGameHours(newDay * 24 + newBlock * 6);
        
        // 检查阶段转换
        boolean phaseChanged = false;
        String newPhase = session.getGamePhase();
        String calculatedPhase = calculatePhase(newDay);
        
        if (!calculatedPhase.equals(session.getGamePhase())) {
            session.setGamePhase(calculatedPhase);
            newPhase = calculatedPhase;
            phaseChanged = true;
        }
        
        // 检查触发的事件
        List<String> triggeredEvents = checkTimeEvents(session, oldDay, newDay);
        
        return TimeAdvanceResult.builder()
                .oldDay(oldDay)
                .newDay(newDay)
                .oldBlock(oldBlock)
                .newBlock(newBlock)
                .dayChanged(newDay != oldDay)
                .phaseChanged(phaseChanged)
                .newPhase(newPhase)
                .triggeredEvents(triggeredEvents)
                .build();
    }

    /**
     * 根据天数计算游戏阶段
     */
    private String calculatePhase(int day) {
        if (day <= 2) return "PREP";           // 准备期 Day 1-2
        if (day <= 5) return "BREAKTHROUGH";   // 突破期 Day 3-5
        return "FINAL";                        // 收官期 Day 6-7
    }

    /**
     * 检查时间相关事件
     */
    private List<String> checkTimeEvents(GameSession session, int oldDay, int newDay) {
        List<String> events = new ArrayList<>();
        
        // Day 1: 初始事件
        if (oldDay < 1 && newDay >= 1) {
            events.add("INIT");
        }
        
        // Day 2: 解锁公司流程
        if (oldDay < 2 && newDay >= 2) {
            events.add("UNLOCK_COMPANIES");
        }
        
        // Day 3: 进入突破期
        if (oldDay < 3 && newDay >= 3) {
            events.add("PHASE_BREAKTHROUGH");
        }
        
        // Day 5: 终面截止提醒
        if (oldDay < 5 && newDay >= 5) {
            events.add("FINAL_INTERVIEW_DEADLINE");
        }
        
        // Day 6: 进入收官期
        if (oldDay < 6 && newDay >= 6) {
            events.add("PHASE_FINAL");
        }
        
        // Day 7: 最终截止
        if (oldDay < 7 && newDay >= 7) {
            events.add("FINAL_DEADLINE");
        }
        
        return events;
    }

    // ==================== 风险系统 ====================

    /**
     * 增加风险值
     * 
     * @param session 游戏会话
     * @param delta 风险增量
     * @return 更新后的风险值
     */
    public int increaseRisk(GameSession session, int delta) {
        int newRisk = Math.min(100, session.getRisk() + delta);
        session.setRisk(newRisk);
        
        // 风险值>=70触发导师突袭检查
        if (newRisk >= 70 && session.getRisk() < 70) {
            log.info("[Risk] 风险值达到{}，导师突袭风险增加", newRisk);
        }
        
        return newRisk;
    }

    /**
     * 降低风险值
     */
    public int decreaseRisk(GameSession session, int delta) {
        int newRisk = Math.max(0, session.getRisk() - delta);
        session.setRisk(newRisk);
        return newRisk;
    }

    // ==================== 导师态度系统 ====================

    /**
     * 更新导师态度
     * 
     * @param session 游戏会话
     * @param delta 态度变化（正数增加好感，负数降低）
     * @return 更新后的态度值
     */
    public int updateMentorMood(GameSession session, int delta) {
        int newMood = Math.max(-100, Math.min(100, session.getMentorMood() + delta));
        session.setMentorMood(newMood);
        
        // 同步更新NPC态度
        String attitude = newMood > 20 ? "友好" : 
                         newMood > 0 ? "一般" : 
                         newMood > -30 ? "不满" : "愤怒";
        log.debug("[Mentor] 态度更新为 {} ({})", newMood, attitude);
        
        return newMood;
    }

    // ==================== 准备度系统 ====================

    /**
     * 增加准备度
     * 
     * @param session 游戏会话
     * @param delta 准备度增量
     * @return 更新后的准备度
     */
    public int increasePreparation(GameSession session, int delta) {
        int newPrep = Math.min(5, session.getPreparation() + delta);
        session.setPreparation(newPrep);
        return newPrep;
    }

    /**
     * 检查并完成任务
     * 
     * @param session 游戏会话
     * @param questId 任务ID
     * @return 是否完成
     */
    public boolean checkSideQuestCompletion(GameSession session, String questId) {
        for (SideQuest quest : session.getSideQuests()) {
            if (quest.getId().equals(questId) && !quest.isCompleted()) {
                boolean completed = quest.addProgress(1);
                if (completed) {
                    // 发放奖励
                    increasePreparation(session, quest.getRewardPrep());
                    updateMentorMood(session, quest.getRewardMood());
                    log.info("[Quest] 任务完成: {}, 奖励准备度+{}, 导师态度+{}", 
                        quest.getDescription(), quest.getRewardPrep(), quest.getRewardMood());
                }
                return completed;
            }
        }
        return false;
    }

    // ==================== 心态状态派生 ====================

    /**
     * 获取心态状态描述
     * 
     * @param mp 当前心态值
     * @return 状态描述
     */
    public String getMentalState(int mp) {
        if (mp >= 70) return "正常";
        if (mp >= 40) return "焦虑";
        if (mp >= 10) return "崩溃边缘";
        return "崩溃";
    }

    /**
     * 获取心态状态对检定的影响
     * 
     * @param mp 当前心态值
     * @return 修正值
     */
    public int getMentalStateModifier(int mp) {
        if (mp >= 70) return 0;
        if (mp >= 40) return -1;
        if (mp >= 10) return -3;
        return -5; // 崩溃状态
    }

    // ==================== 游戏结束判定 ====================

    /**
     * 检查游戏是否结束
     * 
     * @param session 游戏会话
     * @return 结局类型，null表示游戏继续
     */
    public String checkGameEnd(GameSession session) {
        // 检查心态崩溃
        if (session.getPlayer().getMp() <= 0) {
            return "MENTAL_BREAKDOWN";
        }
        
        // 检查时间耗尽
        if (session.getCurrentDay() >= 7 && session.getTimeBlock() >= 3) {
            // 判断是否获得Offer和导师同意
            boolean hasOffer = session.getCompanyProgresses().stream()
                    .anyMatch(c -> c.isHasOffer());
            boolean mentorApproved = session.getMentorMood() >= 0;
            
            if (hasOffer && mentorApproved) return "PERFECT";
            if (hasOffer) return "ESCAPE";
            return "FAILURE";
        }
        
        return null; // 游戏继续
    }

    /**
     * 获取结局描述
     */
    public String getEndingDescription(String endingType) {
        return switch (endingType) {
            case "PERFECT" -> "完美结局：你成功获得了实习Offer，并且导师也同意了你的申请。双喜临门！";
            case "ESCAPE" -> "偷跑结局：你拿到了Offer，但和导师的关系彻底破裂。这是你想要的吗？";
            case "FAILURE" -> "失败结局：时间耗尽，你既没有拿到Offer，也没有处理好导师关系。明年再战吧。";
            case "MENTAL_BREAKDOWN" -> "崩溃结局：巨大的压力让你彻底崩溃。身体最重要，先休息一下吧。";
            default -> "未知结局";
        };
    }
}
