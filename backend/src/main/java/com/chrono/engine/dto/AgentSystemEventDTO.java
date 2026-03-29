package com.chrono.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Agent 系统事件 DTO
 * 
 * 用于向前端右侧边栏推送"幕后日志"，展示多智能体协同的工作流程。
 * 这是 Chrono-Agents 引擎的核心透明度机制，让玩家看到 AI 的"思考过程"。
 * 
 * 业务场景：
 * - Manager 解析玩家意图时推送
 * - Supervisor 进行掷骰子检定时推送
 * - Executor 准备生成剧情时推送
 * 
 * @author Chrono Engine Team
 * @since Phase 1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentSystemEventDTO {

    /**
     * Agent 角色标识
     * 可选值：
     * - "Manager" (调度者)：负责意图解析和任务分发
     * - "Supervisor" (命运裁判)：负责规则判定和掷骰子
     * - "Executor" (角色化身)：负责剧情生成和 NPC 扮演
     * - "Director" (幻境织工)：负责场景氛围控制（预留）
     */
    private String agentRole;

    /**
     * 动作描述
     * 人类可读的 Agent 行为描述，例如：
     * - "解析玩家意图 -> [尝试说服导师]"
     * - "发起 [魅力] 检定... 掷骰结果: 18 (大成功)"
     * - "注入 NPC [王导] 记忆，生成回应中..."
     */
    private String actionDesc;

    /**
     * 事件时间戳 (ISO-8601 格式)
     * 用于前端按时间顺序展示日志流
     */
    private String timestamp;

    /**
     * 便捷工厂方法：自动填充当前时间
     * 
     * @param agentRole Agent 角色
     * @param actionDesc 动作描述
     * @return 构建好的 DTO 实例
     */
    public static AgentSystemEventDTO of(String agentRole, String actionDesc) {
        return AgentSystemEventDTO.builder()
                .agentRole(agentRole)
                .actionDesc(actionDesc)
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }
}
