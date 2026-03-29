package com.chrono.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Chrono Engine 启动类
 * 
 * 研二求生指南 - AI 驱动跑团游戏引擎
 * 
 * 游戏背景：研二学生找实习但导师不放人，需要与导师斗智斗勇。
 * 
 * 核心技术栈：
 * - Java 21 虚拟线程：高并发 SSE 流式推送
 * - Spring Boot 3.2：现代化微服务框架
 * - SSE (Server-Sent Events)：单向实时流式通信
 * 
 * 启动后访问：
 * - 健康检查：GET http://localhost:8080/api/game/health
 * - 游戏接口：GET http://localhost:8080/api/game/action?input=你好
 * 
 * @author Chrono Engine Team
 * @since Phase 1
 */
@SpringBootApplication
public class ChronoEngineApplication {

    public static void main(String[] args) {
        // 验证 Java 版本（必须 21+ 才能使用虚拟线程）
        int javaVersion = Runtime.version().feature();
        if (javaVersion < 21) {
            System.err.println("========================================");
            System.err.println("错误：Chrono Engine 需要 Java 21 或更高版本");
            System.err.println("当前版本：Java " + javaVersion);
            System.err.println("请升级 JDK 后重试");
            System.err.println("========================================");
            System.exit(1);
        }

        System.out.println("""
            ╔══════════════════════════════════════════════════════════════╗
            ║                                                              ║
            ║   ██████╗██╗  ██╗██████╗  ██████╗ ███╗   ██╗ ██████╗         ║
            ║  ██╔════╝██║  ██║██╔══██╗██╔═══██╗████╗  ██║██╔═══██╗        ║
            ║  ██║     ███████║██████╔╝██║   ██║██╔██╗ ██║██║   ██║        ║
            ║  ██║     ██╔══██║██╔══██╗██║   ██║██║╚██╗██║██║   ██║        ║
            ║  ╚██████╗██║  ██║██║  ██║╚██████╔╝██║ ╚████║╚██████╔╝        ║
            ║   ╚═════╝╚═╝  ╚═╝╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═══╝ ╚═════╝         ║
            ║                                                              ║
            ║              研二求生指南 · AI 驱动 · 跑团引擎                  ║
            ║                        Phase 1                               ║
            ║                                                              ║
            ╚══════════════════════════════════════════════════════════════╝
            """);
        
        System.out.println("🚀 正在启动 Chrono Engine...");
        System.out.println("📌 虚拟线程：已启用 (Java 21)");
        System.out.println("📌 服务端口：8080");
        System.out.println("📌 SSE 端点：/api/game/action");
        
        SpringApplication.run(ChronoEngineApplication.class, args);
        
        System.out.println("✅ Chrono Engine 启动成功！");
        System.out.println("🔗 健康检查：http://localhost:8080/api/game/health");
    }
}
