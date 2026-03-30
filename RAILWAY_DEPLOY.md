# Railway 部署指南

## 🚀 快速部署后端

### 第一步：准备 Railway 项目

1. **访问 Railway**
   - 打开 https://railway.app
   - 使用 GitHub 账号登录

2. **新建项目**
   - 点击 "New Project"
   - 选择 "Deploy from GitHub repo"
   - 连接你的 GitHub 仓库（需要先将代码推送到 GitHub）

### 第二步：配置服务

1. **添加 backend 目录**
   ```
   Settings → Root Directory: backend
   ```

2. **设置环境变量**
   在 Railway 控制台的 Variables 中添加：
   ```bash
   DEEPSEEK_API_KEY=your_api_key_here
   SERVER_PORT=8080
   JAVA_VERSION=21
   ```

3. **配置启动命令**
   ```bash
   # Build Command
   mvn clean package -DskipTests
   
   # Start Command
   java -jar target/chrono-engine-*.jar
   ```

### 第三步：获取后端 URL

部署成功后，Railway 会提供类似这样的 URL：
```
https://labescape-backend-production.up.railway.app
```

### 第四步：更新 Vercel 环境变量

回到 Vercel 控制台：
1. 进入项目设置
2. 找到 Environment Variables
3. 添加：`VITE_API_BASE_URL=https://your-railway-url.app`
4. 重新部署前端

---

## 📝 替代方案：其他后端平台

### Render.com
- 免费额度充足
- 支持 Java 应用
- 自动 HTTPS

### Fly.io
- 全球分布式部署
- 按使用量付费
- 需要信用卡验证

### 阿里云/腾讯云
- 国内访问速度快
- 需要备案
- 成本较高

---

## 🔍 验证部署

部署完成后测试：

```bash
# 测试后端健康检查
curl https://your-railway-url.app/api/game/health

# 应该返回 JSON 响应
{"status":"UP","timestamp":"..."}
```

---

## 💡 常见问题

**Q: Railway 部署失败？**
A: 检查日志，通常是 Maven 构建问题或端口配置错误

**Q: 前端提示网络错误？**
A: 确保 `VITE_API_BASE_URL` 配置正确，并且后端已启动

**Q: CORS 跨域错误？**
A: 在后端添加 `@CrossOrigin("*")` 注解或使用 CorsFilter
