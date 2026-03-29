package com.chrono.engine.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.time.Duration;

/**
 * LLM 配置类
 * 
 * 配置 DeepSeek API 客户端，使用 OpenAI 兼容的 SDK。
 * DeepSeek API 完全兼容 OpenAI 的接口格式，只需修改 baseUrl 即可。
 * 
 * 配置项：
 * - baseUrl: https://api.deepseek.com
 * - model: deepseek-chat (DeepSeek-V3)
 * - apiKey: 从环境变量或配置文件读取
 * 
 * @author Chrono Engine Team
 * @since Phase 1
 */
@Slf4j
@Configuration
public class LlmConfig {

    /**
     * DeepSeek API Key
     * 建议从环境变量读取：DEEPSEEK_API_KEY
     * 也可在 application.yml 中配置（不推荐用于生产环境）
     */
    @Value("${DEEPSEEK_API_KEY:${deepseek.api-key:}}")
    private String apiKey;

    /**
     * DeepSeek API 基础 URL
     */
    @Value("${deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    /**
     * 请求超时时间（秒）
     */
    @Value("${deepseek.timeout-seconds:60}")
    private int timeoutSeconds;

    /**
     * 创建 OpenAiService Bean
     * 
     * 使用自定义配置连接 DeepSeek API
     * 
     * @return 配置好的 OpenAiService 实例
     */
    @Bean
    public OpenAiService openAiService() {
        // 验证 API Key
        if (apiKey == null || apiKey.isBlank()) {
            log.error("❌ DeepSeek API Key 未配置！请设置环境变量 DEEPSEEK_API_KEY 或在 application.yml 中配置");
            throw new IllegalStateException("DeepSeek API Key is required");
        }

        log.info("🔑 正在初始化 DeepSeek API 客户端...");
        log.info("📍 Base URL: {}", baseUrl);
        log.info("⏱️  Timeout: {}s", timeoutSeconds);

        // 创建带认证拦截器的 OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .readTimeout(Duration.ofSeconds(timeoutSeconds))
                .writeTimeout(Duration.ofSeconds(timeoutSeconds))
                .addInterceptor(chain -> {
                    // 添加 Authorization 请求头
                    okhttp3.Request request = chain.request().newBuilder()
                            .header("Authorization", "Bearer " + apiKey)
                            .build();
                    return chain.proceed(request);
                })
                .build();

        // 创建 ObjectMapper，配置忽略未知字段（DeepSeek 返回的字段可能比 OpenAI SDK 多）
        ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 创建 Retrofit 实例，指向 DeepSeek API
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl + "/")  // 注意：必须以 / 结尾
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        // 创建 OpenAiApi 实例
        OpenAiApi api = retrofit.create(OpenAiApi.class);

        // 创建并返回 OpenAiService
        OpenAiService service = new OpenAiService(api);
        
        log.info("✅ DeepSeek API 客户端初始化完成");
        return service;
    }

    /**
     * 获取 API Key（用于手动创建请求）
     * 
     * @return DeepSeek API Key
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * 获取 Base URL
     * 
     * @return DeepSeek API Base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }
}
