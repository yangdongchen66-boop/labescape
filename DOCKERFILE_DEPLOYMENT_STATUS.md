# 🚀 LabEscape Dockerfile 部署实时监控

## ✅ 当前状态

**时间：** 2026-03-30  
**方案：** Dockerfile 多阶段构建  
**推送状态：** ✅ 成功（commit d4d7d94）  
**GitHub 仓库：** https://github.com/yangdongchen66-boop/labescape

---

## ⏱️ 部署时间线

| 时间 | 事件 | 状态 |
|------|------|------|
| T+0s | 代码推送到 GitHub | ✅ 完成 |
| T+30s | Railway 检测到新提交 | ⏳ 等待中 |
| T+1min | 开始 Docker 构建 | ⏳ 等待中 |
| T+2min | Maven 下载依赖 | ⏳ 等待中 |
| T+4min | 编译 Java 代码 | ⏳ 等待中 |
| T+5min | 打包 JAR 文件 | ⏳ 等待中 |
| T+6min | 构建 Docker 镜像 | ⏳ 等待中 |
| T+7min | 启动 Spring Boot 应用 | ⏳ 等待中 |
| T+8min | 健康检查通过 | ⏳ 等待中 |
| **T+8min** | **🎉 部署成功** | ⏳ 等待中 |

**预计总时间：8-10 分钟**

---

## 🔍 实时监控方式

### 方式一：Railway Dashboard（推荐 ⭐）

1. **访问 Dashboard**
   ```
   https://railway.app/dashboard
   ```

2. **找到你的项目**
   - 点击 `labescape` 项目卡片

3. **查看部署状态**
   - 点击 **"Deployments"** 标签
   - 查看最新的部署记录
   - 实时滚动查看日志

4. **关键日志阶段**

   **阶段 1：Docker 构建开始**
   ```
   Building with Docker...
   Step 1/10 : FROM maven:3.9.6-eclipse-temurin-21 AS builder
   ```

   **阶段 2：Maven 下载依赖**
   ```
   RUN mvn dependency:go-offline -B
   Downloading from central: https://repo.maven.apache.org/maven2/...
   ```

   **阶段 3：编译和打包**
   ```
   RUN mvn clean package -DskipTests -B
   [INFO] BUILD SUCCESS
   [INFO] Building jar: /app/target/chrono-engine-*.jar
   ```

   **阶段 4：运行阶段**
   ```
   FROM eclipse-temurin:21-jre-alpine
   COPY --from=builder /app/target/*.jar app.jar
   ```

   **阶段 5：应用启动**
   ```
   Starting ChronoEngineApplication...
   Tomcat started on port 8080 (http)
   Started application in X.XXX seconds
   ```

   **阶段 6：健康检查**
   ```
   ✓ Health check passed
   ✓ Application is ready
   ```

---

### 方式二：使用 Railway CLI

```bash
# 实时查看日志
cd E:\chatAI\backend
railway logs --follow

# 查看最近 100 行日志
railway logs --lines 100

# 查看部署状态
railway status
```

---

## 📊 Dockerfile 构建详解

### Stage 1: Builder（构建器）

```dockerfile
FROM maven:3.9.6-eclipse-temurin-21 AS builder
```

**作用：**
- 使用官方 Maven 镜像
- 包含完整的 JDK 21 和 Maven 3.9.6
- 确保构建环境一致性

**步骤：**
1. 复制 `pom.xml`
2. 下载所有 Maven 依赖（利用缓存）
3. 复制源代码
4. 执行 `mvn clean package -DskipTests`
5. 生成 JAR 文件在 `/app/target/`

**预期输出：**
```
[INFO] BUILD SUCCESS
[INFO] Total time:  45.123 s
[INFO] Finished at: 2026-03-30T21:13:09Z
```

---

### Stage 2: Runtime（运行时）

```dockerfile
FROM eclipse-temurin:21-jre-alpine
```

**作用：**
- 使用轻量级 Alpine Linux
- 只包含 JRE 21（无 JDK，体积更小）
- 最终镜像约 200-250MB

**步骤：**
1. 复制 Stage 1 构建的 JAR
2. 设置 JVM 参数
3. 配置健康检查
4. 启动应用

**优势：**
- ✅ 体积小（相比完整镜像减少 70%）
- ✅ 启动快
- ✅ 安全性高（最小化安装）

---

## 🎯 成功标志

当看到以下日志时，表示部署成功：

```
✅ Step 10/10: ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
✅ Health check configured
✅ Container started successfully
✅ Port 8080 exposed
✅ Application is running
```

在 Railway Dashboard 会显示：
- ✅ 绿色对勾图标
- ✅ "Deployed" 状态
- ✅ 生成的域名 URL

---

## 🌐 获取你的后端 URL

部署成功后，立即获取访问地址：

### 方法一：自动生成域名

1. Railway Dashboard → 你的项目
2. **Settings → Domains**
3. 点击 **"Generate Domain"**
4. 复制生成的 URL

示例：
```
https://labescape-production.up.railway.app
```

### 方法二：使用 CLI

```bash
railway domain
```

输出：
```
✓ https://labescape-production.up.railway.app
```

---

## 🔗 下一步：连接前后端

获得 Railway URL 后，立即执行：

### Step 1: 更新 Vercel 环境变量

1. 访问 https://vercel.com/dashboard
2. 选择 `frontend` 项目
3. **Settings → Environment Variables**
4. 编辑 `VITE_API_BASE_URL`
5. 设置为你的 Railway URL：
   ```
   https://labescape-production.up.railway.app
   ```
6. 点击 **Save**

### Step 2: 重新部署前端

在 Vercel 项目页面：
- 点击右上角 **"Redeploy"**
- 确认部署
- 等待 1-2 分钟

### Step 3: 测试游戏

访问 Vercel URL：
```
https://frontend-4a29rb70r-yangdongchens-projects.vercel.app
```

**测试清单：**
- [ ] 页面正常加载
- [ ] 能看到开场剧情
- [ ] 行动选项可以点击
- [ ] 骰子动画正常显示
- [ ] HP/MP/金币正确变化
- [ ] 濒临崩溃警告触发
- [ ] NPC 态度表情显示

---

## 🚨 故障排查

### Q1: Railway 没有自动开始部署？

**解决方案：**
1. 检查 GitHub webhook 是否配置
2. 手动触发部署：
   - Railway Dashboard → Deployments
   - 点击 **"Redeploy"**

### Q2: Docker 构建失败？

**可能原因：**
- Dockerfile 语法错误
- 基础镜像无法下载
- Maven 仓库连接超时

**解决方案：**
```bash
# 本地测试构建
cd E:\chatAI\backend
docker build -t labescape-backend .
```

### Q3: 构建卡在 Maven 下载？

**解决方案：**
- 耐心等待（首次构建需要 5-8 分钟）
- 检查 Railway 日志是否有网络错误
- 考虑使用国内 Maven 镜像（如需要）

### Q4: 端口绑定失败？

**检查环境变量：**
```
SERVER_PORT=8080
```

确保已在 Railway Variables 中配置。

---

## 📈 性能优化建议

### 1. Docker 层缓存

Dockerfile 已优化：
```dockerfile
# 先复制 pom.xml（变化少）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 再复制源代码（变化多）
COPY src ./src
RUN mvn clean package -DskipTests -B
```

**好处：**
- 如果只改了代码，依赖下载会使用缓存
- 构建速度提升 50%

### 2. 多阶段构建

**优势：**
- 最终镜像只包含运行时必要文件
- 体积从 ~800MB 减小到 ~250MB
- 启动速度更快

### 3. 健康检查

```dockerfile
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget --no-verbose --tries=1 --spider \
    http://localhost:8080/api/game/health || exit 1
```

**好处：**
- Railway 会自动检测应用是否健康
- 失败时自动重启容器

---

## 💡 环境变量检查清单

部署前请确认 Railway Variables 已添加：

| 变量名 | 值 | 必填 |
|--------|-----|------|
| `DEEPSEEK_API_KEY` | `sk_xxxxxxxxxxxx` | ✅ |
| `SERVER_PORT` | `8080` | ✅ |
| `JAVA_VERSION` | `21` | ✅ |
| `MAVEN_OPTS` | `-Xmx2g -XX:MaxMetaspaceSize=512m` | 可选 |

---

## 🎉 完整部署流程图

```
┌─────────────┐
│  Git Push   │ ✅ 已完成
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│ GitHub 接收提交 │ ✅ 已完成
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│ Railway 检测到   │ ⏳ 进行中...
│ 新的提交        │
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│ 自动触发部署    │ ⏳ 等待中
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│ Docker 构建     │ ⏳ 等待中
│ - Stage 1:      │
│   Maven 构建    │
│ - Stage 2:      │
│   JRE 运行      │
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│ 启动 Spring     │ ⏳ 等待中
│ Boot 应用       │
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│ 健康检查通过    │ ⏳ 等待中
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│ 🎉 部署成功！   │
│ 生成访问 URL    │
└─────────────────┘
```

---

## 🆘 需要帮助？

如果遇到任何问题：

1. 📖 查看 Railway 文档：https://docs.railway.app
2. 💬 查看完整部署日志
3. 🔍 搜索错误信息
4. 📧 联系 Railway 支持

---

## 📱 快速链接

- [Railway Dashboard](https://railway.app/dashboard)
- [查看部署日志](https://railway.app/dashboard → 项目 → Deployments)
- [GitHub 仓库](https://github.com/yangdongchen66-boop/labescape)
- [Vercel 前端](https://frontend-4a29rb70r-yangdongchens-projects.vercel.app)

---

**保持耐心，Docker 构建正在进行中！** 🚀

预计还需等待：**5-8 分钟**

刷新 Railway Dashboard 查看实时进度！☕
