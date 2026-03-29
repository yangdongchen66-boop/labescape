package com.chrono.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 叙事流 DTO
 * 
 * 用于向前端主屏幕逐字推送剧情文本，实现"打字机"式的流式叙事效果。
 * 这是跑团游戏的核心体验——让玩家感受到故事是"实时生成"的。
 * 
 * 业务场景：
 * - Executor 生成剧情时，将长文本切分为多个 chunk 逐段推送
 * - 每个 chunk 包含少量文字（2-4 个汉字），配合 100-300ms 延迟
 * - 最终推送 isFinished=true 标记流结束
 * 
 * @author Chrono Engine Team
 * @since Phase 1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NarrativeStreamDTO {

    /**
     * 发言者标识
     * 可选值：
     * - "DM" (守秘者/游戏主持)：旁白、场景描述、系统播报
     * - "NPC:{名字}" (非玩家角色)：如 "NPC:王导"
     * - "PLAYER" (玩家)：回显玩家自己的输入（可选）
     */
    private String speaker;

    /**
     * 文本片段（chunk）
     * 这是流式推送的最小单位，通常 2-4 个汉字，例如：
     * - "你"
     * - "轻巧地"
     * - "绕过了"
     * - "守卫"
     * 
     * 前端会将这些 chunk 拼接成完整句子
     */
    private String contentChunk;

    /**
     * 流结束标记
     * - false：还有更多 chunk 即将到来
     * - true：这是最后一个 chunk，流已结束
     * 
     * 前端收到 true 后，可以：
     * - 显示"输入框可用"状态
     * - 播放完成音效
     * - 允许玩家进行下一轮输入
     */
    private boolean isFinished;

    /**
     * 便捷工厂方法：创建中间片段
     * 
     * @param speaker 发言者
     * @param contentChunk 文本片段
     * @return 未完成的流式 DTO
     */
    public static NarrativeStreamDTO chunk(String speaker, String contentChunk) {
        return NarrativeStreamDTO.builder()
                .speaker(speaker)
                .contentChunk(contentChunk)
                .isFinished(false)
                .build();
    }

    /**
     * 便捷工厂方法：创建结束标记
     * 
     * @param speaker 发言者
     * @param finalChunk 最后一段文本（可为空字符串）
     * @return 标记为完成的流式 DTO
     */
    public static NarrativeStreamDTO finish(String speaker, String finalChunk) {
        return NarrativeStreamDTO.builder()
                .speaker(speaker)
                .contentChunk(finalChunk)
                .isFinished(true)
                .build();
    }
}
