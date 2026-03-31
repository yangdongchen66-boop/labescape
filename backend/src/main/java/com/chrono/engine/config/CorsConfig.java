package com.chrono.engine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS（跨域资源共享）配置
 * 
 * 允许前端从 Vercel 访问 Railway 后端 API。
 * 
 * 配置的域名：
 * - Vercel 生产环境：https://frontend-*.vercel.app
 * - Vercel 预览环境：https://*-yangdongchens-projects.vercel.app
 * - 本地开发：http://localhost:5173
 * 
 * @author Chrono Engine Team
 * @since Phase 1
 */
@Configuration
public class CorsConfig {

    /**
     * 全局 CORS 配置
     * 
     * @return WebMvcConfigurer 配置
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // 对所有 API 路径启用 CORS
                registry.addMapping("/api/**")
                        // 允许的源（使用 OriginPatterns 支持通配符）
                        .allowedOriginPatterns(
                            // Vercel 生产部署
                            "https://frontend-do4ncxp7j-yangdongchens-projects.vercel.app",
                            "https://frontend-sigma-gray-ncizc6gc4c.vercel.app",
                            // Vercel 预览部署（通配符）
                            "https://*-yangdongchens-projects.vercel.app",
                            // 本地开发环境
                            "http://localhost:5173",
                            "http://localhost:3000",
                            "http://127.0.0.1:5173"
                        )
                        // 允许所有 HTTP 方法
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        // 允许携带认证信息（cookies、HTTP headers 等）
                        .allowCredentials(true)
                        // 允许所有 headers
                        .allowedHeaders("*")
                        // 暴露 headers 给前端
                        .exposedHeaders("Content-Type", "Authorization", "X-Requested-With")
                        // 预检请求缓存时间（秒）
                        .maxAge(3600);

                // 对健康检查接口也启用 CORS（允许所有来源，但不携带认证信息）
                registry.addMapping("/api/game/health")
                        .allowedOriginPatterns("*")
                        .allowedMethods("GET")
                        .allowCredentials(false);
            }
        };
    }
}
