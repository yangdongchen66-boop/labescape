package com.chrono.engine.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公司求职进度实体
 * 
 * 追踪玩家在特定公司的求职流程进度
 * 从投递简历到获得Offer的完整流程
 * 
 * @author Chrono Engine Team
 * @since Phase 3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyProgress {

    /**
     * 公司名称
     * 例如："阿里", "字节", "腾讯"
     */
    private String companyName;

    /**
     * 当前阶段
     * apply: 投递简历
     * written: 笔试
     * interview1: 一面
     * interview2: 二面
     * offer: 获得Offer
     * rejected: 被拒
     */
    @Builder.Default
    private String stage = "apply";

    /**
     * 难度等级 (10-20)
     * 影响各阶段检定的DC值
     */
    @Builder.Default
    private int difficulty = 15;

    /**
     * 是否已完成整个流程（获得Offer或被拒）
     */
    @Builder.Default
    private boolean isCompleted = false;

    /**
     * 是否已获得Offer
     */
    @Builder.Default
    private boolean hasOffer = false;

    /**
     * 当前阶段的尝试次数
     * 用于计算连续失败的惩罚
     */
    @Builder.Default
    private int attemptCount = 0;

    /**
     * 获取当前阶段的检定属性
     * 
     * @return 属性名称 (INT/CHA/WIS)
     */
    public String getCheckAttribute() {
        return switch (stage) {
            case "written" -> "INT";      // 笔试需要智力
            case "interview1", "interview2" -> "CHA";  // 面试需要魅力
            case "offer" -> "WIS";      // 最终决策需要判断
            default -> "INT";
        };
    }

    /**
     * 获取当前阶段的DC值
     * 
     * @return 难度等级
     */
    public int getCurrentDC() {
        // 基础DC + 尝试次数惩罚
        return difficulty + (attemptCount > 2 ? 2 : 0);
    }

    /**
     * 推进到下一阶段
     * 
     * @return 是否成功推进
     */
    public boolean advanceStage() {
        if (isCompleted) return false;
        
        attemptCount = 0;
        
        stage = switch (stage) {
            case "apply" -> "written";
            case "written" -> "interview1";
            case "interview1" -> "interview2";
            case "interview2" -> {
                hasOffer = true;
                isCompleted = true;
                yield "offer";
            }
            default -> stage;
        };
        
        return true;
    }

    /**
     * 标记为失败
     */
    public void markFailed() {
        isCompleted = true;
        hasOffer = false;
        stage = "rejected";
    }

    /**
     * 工厂方法：创建默认公司进度
     * 
     * @param companyName 公司名称
     * @param difficulty 难度等级
     * @return 初始化的公司进度
     */
    public static CompanyProgress createNew(String companyName, int difficulty) {
        return CompanyProgress.builder()
                .companyName(companyName)
                .stage("apply")
                .difficulty(difficulty)
                .isCompleted(false)
                .hasOffer(false)
                .attemptCount(0)
                .build();
    }
}
