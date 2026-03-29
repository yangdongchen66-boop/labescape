package com.chrono.engine.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 子任务实体
 * 
 * 动态生成的可选任务，完成后提供准备度奖励
 * 
 * @author Chrono Engine Team
 * @since Phase 3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SideQuest {

    /**
     * 任务唯一标识
     */
    private String id;

    /**
     * 任务描述
     * 例如："刷题3次", "与师兄交流"
     */
    private String description;

    /**
     * 任务类型
     * study: 学习类
     * social: 社交类
     * prep: 准备类
     */
    private String type;

    /**
     * 是否已完成
     */
    @Builder.Default
    private boolean isCompleted = false;

    /**
     * 完成所需进度
     */
    @Builder.Default
    private int requiredProgress = 1;

    /**
     * 当前进度
     */
    @Builder.Default
    private int currentProgress = 0;

    /**
     * 完成奖励的准备度
     */
    @Builder.Default
    private int rewardPrep = 1;

    /**
     * 完成奖励的导师态度变化
     */
    @Builder.Default
    private int rewardMood = 0;

    /**
     * 增加进度
     * 
     * @param amount 进度增量
     * @return 是否已完成
     */
    public boolean addProgress(int amount) {
        if (isCompleted) return true;
        
        currentProgress += amount;
        if (currentProgress >= requiredProgress) {
            isCompleted = true;
            return true;
        }
        return false;
    }

    /**
     * 获取进度百分比
     * 
     * @return 0-100
     */
    public int getProgressPercent() {
        return Math.min(100, (currentProgress * 100) / requiredProgress);
    }

    /**
     * 工厂方法：创建刷题任务
     */
    public static SideQuest createStudyQuest(String id, String desc, int required) {
        return SideQuest.builder()
                .id(id)
                .description(desc)
                .type("study")
                .requiredProgress(required)
                .rewardPrep(1)
                .build();
    }

    /**
     * 工厂方法：创建社交任务
     */
    public static SideQuest createSocialQuest(String id, String desc) {
        return SideQuest.builder()
                .id(id)
                .description(desc)
                .type("social")
                .requiredProgress(1)
                .rewardPrep(1)
                .rewardMood(5)
                .build();
    }
}
