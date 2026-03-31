# 🚀 LabEscape 一键部署完成指南

## ✅ 当前状态

### 已完成的工作
- ✨ **游戏升级**：8 个新功能全部实现并测试通过
- 📦 **代码提交**：所有更改已 commit（commit ID: 922661a）
- 🌐 **前端部署**：已成功部署到 Vercel
  - 🔗 访问地址：https://frontend-4a29rb70r-yangdongchens-projects.vercel.app
- 🔧 **配置就绪**：Railway 和 Nixpacks 配置文件已创建

---

## 🎯 接下来只需 3 步

### 第 1 步：推送到 GitHub ⏫

```bash
cd e:\chatAI
git push origin main
```

**如果没有 GitHub 仓库**，请先创建：
1. 访问 https://github.com/new
2. 仓库名：`labescape`
3. 选择 **Public**
4. 点击 "Create repository"
5. 然后执行推送命令

---

### 第 2 步：部署到 Railway 🚂

#### 选项 A：GitHub 自动部署（最简单）

1. **访问 Railway**
   - 打开：https://railway.app/dashboard
   
2. **创建项目**
   - 点击 **"New Project"**
   - 选择 **"Deploy from GitHub repo"**
   - 授权 Railway 访问你的 GitHub
   - 选择 `labescape` 仓库

3. **配置项目**
   - **Settings → Root Directory**: 输入 `backend`
   
4. **添加环境变量**
   - 点击 **"Variables"**
   - 添加以下变量：
     ```
     DEEPSEEK_API_KEY=sk_xxxxxxxxxxxxxxxxxxxx
     SERVER_PORT=8080
     JAVA_VERSION=21
     ```

5. **开始部署**
   - Railway 会自动检测并开始构建
   - 等待 3-5 分钟

6. **获取域名**
   - **Settings → Domains → Generate Domain**
   - 复制生成的 URL（类似：`https://labescape-production.up.railway.app`）

#### 选项 B：CLI 部署（高级）

```bash
cd backend
railway init
railway variables set DEEPSEEK_API_KEY=your_key
railway up --detach
railway domain
```

---

### 第 3 步：连接前后端 🔗

1. **更新 Vercel 环境变量**
   - 访问：https://vercel.com/dashboard
   - 选择 `frontend` 项目
   - **Settings → Environment Variables**
   - 编辑 `VITE_API_BASE_URL`
   - 设置为你的 Railway URL：
     ```
     https://labescape-production.up.railway.app
     ```
   - 保存

2. **重新部署前端**
   ```bash
   cd frontend
   vercel deploy --prod --yes
   ```

---

## 🎉 完成！

现在可以访问 Vercel URL 开始游戏了！

**测试清单**：
- [ ] 前端页面能正常加载
- [ ] 能看到游戏开场剧情
- [ ] 行动选项可以点击
- [ ] 骰子动画正常显示
- [ ] HP/MP/金币变化正确
- [ ] 濒临崩溃警告触发
- [ ] NPC 态度表情显示

---

## 📱 分享你的游戏

部署成功后，你可以：
- 📤 将 Vercel URL 分享给朋友
- 🌟 在社交媒体展示你的作品
- 💼 作为全栈开发案例加入作品集

---

## 💡 快速故障排查

### ❌ 前端提示网络错误
**原因**：后端未部署或 URL 配置错误  
**解决**：
1. 检查 Railway 项目是否部署成功
2. 确认 `VITE_API_BASE_URL` 配置正确
3. 等待 Railway 构建完成

### ❌ Railway 部署失败
**查看日志**：
```bash
railway logs --follow
```

**常见原因**：
- API Key 无效 → 检查 DeepSeek API Key
- Maven 构建错误 → 检查 `pom.xml`
- 端口冲突 → 确认使用 `SERVER_PORT` 环境变量

### ❌ CORS 跨域错误
在后端添加 CORS 配置（如需要）。

---

## 📚 更多资源

- 📄 完整部署文档：`README_DEPLOYMENT.md`
- 📄 后端详细指南：`BACKEND_DEPLOY.md`
- 📄 前端配置：`DEPLOYMENT.md`

---

## 🆘 需要帮助？

如果遇到任何问题：
1. 查看 Railway/Vercel 控制台的日志
2. 检查环境变量配置
3. 确认 API Key 有效且未过期
4. 重启服务（Railway 会自动重启）

---

**准备好了吗？让我们开始部署吧！🚀**

运行这个命令开始：
```bash
git push origin main
```
