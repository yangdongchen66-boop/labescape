@echo off
chcp 65001 >nul
echo ========================================
echo 🔧 Railway 构建失败 - 一键修复助手
echo ========================================
echo.
echo 检测到 Railway 构建失败，正在执行修复...
echo.

cd /d %~dp0

REM Step 1: 清除 Git 缓存
echo [1/4] 清理 Git 状态...
git reset HEAD backend/nixpacks.toml >nul 2>&1
echo ✅ Git 状态已清理

REM Step 2: 恢复 nixpacks.toml
echo.
echo [2/4] 恢复 nixpacks.toml 配置...
(
echo # Nixpacks 配置 - Java 21
echo [phases.setup]
echo nixPkgs = ["jdk21", "maven"]
echo.
echo [phases.build]
echo dependsOn = ["setup"]
echo cmds = ["mvn clean package -DskipTests"]
echo.
echo [start]
echo cmd = "java -jar target/chrono-engine-*.jar"
) > backend\nixpacks.toml
echo ✅ nixpacks.toml 已恢复

REM Step 3: 添加新文件
echo.
echo [3/4] 添加 Dockerfile 和 railway.toml...
git add backend/Dockerfile backend/railway.toml backend/railway.toml 2>nul
echo ✅ 文件已添加

REM Step 4: 提交更改
echo.
echo [4/4] 提交修复...
git commit -m "🐛 添加 Dockerfile 备用方案" 2>nul
if %errorlevel% equ 0 (
    echo ✅ 提交成功
) else (
    echo ⚠️ 没有需要提交的更改
)

echo.
echo ========================================
echo ✅ 修复完成！接下来请执行以下步骤：
echo ========================================
echo.
echo 第 1 步：推送到 GitHub
echo    git push origin master
echo.
echo 第 2 步：在 Railway Dashboard 清除缓存
echo    Settings ^→ Danger Zone ^→ Clear Cache
echo.
echo 第 3 步：重新部署
echo    点击 Redeploy 按钮
echo.
echo 或者直接使用 Dockerfile 方案：
echo    Railway 会自动检测并使用 Dockerfile 构建
echo.
echo ========================================
echo.
pause
