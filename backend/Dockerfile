# 多阶段构建 Dockerfile for LabEscape Backend
# 使用 Maven 官方镜像确保构建一致性

FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# 首先复制 pom.xml（利用 Docker 缓存层）
COPY pom.xml .

# 下载所有依赖（如果 pom.xml 未变化则使用缓存）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 执行构建
RUN mvn clean package -DskipTests -B

# ========================================
# 运行阶段：使用轻量级 JRE 镜像
# ========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 复制构建好的 JAR
COPY --from=builder /app/target/chrono-engine-1.0.0-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 8080

# JVM 参数优化
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC"

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/game/health || exit 1

# 启动应用
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
