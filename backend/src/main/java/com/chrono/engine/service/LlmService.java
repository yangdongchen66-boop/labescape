package com.chrono.engine.service;

import java.util.function.Consumer;

/**
 * LLM 服务接口
 * 
 * 定义与大语言模型交互的契约，支持：
 * - 同步调用：一次性返回完整响应
 * - 流式调用：逐字推送响应（SSE 场景）
 * 
 * 设计模式：策略模式，便于后续切换不同 LLM 提供商
 * （DeepSeek、OpenAI、Claude 等）
 * 
 * @author Chrono Engine Team
 * @since Phase 1
 */
public interface LlmService {

    /**
     * 同步调用 LLM
     * 
     * 适用于不需要流式输出的场景，如 Manager/Supervisor 的内部决策
     * 
     * @param systemPrompt 系统提示词（定义 AI 角色和行为）
     * @param userPrompt 用户提示词（玩家输入或内部指令）
     * @return LLM 生成的完整文本
     */
    String chat(String systemPrompt, String userPrompt);

    /**
     * 流式调用 LLM
     * 
     * 适用于需要打字机效果的场景，如 Executor 生成剧情
     * 通过回调函数逐字接收响应
     * 
     * @param systemPrompt 系统提示词
     * @param userPrompt 用户提示词
     * @param onChunk 回调函数，接收每个文本片段
     * @param onComplete 回调函数，流完成时调用
     * @param onError 回调函数，发生错误时调用
     */
    void chatStream(
            String systemPrompt,
            String userPrompt,
            Consumer<String> onChunk,
            Runnable onComplete,
            Consumer<Throwable> onError
    );

    /**
     * 检查 LLM 服务是否可用
     * 
     * @return true 如果服务正常
     */
    boolean isAvailable();
}
