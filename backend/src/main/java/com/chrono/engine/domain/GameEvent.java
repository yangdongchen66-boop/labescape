package com.chrono.engine.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 游戏事件实体
 * 
 * 代表游戏中触发的各种事件，包括导师突袭、求职进展、环境变化等
 * 
 * @author Chrono Engine Team
 * @since Phase 3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameEvent {

    /**
     * 事件唯一标识
     */
    private String id;

    /**
     * 事件类型
     * - MENTOR: 导师相关事件
     * - JOB: 求职相关事件
     * - ENVIRONMENT: 环境事件
     * - SOCIAL: 社交事件
     * - SYSTEM: 系统事件
     */
    private String type;

    /**
     * 事件标题
     */
    private String title;

    /**
     * 事件描述
     */
    private String description;

    /**
     * 事件效果
     */
    @Builder.Default
    private EventEffect effect = new EventEffect();

    /**
     * 触发条件
     */
    private TriggerCondition triggerCondition;

    /**
     * 是否强制触发（不可跳过）
     */
    @Builder.Default
    private boolean isMandatory = false;

    /**
     * 是否已触发
     */
    @Builder.Default
    private boolean isTriggered = false;

    /**
     * 事件效果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventEffect {
        // 数值变化
        @Builder.Default
        private int hpChange = 0;
        @Builder.Default
        private int mpChange = 0;
        @Builder.Default
        private int riskChange = 0;
        @Builder.Default
        private int mentorMoodChange = 0;
        @Builder.Default
        private int preparationChange = 0;
        
        // 特殊效果
        @Builder.Default
        private boolean forceRest = false;  // 强制休息
        @Builder.Default
        private boolean blockActions = false;  // 阻止某些行动
        @Builder.Default
        private List<String> availableActions = null;  // 限定可用行动
    }

    /**
     * 触发条件
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TriggerCondition {
        // 时间条件
        private Integer minDay;
        private Integer maxDay;
        private Integer timeBlock;
        
        // 状态条件
        private Integer minRisk;
        private Integer maxRisk;
        private Integer minMentorMood;
        private Integer maxMentorMood;
        
        // 概率条件 (0-100)
        @Builder.Default
        private int probability = 100;
        
        // 前置事件
        private List<String> requiredEvents;
        
        // 前置任务
        private List<String> requiredQuests;
    }

    /**
     * 检查事件是否可以触发
     */
    public boolean canTrigger(GameSession session) {
        if (triggerCondition == null) return true;
        
        // 检查时间
        if (triggerCondition.getMinDay() != null && 
            session.getCurrentDay() < triggerCondition.getMinDay()) {
            return false;
        }
        if (triggerCondition.getMaxDay() != null && 
            session.getCurrentDay() > triggerCondition.getMaxDay()) {
            return false;
        }
        if (triggerCondition.getTimeBlock() != null && 
            session.getTimeBlock() != triggerCondition.getTimeBlock()) {
            return false;
        }
        
        // 检查风险
        if (triggerCondition.getMinRisk() != null && 
            session.getRisk() < triggerCondition.getMinRisk()) {
            return false;
        }
        if (triggerCondition.getMaxRisk() != null && 
            session.getRisk() > triggerCondition.getMaxRisk()) {
            return false;
        }
        
        // 检查导师态度
        if (triggerCondition.getMinMentorMood() != null && 
            session.getMentorMood() < triggerCondition.getMinMentorMood()) {
            return false;
        }
        if (triggerCondition.getMaxMentorMood() != null && 
            session.getMentorMood() > triggerCondition.getMaxMentorMood()) {
            return false;
        }
        
        return true;
    }

    /**
     * 应用事件效果到游戏会话
     */
    public void applyEffect(GameSession session) {
        if (effect == null) return;
        
        PlayerCharacter player = session.getPlayer();
        
        // 应用数值变化
        if (effect.getHpChange() != 0) {
            int newHp = Math.max(0, Math.min(player.getMaxHp(), 
                player.getHp() + effect.getHpChange()));
            player.setHp(newHp);
        }
        
        if (effect.getMpChange() != 0) {
            int newMp = Math.max(0, Math.min(player.getMaxMp(), 
                player.getMp() + effect.getMpChange()));
            player.setMp(newMp);
        }
        
        if (effect.getRiskChange() != 0) {
            int newRisk = Math.max(0, Math.min(100, 
                session.getRisk() + effect.getRiskChange()));
            session.setRisk(newRisk);
        }
        
        if (effect.getMentorMoodChange() != 0) {
            int newMood = Math.max(-100, Math.min(100, 
                session.getMentorMood() + effect.getMentorMoodChange()));
            session.setMentorMood(newMood);
        }
        
        if (effect.getPreparationChange() != 0) {
            int newPrep = Math.max(0, Math.min(5, 
                session.getPreparation() + effect.getPreparationChange()));
            session.setPreparation(newPrep);
        }
        
        isTriggered = true;
    }

    // ==================== 预设事件工厂方法 ====================

    /**
     * 导师突袭检查
     */
    public static GameEvent mentorRaid() {
        return GameEvent.builder()
                .id("mentor_raid")
                .type("MENTOR")
                .title("导师突袭")
                .description("王导突然出现在实验室，要求查看你的进度。")
                .effect(EventEffect.builder()
                        .hpChange(-10)
                        .mpChange(-15)
                        .riskChange(-10)
                        .build())
                .triggerCondition(TriggerCondition.builder()
                        .minRisk(70)
                        .probability(50)
                        .build())
                .isMandatory(true)
                .build();
    }

    /**
     * HR主动联系
     */
    public static GameEvent hrContact() {
        return GameEvent.builder()
                .id("hr_contact")
                .type("JOB")
                .title("HR主动联系")
                .description("某大厂HR看到你的简历，主动邀请你参加笔试。")
                .effect(EventEffect.builder()
                        .mpChange(10)
                        .preparationChange(1)
                        .build())
                .triggerCondition(TriggerCondition.builder()
                        .minDay(2)
                        .maxDay(5)
                        .probability(20)
                        .build())
                .build();
    }

    /**
     * 实验室断电
     */
    public static GameEvent powerOutage() {
        return GameEvent.builder()
                .id("power_outage")
                .type("ENVIRONMENT")
                .title("实验室断电")
                .description("突然断电，你失去了未保存的工作进度。")
                .effect(EventEffect.builder()
                        .hpChange(-5)
                        .mpChange(-10)
                        .riskChange(5)
                        .build())
                .triggerCondition(TriggerCondition.builder()
                        .probability(10)
                        .build())
                .build();
    }

    /**
     * 学长建议
     */
    public static GameEvent seniorAdvice() {
        return GameEvent.builder()
                .id("senior_advice")
                .type("SOCIAL")
                .title("学长建议")
                .description("学长分享了他当年应付导师的经验。")
                .effect(EventEffect.builder()
                        .mpChange(5)
                        .mentorMoodChange(5)
                        .preparationChange(1)
                        .build())
                .triggerCondition(TriggerCondition.builder()
                        .minDay(1)
                        .probability(25)
                        .build())
                .build();
    }

    /**
     * 组会汇报（强制事件）
     */
    public static GameEvent weeklyMeeting() {
        return GameEvent.builder()
                .id("weekly_meeting")
                .type("MENTOR")
                .title("组会汇报")
                .description("被迫参加组会，汇报项目进度。")
                .effect(EventEffect.builder()
                        .hpChange(-10)
                        .mpChange(-15)
                        .mentorMoodChange(5)
                        .build())
                .triggerCondition(TriggerCondition.builder()
                        .minDay(2)
                        .timeBlock(0)  // 上午
                        .probability(100)
                        .build())
                .isMandatory(true)
                .build();
    }

    /**
     * Deadline提醒（强制事件）
     */
    public static GameEvent deadlineReminder() {
        return GameEvent.builder()
                .id("deadline_reminder")
                .type("SYSTEM")
                .title("Deadline临近")
                .description("内推截止日期即将到来，时间紧迫！")
                .effect(EventEffect.builder()
                        .mpChange(-10)
                        .riskChange(10)
                        .build())
                .triggerCondition(TriggerCondition.builder()
                        .minDay(5)
                        .probability(100)
                        .build())
                .isMandatory(true)
                .build();
    }
}
