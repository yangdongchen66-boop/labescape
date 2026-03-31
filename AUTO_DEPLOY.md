# 🚀 LabEscape 自动部署指南

## ✅ 代码已准备就绪

所有代码已经提交（commit ID: 922661a），现在需要推送到 GitHub。

---

## 📤 第一步：推送代码到 GitHub

### 方法 A：使用 Git GUI（推荐）

1. **打开 GitHub Desktop 或 SourceTree**
2. **添加仓库**
   - 选择 `E:\chatAI` 目录
3. **点击 "Push"**
   - 远程仓库：`https://github.com/yangdongchen66-boop/labescape.git`
   - 分支：`master`

### 方法 B：命令行（如果网络正常）

```bash
cd E:\chatAI
git remote set-url origin https://github.com/yangdongchen66-boop/labescape.git
git push origin master
```

### 方法 C：直接上传（备选方案）

1. 访问 https://github.com/yangdongchen66-boop/labescape
2. 点击 "uploading an existing file"
3. 拖拽整个项目文件夹
4. 点击 "Commit changes"

---

## 🔗 第二步：连接 Railway

### 1. 访问 Railway
打开：https://railway.app/dashboard

### 2. 创建新项目
- 点击 **"New Project"**
- 选择 **"Deploy from GitHub repo"**
- 如果未授权，点击 **"Authorize with GitHub"**
- 找到 `labescape` 仓库并选择

### 3. 配置 Root Directory
在 Railway 控制台：
- 点击项目进入详情
- **Settings → Root Directory**
- 输入：`backend`
- 点击 **Save**

### 4. 添加环境变量
在 Railway 控制台的 **Variables** 标签页：

点击 **Add Variable**，依次添加：

| 变量名 | 值 |
|--------|-----|
| `DEEPSEEK_API_KEY` | `sk_xxxxxxxxxxxxxxxxxxxx` (替换为你的 API Key) |
| `SERVER_PORT` | `8080` |
| `JAVA_VERSION` | `21` |

### 5. 开始部署
Railway 会自动检测 Java 项目并开始构建，过程约 3-5 分钟。

查看部署进度：
- 点击项目卡片
- 查看实时日志
- 等待状态变为 "Deployed"

### 6. 获取域名
部署成功后：
- **Settings → Domains**
- 点击 **"Generate Domain"**
- 复制生成的 URL（类似：`https://labescape-production.up.railway.app`）

---

## 🌐 第三步：更新 Vercel 前端配置

### 1. 访问 Vercel 控制台
打开：https://vercel.com/dashboard

### 2. 选择 frontend 项目
找到之前部署的 `frontend` 项目

### 3. 配置环境变量
- **Settings → Environment Variables**
- 找到 `VITE_API_BASE_URL`
- 点击编辑
- 设置为你的 Railway URL：
  ```
  https://labescape-production.up.railway.app
  ```
- 点击 **Save**

### 4. 重新部署前端
- 返回 **Overview** 标签页
- 点击右上角 **"Redeploy"**
- 确认重新部署
- 等待部署完成（约 1-2 分钟）

---

## 🎉 完成！测试游戏

### 访问游戏
打开浏览器访问 Vercel URL：
```
https://frontend-4a29rb70r-yangdongchens-projects.vercel.app
```

### 测试清单
- [ ] 页面正常加载
- [ ] 能看到开场剧情
- [ ] HP/MP/金币显示正常
- [ ] 点击行动选项能触发骰子动画
- [ ] 濒临崩溃时出现红色边框警告
- [ ] NPC 对话显示态度表情

---

## 🔍 验证后端健康检查

在浏览器或终端测试：
```bash
curl https://labescape-production.up.railway.app/api/game/health
```

预期响应：
```json
{
  "status": "UP",
  "timestamp": "2026-03-30T13:13:09.783Z"
}
```

---

## 💡 常见问题

### Q1: Railway 部署失败？
**解决方案：**
1. 查看 Railway 日志：点击项目 → Logs
2. 检查环境变量是否正确
3. 确认 Root Directory 设置为 `backend`
4. 如果是 Maven 错误，尝试重新部署

### Q2: 前端提示"Network Error"？
**解决方案：**
1. 确认 Railway 部署成功
2. 检查 `VITE_API_BASE_URL` 是否配置正确
3. 等待几分钟让环境变量生效
4. 重新部署前端

### Q3: GitHub 推送失败？
**解决方案：**
1. 检查网络连接
2. 使用 GitHub Desktop 等 GUI 工具
3. 检查 SSH key 配置
4. 使用 HTTPS 而非 SSH

### Q4: CORS 跨域错误？
**解决方案：**
在后端添加 CORS 配置（如需要）：
```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
            }
        };
    }
}
```

---

## 📊 监控和维护

### Railway 日志
```bash
# 如果使用 CLI
railway logs --follow
```

或在 Railway 控制台查看实时日志

### Vercel 部署日志
- 访问 Vercel Dashboard
- 选择项目
- 点击 "Deployments" 查看历史

### 性能监控
- Railway Dashboard → Metrics
- 查看 CPU、内存使用
- 设置告警阈值

---

## 🎯 下一步优化建议

### 1. 数据库持久化
当前使用内存存储，重启后数据丢失。

**建议添加**：
- PostgreSQL（Railway 一键安装）
- Redis（缓存会话）

### 2. CDN 加速
- 使用 Cloudflare CDN
- 静态资源优化

### 3. 监控告警
- Sentry（错误追踪）
- Uptime Robot（可用性监控）

### 4. 自定义域名
- 购买域名
- 配置 DNS
- SSL 证书自动续签

---

## 📱 分享你的作品

部署成功后：
- 📤 将 URL 分享给朋友试玩
- 🌟 在社交媒体展示
- 💼 加入作品集作为全栈案例
- 📝 写技术博客分享经验

---

**祝您部署顺利！🚀**

如有任何问题，请查看日志或联系支持。
