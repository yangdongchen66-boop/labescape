@echo off
echo 🚀 Railway 后端部署助手
echo.
echo 💡 请按以下步骤完成部署：
echo.
echo 第一步：创建 Railway 项目
echo -----------------------------------
echo 1. 访问：https://railway.app/dashboard
echo 2. 点击 "New Project"
echo 3. 选择 "Deploy from GitHub repo"
echo 4. 授权并选择你的仓库
echo.
echo 第二步：配置 Root Directory
echo -----------------------------------
echo Settings → Root Directory: backend
echo.
echo 第三步：添加环境变量
echo -----------------------------------
echo Variables → Add Variable:
echo   DEEPSEEK_API_KEY = your_api_key_here
echo   SERVER_PORT = 8080
echo   JAVA_VERSION = 21
echo.
echo 第四步：开始部署
echo -----------------------------------
echo Railway 会自动检测 Java 项目并开始构建
echo.
echo 第五步：获取域名
echo -----------------------------------
echo Settings → Domains → Generate Domain
echo 复制生成的 URL（类似 https://xxx.up.railway.app）
echo.
echo 第六步：更新 Vercel 前端配置
echo -----------------------------------
echo 访问：https://vercel.com/dashboard
echo 选择 frontend 项目 → Settings → Environment Variables
echo 编辑 VITE_API_BASE_URL = https://your-railway-url.app
echo 保存并重新部署
echo.
echo ========================================
echo ✅ 完成后，在浏览器访问 Vercel 链接测试游戏！
echo ========================================
echo.
pause
