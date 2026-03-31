@echo off
chcp 65001 >nul
echo ========================================
echo 🚀 LabEscape GitHub 推送助手
echo ========================================
echo.
echo 正在推送代码到 GitHub...
echo 仓库：https://github.com/yangdongchen66-boop/labescape.git
echo.

cd /d %~dp0

REM 设置 Git 配置
git config --global http.postBuffer 524288000
git config --global https.postBuffer 524288000

echo ✅ Git 配置已优化

REM 尝试推送
echo.
echo 📤 开始推送...
echo.

git push origin master

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo ✅ 推送成功！
    echo ========================================
    echo.
    echo 下一步：
    echo 1. 访问 https://railway.app/dashboard
    echo 2. 创建新项目并连接 labescape 仓库
    echo 3. 按照 deploy-wizard.html 中的步骤操作
    echo.
    pause
) else (
    echo.
    echo ========================================
    echo ❌ 推送失败
    echo ========================================
    echo.
    echo 可能的原因：
    echo 1. 网络连接问题
    echo 2. GitHub 账号未授权
    echo 3. 仓库地址不正确
    echo.
    echo 解决方案：
    echo ✅ 使用 GitHub Desktop（推荐）
    echo ✅ 使用网页版上传文件
    echo ✅ 检查网络连接后重试
    echo.
    echo 查看详细指南：deploy-wizard.html
    echo.
    pause
)
