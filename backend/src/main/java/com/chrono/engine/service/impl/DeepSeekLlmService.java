package com.chrono.engine.service.impl;

import com.chrono.engine.service.LlmService;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * DeepSeek LLM 服务实现
 * 
 * 使用 OpenAI 兼容 SDK 调用 DeepSeek API。
 * DeepSeek API 完全兼容 OpenAI 的 Chat Completion 接口。
 * 
 * 支持的模型：
 * - deepseek-chat (DeepSeek-V3)：通用对话，适合剧情生成
 * - deepseek-coder (DeepSeek-Coder)：代码相关任务
 * 
 * @author Chrono Engine Team
 * @since Phase 1
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekLlmService implements LlmService {

    /**
     * OpenAI Service 实例（由 LlmConfig 配置）
     * 已配置为连接 DeepSeek API
     */
    private final OpenAiService openAiService;

    /**
     * 使用的模型名称
     */
    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    /**
     * 温度参数（创造性 vs 确定性）
     * 0.0 - 1.0，越高越随机，建议剧情生成用 0.7-0.9
     */
    @Value("${deepseek.temperature:0.8}")
    private double temperature;

    /**
     * 最大生成 token 数
     * 1 个中文约等于 1-2 个 token
     */
    @Value("${deepseek.max-tokens:500}")
    private int maxTokens;

    /**
     * 同步调用
     * 
     * 一次性返回完整响应，适合内部决策场景
     */
    @Override
    public String chat(String systemPrompt, String userPrompt) {
        log.debug("[DeepSeek] 同步调用 - Model: {}, System: {}, User: {}", 
                model, truncate(systemPrompt, 50), truncate(userPrompt, 50));

        try {
            // 构建消息列表
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), userPrompt));

            // 构建请求
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .build();

            // 发送请求并获取响应
            String response = openAiService.createChatCompletion(request)
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();

            log.debug("[DeepSeek] 响应: {}", truncate(response, 100));
            return response;

        } catch (Exception e) {
            log.error("[DeepSeek] 同步调用失败", e);
            throw new RuntimeException("LLM 调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 流式调用
     * 
     * 逐字推送响应，适合剧情生成的打字机效果
     * 使用 SSE (Server-Sent Events) 风格的事件流
     */
    @Override
    public void chatStream(
            String systemPrompt,
            String userPrompt,
            Consumer<String> onChunk,
            Runnable onComplete,
            Consumer<Throwable> onError) {

        log.debug("[DeepSeek] 流式调用 - Model: {}, System: {}, User: {}", 
                model, truncate(systemPrompt, 50), truncate(userPrompt, 50));

        try {
            // 构建消息列表
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), userPrompt));

            // 构建流式请求
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .stream(true)  // 启用流式输出
                    .build();

            // 发送流式请求并处理响应
            openAiService.streamChatCompletion(request)
                    .doOnNext(chunk -> {
                        // 提取文本片段
                        String content = extractChunkContent(chunk);
                        if (content != null && !content.isEmpty()) {
                            onChunk.accept(content);
                        }
                    })
                    .doOnComplete(() -> {
                        log.debug("[DeepSeek] 流式调用完成");
                        onComplete.run();
                    })
                    .doOnError(error -> {
                        log.error("[DeepSeek] 流式调用错误", error);
                        onError.accept(error);
                    })
                    .blockingSubscribe();  // 阻塞直到流完成

        } catch (Exception e) {
            log.error("[DeepSeek] 流式调用失败", e);
            onError.accept(e);
        }
    }

    /**
     * 从 ChatCompletionChunk 中提取文本内容
     * 
     * @param chunk 流式响应片段
     * @return 文本内容，如果没有则返回 null
     */
    private String extractChunkContent(ChatCompletionChunk chunk) {
        if (chunk == null || chunk.getChoices() == null || chunk.getChoices().isEmpty()) {
            return null;
        }
        
        var delta = chunk.getChoices().get(0).getMessage();
        if (delta == null || delta.getContent() == null) {
            return null;
        }
        
        return delta.getContent();
    }

    /**
     * 检查服务可用性
     * 
     * 简单检查：尝试调用一次 API
     */
    @Override
    public boolean isAvailable() {
        try {
            chat("You are a helpful assistant", "Hello");
            return true;
        } catch (Exception e) {
            log.warn("[DeepSeek] 服务不可用", e);
            return false;
        }
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
}
