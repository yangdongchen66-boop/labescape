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
                        // 允许的源（前端域名）
                        .allowedOrigins(
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

                // 对健康检查接口也启用 CORS
                registry.addMapping("/api/game/health")
                        .allowedOrigins("*")
                        .allowedMethods("GET");
            }
        };
    }

    /**
     * 创建 CorsFilter Bean（备用方案）
     * 
     * 如果上面的 WebMvcConfigurer 不生效，可以使用这个 Filter 方式
     * 
     * @return CorsFilter 实例
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的源
        config.addAllowedOriginPattern("https://*-yangdongchens-projects.vercel.app");
        config.addAllowedOriginPattern("https://frontend-*.vercel.app");
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://localhost:3000");
        
        // 允许所有 headers
        config.addAllowedHeader("*");
        
        // 允许所有 methods
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("PATCH");
        
        // 允许携带认证信息
        config.setAllowCredentials(true);
        
        // 暴露 headers
        config.addExposedHeader("Content-Type");
        config.addExposedHeader("Authorization");
        config.addExposedHeader("X-Requested-With");
        
        // 预检请求缓存时间
        config.setMaxAge(3600L);
        
        // 应用到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
