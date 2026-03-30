# 后端部署指南

## 🚀 Railway 一键部署

### 方法一：使用 Railway CLI（推荐）

#### 1. 安装 Railway CLI
```bash
npm install -g @railway/cli
```

#### 2. 登录 Railway
```bash
railway login
```

#### 3. 初始化项目
```bash
cd backend
railway init
```

#### 4. 创建新项目
- 选择 "Create new project"
- 项目名称：`labescape-backend`
- 环境：`production`

#### 5. 配置环境变量
```bash
railway variables set DEEPSEEK_API_KEY=your_api_key_here
railway variables set SERVER_PORT=8080
railway variables set JAVA_VERSION=21
```

#### 6. 部署
```bash
railway up --detach
```

#### 7. 查看部署状态
```bash
railway logs
railway status
```

#### 8. 获取域名
```bash
railway domain
```

会返回类似：
```
https://labescape-backend-production.up.railway.app
```

---

### 方法二：通过 GitHub 自动部署

#### 1. 推送到 GitHub
```bash
# 初始化 git（如果还没有）
git init
git add .
git commit -m "Initial commit: LabEscape game with enhanced features"

# 添加远程仓库（替换为您的 GitHub 用户名和仓库名）
git remote add origin https://github.com/YOUR_USERNAME/labescape.git
git push -u origin main
```

#### 2. 连接 Railway
1. 访问 https://railway.app/dashboard
2. 点击 "New Project"
3. 选择 "Deploy from GitHub repo"
4. 授权 Railway 访问你的仓库
5. 选择 `labescape` 仓库

#### 3. 配置 Root Directory
在 Railway 控制台：
- Settings → Root Directory: `backend`

#### 4. 添加环境变量
Variables → Add Variable：
```
DEEPSEEK_API_KEY=sk_xxxxxxxxxxxxxxxxxxxx
SERVER_PORT=8080
JAVA_VERSION=21
```

#### 5. 自动部署
Railway 会自动检测 Java 项目并开始构建部署。

---

## 🔍 验证部署

### 健康检查
```bash
curl https://your-railway-url.app/api/game/health
```

预期响应：
```json
{
  "status": "UP",
  "timestamp": "2026-03-30T13:13:09.783Z"
}
```

### 测试游戏接口
```bash
curl -X POST https://your-railway-url.app/api/game/action \
  -H "Content-Type: application/json" \
  -d '{"sessionId":"test","playerInput":"你好"}'
```

---

## ⚙️ 更新 Vercel 前端配置

获得 Railway URL 后，更新 Vercel 环境变量：

### 方法一：Vercel 控制台
1. 访问 https://vercel.com/dashboard
2. 选择 `frontend` 项目
3. Settings → Environment Variables
4. 编辑 `VITE_API_BASE_URL`
5. 设置为：`https://your-railway-url.app`
6. 保存并重新部署

### 方法二：Vercel CLI
```bash
cd frontend
vercel env add VITE_API_BASE_URL https://your-railway-url.app production
vercel --prod
```

---

## 💡 常见问题

### Q1: Railway 部署失败？
**解决：**
```bash
# 查看详细日志
railway logs --all

# 本地测试构建
railway run mvn clean package
```

### Q2: 端口绑定错误？
确保 `application.yml` 中使用环境变量：
```yaml
server:
  port: ${SERVER_PORT:8080}
```

### Q3: CORS 跨域问题？
在后端添加 CORS 配置，参考 `CorsConfig.java`

### Q4: API Key 安全？
- ✅ 不要将 API Key 提交到 Git
- ✅ 使用 Railway 的环境变量
- ✅ 定期检查密钥轮换

---

## 📊 监控和日志

### Railway 日志
```bash
# 实时日志
railway logs --follow

# 最近 100 条日志
railway logs --lines 100
```

### 性能监控
- Railway Dashboard → Metrics
- 查看 CPU、内存使用情况
- 设置告警阈值

---

## 🎯 下一步

1. ✅ 完成 Railway 部署
2. ✅ 配置 VITE_API_BASE_URL
3. ✅ 测试端到端功能
4. 🎉 开始游戏！
