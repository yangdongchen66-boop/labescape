package com.chrono.engine.controller;

import com.chrono.engine.domain.GameSession;
import com.chrono.engine.service.GameEngineService;
import com.chrono.engine.service.impl.AgentOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 游戏控制器 (Game Controller)
 * 
 * 对外暴露 REST API 和 SSE 端点，是前后端交互的唯一入口。
 * 采用分层架构设计，Controller 层只负责：
 * - 参数校验与转换
 * - 调用 Service 层业务逻辑
 * - 返回标准化的响应格式
 * 
 * SSE (Server-Sent Events) 说明：
 * - 基于 HTTP 的单向流式通信协议
 * - 适合服务器向客户端推送实时事件（如 AI 流式生成）
 * - 比 WebSocket 更轻量，自动处理重连和心跳
 * 
 * 前端连接示例：
 * ```javascript
 * const eventSource = new EventSource('/api/game/action?sessionId=xxx&input=你好');
 * eventSource.addEventListener('agent-event', (e) => { ... });
 * eventSource.addEventListener('narrative-chunk', (e) => { ... });
 * ```
 * 
 * @author Chrono Engine Team
 * @since Phase 1
 */
@Slf4j
@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
@CrossOrigin(
    origins = "*",  // 开发阶段允许所有来源，生产环境应配置具体域名
    allowedHeaders = "*",
    exposedHeaders = "*"
)
public class GameController {

    /**
     * SSE 连接超时时间（毫秒）
     * 设置较长的超时时间，因为跑团游戏流程可能持续数分钟
     * 0 表示永不超时（由业务逻辑控制关闭）
     */
    private static final long SSE_TIMEOUT = TimeUnit.MINUTES.toMillis(5);

    /**
     * 游戏引擎服务
     * 核心业务流程由 Service 层处理，Controller 只负责"接线"
     */
    private final GameEngineService gameEngineService;

    /**
     * Agent 编排服务
     * 用于策略建议生成
     */
    private final AgentOrchestratorService agentOrchestrator;

    /**
     * 重置游戏会话
     * 
     * 前端点击"重开一局"时调用，清除后端会话状态
     * 
     * @param sessionId 会话ID
     * @return 重置结果
     */
    @PostMapping("/reset")
    public ResetResponse resetGame(@RequestParam(name = "sessionId") String sessionId) {
        log.info("[Controller] 重置游戏会话: {}", sessionId);
        boolean success = gameEngineService.resetSession(sessionId);
        return ResetResponse.builder()
                .sessionId(sessionId)
                .success(success)
                .message(success ? "游戏已重置，命运之轮再次转动" : "会话不存在")
                .build();
    }

    /**
     * 重置响应
     */
    @lombok.Builder
    @lombok.Data
    private static class ResetResponse {
        private String sessionId;
        private boolean success;
        private String message;
    }

    /**
     * 处理玩家动作 - SSE 流式接口
     * 
     * 这是整个后端最核心的接口，前端通过 SSE 连接此端点，
     * 接收多智能体协同产生的流式事件。
     * 
     * 请求流程：
     * 1. 前端建立 SSE 连接（保持 HTTP 长连接）
     * 2. 后端在虚拟线程中启动 GameEngineService
     * 3. 通过 emitter.send() 推送事件到前端
     * 4. 流程完成后 emitter.complete() 关闭连接
     * 
     * 事件类型（前端需分别监听）：
     * - "agent-event": AgentSystemEventDTO，右侧边栏日志
     * - "narrative-chunk": NarrativeStreamDTO，主屏幕打字机效果
     * 
     * @param sessionId 会话ID（可选，首次连接可留空，后端会生成）
     * @param input 玩家输入文本
     * @return SseEmitter 实例，Spring 自动管理 SSE 连接生命周期
     */
    @GetMapping(value = "/action", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter handlePlayerAction(
            @RequestParam(name = "sessionId", required = false) String sessionIdParam,
            @RequestParam(name = "input") String input,
            @RequestParam(name = "requestId", required = false) String requestId) {

        // ========== Step 1: 参数处理 ==========
        // 如果前端未提供 sessionId，生成新的 UUID
        // 实际生产环境中，sessionId 应由前端维护（localStorage/cookie）
        final String sessionId;
        if (sessionIdParam == null || sessionIdParam.isBlank()) {
            sessionId = UUID.randomUUID().toString();
            log.info("[Controller] 新生成会话ID: {}", sessionId);
        } else {
            sessionId = sessionIdParam;
        }
        
        // 如果没有requestId，生成一个
        final String finalRequestId = (requestId == null || requestId.isBlank()) 
            ? UUID.randomUUID().toString() 
            : requestId;

        // 输入截断与清理（防止超长输入和 XSS）
        final String sanitizedInput = sanitizeInput(input);
        log.info("[Controller] 收到请求 - Session: {}, RequestId: {}, Input: {}", sessionId, finalRequestId, sanitizedInput);

        // ========== Step 2: 创建 SSE 发射器 ==========
        // SseEmitter 是 Spring 提供的 SSE 工具类，封装了 HTTP 长连接细节
        // timeout=0 表示不设置超时，由业务逻辑控制连接生命周期
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // ========== Step 3: 注册回调（可选，用于日志和清理） ==========
        emitter.onCompletion(() -> {
            log.info("[Controller] SSE 连接完成 - Session: {}", sessionId);
        });
        
        emitter.onTimeout(() -> {
            log.warn("[Controller] SSE 连接超时 - Session: {}", sessionId);
        });
        
        emitter.onError((e) -> {
            log.error("[Controller] SSE 连接异常 - Session: {}", sessionId, e);
        });

        // ========== Step 4: 启动业务处理 ==========
        // 关键：在虚拟线程中执行业务逻辑，避免阻塞 Tomcat 线程池
        // Java 21 虚拟线程由 JVM 调度，可轻松支撑数万并发连接
        Thread.startVirtualThread(() -> {
            gameEngineService.processPlayerAction(sessionId, sanitizedInput, finalRequestId, emitter);
        });

        // 立即返回 emitter，HTTP 连接保持打开状态
        // 后续通过 emitter.send() 异步推送数据
        return emitter;
    }

    /**
     * 健康检查端点
     * 
     * 用于监控和负载均衡检测，返回服务状态。
     * 
     * @return 状态信息
     */
    @GetMapping("/health")
    public HealthResponse healthCheck() {
        return HealthResponse.builder()
                .status("UP")
                .service("chrono-engine")
                .version("Phase-1")
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();
    }

    /**
     * 获取会话状态（预留接口）
     * 
     * 用于前端页面刷新后恢复游戏状态。
     * Phase 1 仅返回简单信息，后续将返回完整 GameSession 状态。
     * 
     * @param sessionId 会话ID
     * @return 会话状态
     */
    @GetMapping("/session/{sessionId}")
    public SessionStatusResponse getSessionStatus(@PathVariable String sessionId) {
        log.info("[Controller] 查询会话状态: {}", sessionId);
        return SessionStatusResponse.builder()
                .sessionId(sessionId)
                .status("ACTIVE")
                .message("Phase 1 仅支持流式接口，状态查询功能预留")
                .build();
    }

    /**
     * 获取策略建议
     * 
     * 基于当前游戏状态生成策略建议
     * 
     * @param sessionId 会话ID
     * @return 策略建议 JSON
     */
    @GetMapping("/strategy")
    public StrategyResponse getStrategySuggestion(
            @RequestParam(name = "sessionId") String sessionId) {
        log.info("[Controller] 获取策略建议: {}", sessionId);
        
        // 这里应该从 sessionPool 获取真实的 GameSession
        // 简化实现：返回示例响应
        return StrategyResponse.builder()
                .sessionId(sessionId)
                .priority(java.util.Arrays.asList(
                    "刷题提升准备度",
                    "投递简历开启流程",
                    "与师兄交流获取信息"
                ))
                .riskWarning("当前风险较低，可以放心行动")
                .recommendedAction("建议先刷题提升准备度")
                .build();
    }

    /**
     * 获取完整游戏状态
     * 
     * 返回当前游戏的所有状态信息
     * 
     * @param sessionId 会话ID
     * @return 游戏状态
     */
    @GetMapping("/state")
    public GameStateResponse getGameState(
            @RequestParam(name = "sessionId") String sessionId) {
        log.info("[Controller] 获取游戏状态: {}", sessionId);
        
        // 简化实现：返回示例状态
        return GameStateResponse.builder()
                .sessionId(sessionId)
                .gamePhase("PREP")
                .currentDay(1)
                .timeBlock(2)
                .risk(0)
                .mentorMood(-10)
                .preparation(0)
                .hp(100)
                .mp(100)
                .gold(800)
                .companies(java.util.Arrays.asList(
                    Map.of("name", "阿里", "stage", "apply", "hasOffer", false)
                ))
                .mainQuests(java.util.Arrays.asList(
                    Map.of("id", "pass_written_test", "description", "通过一次笔试", "isCompleted", false),
                    Map.of("id", "get_offer", "description", "获得实习Offer", "isCompleted", false)
                ))
                .build();
    }

    /**
     * 输入清理工具方法
     * 
     * 防止 XSS 攻击和超长输入
     * 
     * @param input 原始输入
     * @return 清理后的输入
     */
    private String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        // 截断超长输入（限制 500 字符）
        String truncated = input.length() > 500 ? input.substring(0, 500) + "..." : input;
        // 基础 XSS 过滤（生产环境应使用更完善的库如 OWASP Java Encoder）
        return truncated
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .trim();
    }

    // ========== 内部 DTO 类 ==========

    /**
     * 健康检查响应
     */
    @lombok.Builder
    @lombok.Data
    private static class HealthResponse {
        private String status;
        private String service;
        private String version;
        private String timestamp;
    }

    /**
     * 会话状态响应
     */
    @lombok.Builder
    @lombok.Data
    private static class SessionStatusResponse {
        private String sessionId;
        private String status;
        private String message;
    }

    /**
     * 策略建议响应
     */
    @lombok.Builder
    @lombok.Data
    private static class StrategyResponse {
        private String sessionId;
        private java.util.List<String> priority;
        private String riskWarning;
        private String recommendedAction;
    }

    /**
     * 游戏状态响应
     */
    @lombok.Builder
    @lombok.Data
    private static class GameStateResponse {
        private String sessionId;
        private String gamePhase;
        private int currentDay;
        private int timeBlock;
        private int risk;
        private int mentorMood;
        private int preparation;
        private int hp;
        private int mp;
        private int gold;
        private java.util.List<Map<String, Object>> companies;
        private java.util.List<Map<String, Object>> mainQuests;
    }
}
