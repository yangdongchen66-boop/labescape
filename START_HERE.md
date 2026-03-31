# 🎯 LabEscape 部署 - 3 分钟快速开始

## ✅ 当前状态（已完成）

- ✨ 游戏升级：8 个新功能全部就绪
- 📦 代码提交：commit 922661a
- 🌐 前端部署：Vercel 已上线
- 🔧 配置就绪：Railway 配置文件完成
- 📚 文档齐全：多个部署指南可用

---

## 🚀 立即部署（只需 3 步）

### 第 1 步：推送代码到 GitHub（2 分钟）

**最简单方式**：运行脚本
```bash
双击运行：push-to-github.bat
```

或使用 **GitHub Desktop**：
1. 打开 GitHub Desktop
2. File → Add Local Repository → `E:\chatAI`
3. 点击 "Push origin"

---

### 第 2 步：连接 Railway（3 分钟）

**交互式助手**：
```bash
双击运行：deploy-wizard.html
```

或手动操作：
1. 访问 https://railway.app/dashboard
2. New Project → Deploy from GitHub repo
3. 选择 `labescape` 仓库
4. Settings → Root Directory: `backend`
5. Variables 添加：
   - `DEEPSEEK_API_KEY` = your_key
   - `SERVER_PORT` = 8080
   - `JAVA_VERSION` = 21
6. 等待部署，获取生成的 URL

---

### 第 3 步：更新 Vercel（1 分钟）

1. 访问 https://vercel.com/dashboard
2. 选择 `frontend` 项目
3. Settings → Environment Variables
4. 编辑 `VITE_API_BASE_URL` = Railway URL
5. Redeploy

---

## 🎉 完成！

访问游戏：
```
https://frontend-4a29rb70r-yangdongchens-projects.vercel.app
```

---

## 📱 快速参考

### 核心文件
| 文件 | 用途 |
|------|------|
| `push-to-github.bat` | 一键推送到 GitHub |
| `deploy-wizard.html` | 交互式部署向导 ⭐ |
| `AUTO_DEPLOY.md` | 详细自动部署指南 |
| `QUICK_START.md` | 3 步快速指南 |

### 重要链接
- GitHub 仓库：https://github.com/yangdongchen66-boop/labescape
- Railway Dashboard：https://railway.app/dashboard
- Vercel Dashboard：https://vercel.com/dashboard
- 前端预览：https://frontend-4a29rb70r-yangdongchens-projects.vercel.app

### 环境变量
**Railway（后端）**：
```env
DEEPSEEK_API_KEY=sk_xxxxxxxxxxxx
SERVER_PORT=8080
JAVA_VERSION=21
```

**Vercel（前端）**：
```env
VITE_API_BASE_URL=https://your-app.up.railway.app
```

---

## 💡 遇到问题？

### 网络错误无法推送
✅ 使用 GitHub Desktop  
✅ 直接访问 GitHub 网页上传

### Railway 部署失败
✅ 检查日志：Railway Dashboard → Logs  
✅ 确认 Root Directory = `backend`  
✅ 验证 API Key 正确

### 前端网络错误
✅ 确认 Railway 部署成功  
✅ 检查 VITE_API_BASE_URL 配置  
✅ 重新部署前端

---

## 🆘 获取帮助

1. 查看 `deploy-wizard.html` 交互式指南
2. 阅读 `AUTO_DEPLOY.md` 详细步骤
3. 查看 Railway/Vercel 日志

---

**准备好了吗？开始部署吧！** 🚀

**预计总时间：5-10 分钟**
