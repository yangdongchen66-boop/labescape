@echo off
chcp 65001 >nul
echo ========================================
echo 🚀 LabEscape Dockerfile 部署监控助手
echo ========================================
echo.
echo 当前状态：
echo - 代码推送：✅ 已完成 (commit d4d7d94)
echo - GitHub 仓库：https://github.com/yangdongchen66-boop/labescape
echo.
echo 正在检查 Railway 部署状态...
echo.

REM 打开 Railway Dashboard
echo [1/3] 打开 Railway Dashboard...
start https://railway.app/dashboard
echo ✅ 已打开 Railway Dashboard

REM 打开部署状态文档
echo.
echo [2/3] 打开部署状态指南...
start DOCKERFILE_DEPLOYMENT_STATUS.md
echo ✅ 已打开部署状态指南

REM 打开 GitHub 仓库
echo.
echo [3/3] 打开 GitHub 仓库...
start https://github.com/yangdongchen66-boop/labescape
echo ✅ 已打开 GitHub 仓库

echo.
echo ========================================
echo ✅ 所有页面已打开！
echo ========================================
echo.
echo 📊 接下来请执行以下步骤：
echo.
echo 第 1 步：在 Railway Dashboard 查看自动部署
echo    - 应该能看到 "Building..." 或 "Deploying..."
echo    - 点击项目卡片查看详细日志
echo.
echo 第 2 步：耐心等待 Docker 构建完成
echo    - 预计时间：8-10 分钟
echo    - Maven 首次下载依赖需要较长时间
echo.
echo 第 3 步：部署成功后获取 URL
echo    - Settings ^→ Domains ^→ Generate Domain
echo    - 复制生成的 URL
echo.
echo 第 4 步：更新 Vercel 前端配置
echo    - 将 Railway URL 设置到 VITE_API_BASE_URL
echo    - 重新部署前端
echo.
echo ========================================
echo.
echo 💡 提示：可以刷新 Railway Dashboard 查看实时日志
echo.
pause
