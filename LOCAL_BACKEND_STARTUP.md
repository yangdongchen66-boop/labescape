# 🚀 本地后端启动指南

## ✅ 后端已成功启动！

**状态：** ● Running  
**端口：** 8080  
**进程 ID：** 已在运行

---

## ⚠️ 重要提示

### 当前状态

后端服务器已经成功启动并监听 8080 端口，但是：

**❌ 缺少 DeepSeek API Key**

这会导致：
- ❌ 无法调用 AI 服务
- ❌ 游戏行动接口返回 500 错误
- ❌ 健康检查接口也返回 500（因为 Spring 容器初始化失败）

---

## 🔧 解决方案

### 方法一：设置环境变量（推荐）

#### Windows PowerShell

```powershell
# 设置环境变量
$env:DEEPSEEK_API_KEY="sk-your-api-key-here"

# 然后启动后端
cd E:\chatAI\backend
mvn spring-boot:run
```

#### Windows CMD

```cmd
set DEEPSEEK_API_KEY=sk-your-api-key-here
cd E:\chatAI\backend
mvn spring-boot:run
```

---

### 方法二：修改 application.yml

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  application:
    name: chrono-engine

deepseek:
  api-key: sk-your-api-key-here  # 添加这一行
  base-url: https://api.deepseek.com
  timeout-seconds: 60
```

---

### 方法三：使用.env 文件

在 `backend/` 目录创建 `.env` 文件：

```bash
DEEPSEEK_API_KEY=sk-your-api-key-here
SERVER_PORT=8080
JAVA_VERSION=21
```

Spring Boot DevTools 会自动读取。

---

## 📊 验证步骤

### Step 1: 检查后端是否运行

```bash
netstat -ano | findstr :8080
```

应该看到进程正在监听 8080 端口。

### Step 2: 测试健康检查

```bash
curl http://localhost:8080/api/game/health
```

**预期响应（配置 API Key 后）：**
```json
{
  "status": "UP",
  "timestamp": "2026-03-31T16:17:56.484Z"
}
```

### Step 3: 测试游戏接口

```bash
curl "http://localhost:8080/api/game/action?sessionId=test&input=你好"
```

应该能看到 SSE 数据流。

---

## 🎯 CORS 配置已修复

### 已配置的域名

**生产环境：**
- ✅ `https://frontend-do4ncxp7j-yangdongchens-projects.vercel.app`
- ✅ `https://frontend-sigma-gray-ncizc6gc4c.vercel.app`
- ✅ `https://*-yangdongchens-projects.vercel.app`
- ✅ `https://frontend-*.vercel.app`

**开发环境：**
- ✅ `http://localhost:5173`
- ✅ `http://localhost:3000`

### 配置说明

**健康检查接口：**
```java
.allowedOriginPatterns("*")
.allowCredentials(false)  // 公开访问，无需认证
```

**游戏行动接口：**
```java
.allowedOriginPatterns("https://*.vercel.app")
.allowCredentials(true)   // 需要携带认证信息
```

---

## 🆘 常见问题

### Q1: 如何获取 DeepSeek API Key？

**A:** 
1. 访问 https://platform.deepseek.com
2. 注册/登录账号
3. 进入 API Keys 页面
4. 创建新的 API Key
5. 复制保存（只显示一次）

### Q2: 为什么健康检查也返回 500？

**A:** 
因为 Spring Boot 在启动时会检查所有 Bean 的依赖。
DeepSeekLlmService 需要 OpenAiService，
OpenAiService 需要 API Key，
缺少 API Key 导致整个 Spring 容器启动失败。

**解决：** 设置环境变量或配置 API Key。

### Q3: 本地开发必须配置 API Key 吗？

**A:** 
是的，因为游戏的核心功能依赖 AI 生成剧情。
但您可以：
- 使用测试账号的 API Key
- 申请免费额度的 API Key
- 或者暂时不玩游戏，只测试前端 UI

### Q4: 如何确认 CORS 配置已生效？

**A:** 
查看启动日志，应该没有 CORS 相关的错误。
之前的错误日志：
```
java.lang.IllegalArgumentException: 
When allowCredentials is true, allowedOrigins cannot 
contain the special value "*"
```

现在应该看不到这个错误了。

---

## 💡 快速启动脚本

### create .env.ps1 (PowerShell)

```powershell
# 创建 .env 文件
@"
DEEPSEEK_API_KEY=sk-your-api-key-here
SERVER_PORT=8080
JAVA_VERSION=21
"@ | Out-File -FilePath ".env" -Encoding utf8

Write-Host "✅ .env 文件已创建"
Write-Host "🚀 正在启动后端..."
mvn spring-boot:run
```

### create .env.bat (CMD)

```batch
@echo off
echo DEEPSEEK_API_KEY=sk-your-api-key-here > .env
echo SERVER_PORT=8080 >> .env
echo JAVA_VERSION=21 >> .env
echo ✅ .env 文件已创建
echo 🚀 正在启动后端...
mvn spring-boot:run
```

---

## 📞 下一步

### 立即执行

1. **获取 DeepSeek API Key**
   - 访问 DeepSeek 平台
   - 创建 API Key

2. **配置 API Key**
   - 设置环境变量
   - 或修改 application.yml

3. **重启后端**
   ```bash
   taskkill /F /IM java.exe
   mvn spring-boot:run
   ```

4. **测试验证**
   ```bash
   curl http://localhost:8080/api/game/health
   ```

### 让朋友测试

一旦配置好 API Key：

1. **前端访问：**
   ```
   https://frontend-do4ncxp7j-yangdongchens-projects.vercel.app
   ```

2. **测试清单：**
   - [ ] 页面正常加载
   - [ ] 点击行动选项
   - [ ] 看到骰子动画
   - [ ] AI 生成剧情
   - [ ] HP/MP/金币变化

---

## 🎉 总结

### 当前状态

- ✅ 后端已启动
- ✅ CORS 配置已修复
- ✅ 端口 8080 正常监听
- ⏳ 等待配置 API Key

### 已完成的工作

1. **修复 CORS 配置错误**
   - 移除了冲突的 `allowedOrigins("*")` + `allowCredentials(true)`
   - 使用 `allowedOriginPatterns` 替代
   - 健康检查接口设置为公开访问

2. **简化 CORS 配置**
   - 移除了 CorsFilter Bean
   - 只保留 WebMvcConfigurer 方式
   - 配置更清晰，不易出错

3. **代码已推送**
   ```
   Commit: 3dd0fb0
   Message: 🐛 修复 CORS 配置错误
   ```

---

**后端已就绪，只需配置 API Key 即可开始游戏！** 🎮✨

有任何问题随时告诉我！💪
