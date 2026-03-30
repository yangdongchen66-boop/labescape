# 部署指南

## 🚀 快速部署方案（推荐）

### 前端部署到 Vercel

1. **安装 Vercel CLI**
```bash
npm install -g vercel
```

2. **登录 Vercel**
```bash
vercel login
```

3. **部署前端**
```bash
cd frontend
vercel --prod
```

4. **配置环境变量**
   - 在 Vercel 控制台设置：`VITE_API_BASE_URL=https://your-backend-url.railway.app`

### 后端部署到 Railway

1. **创建 Railway 项目**
   - 访问 https://railway.app
   - 新建项目，选择 "Deploy from GitHub repo"
   - 连接你的 GitHub 仓库

2. **配置 Railway**
   - 添加 `backend` 目录作为服务根目录
   - 设置 Java 版本：`JAVA_VERSION=21`
   - 添加 Maven 构建命令：`mvn clean package -DskipTests`
   - 启动命令：`java -jar target/chrono-engine-*.jar`

3. **配置环境变量**
   ```
   DEEPSEEK_API_KEY=your_api_key
   SERVER_PORT=8080
   ```

### 本地构建测试

**前端构建：**
```bash
cd frontend
npm run build
```

**后端打包：**
```bash
cd backend
mvn clean package -DskipTests
```

---

## 📦 离线版本（可选）

如果需要生成可执行文件：

1. **使用 jpackage 打包（Java 14+）**
```bash
jpackage --input backend/target \
         --main-jar chrono-engine-1.0.0-SNAPSHOT.jar \
         --main-class com.chrono.engine.ChronoEngineApplication \
         --name LabEscape \
         --type exe
```

2. **前端打包为桌面应用**
```bash
cd frontend
npm run build
npm install electron
electron-builder
```

---

## ⚙️ 生产环境配置

### 前端环境变量
在 `frontend/.env.production` 中配置：
```env
VITE_API_BASE_URL=https://your-backend-domain.com
```

### 后端生产配置
在 `backend/src/main/resources/application-prod.yml` 中配置：
```yaml
server:
  port: 8080
  
spring:
  datasource:
    url: ${DATABASE_URL}
    
llm:
  api-key: ${DEEPSEEK_API_KEY}
```

---

## 🔍 健康检查

部署后验证服务是否正常：

**后端检查：**
```bash
curl https://your-backend-url.railway.app/api/game/health
```

**前端检查：**
打开浏览器访问 Vercel 提供的域名

---

## 💡 注意事项

1. **API Key 安全**：不要将 DeepSeek API Key 提交到代码库
2. **CORS 配置**：确保后端允许前端域名的跨域请求
3. **数据库**：如果使用持久化存储，需要配置 PostgreSQL 或 MySQL
4. **日志监控**： Railway 和 Vercel 都提供日志查看功能
