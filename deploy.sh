#!/bin/bash

echo "🚀 开始部署 LabEscape 到 Vercel..."

# 检查是否登录
echo "📝 检查 Vercel 登录状态..."
vercel whoami

if [ $? -ne 0 ]; then
    echo "❌ 未登录，请先登录 Vercel"
    vercel login
fi

# 进入前端目录
cd frontend

# 构建项目
echo "🔨 构建前端项目..."
npm run build

if [ $? -ne 0 ]; then
    echo "❌ 构建失败！"
    exit 1
fi

echo "✅ 构建完成！"

# 部署到 Vercel
echo "🌐 开始部署到 Vercel..."
echo ""
echo "💡 提示："
echo "   - 首次部署会自动创建新项目"
echo "   - 按 Enter 确认所有默认选项"
echo "   - 部署完成后会提供访问链接"
echo ""

# 生产环境部署
vercel --prod

echo ""
echo "🎉 部署完成！"
echo ""
echo "⚠️  重要提醒："
echo "   1. 在 Vercel 控制台配置环境变量：VITE_API_BASE_URL"
echo "   2. 后端需要单独部署到 Railway 或其他平台"
echo "   3. 查看 DEPLOYMENT.md 获取完整部署指南"
echo ""
