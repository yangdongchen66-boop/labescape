# 🎛️ Railway 环境变量配置指南

## 📋 需要添加的环境变量

在 Railway 部署后端之前，需要添加以下 3 个环境变量：

| 变量名 | 值示例 | 说明 | 必填 |
|--------|---------|------|------|
| `DEEPSEEK_API_KEY` | `sk_xxxxxxxxxxxxxxxxxxxx` | DeepSeek AI API 密钥 | ✅ 必填 |
| `SERVER_PORT` | `8080` | 服务器端口 | ✅ 必填 |
| `JAVA_VERSION` | `21` | Java 版本 | ✅ 必填 |

---

## 🔧 方法一：Railway Dashboard（推荐）

### Step 1: 打开项目设置

1. 访问 https://railway.app/dashboard
2. 点击你的项目卡片（`labescape`）
3. 进入项目详情页

### Step 2: 找到 Variables 标签

在项目详情页顶部导航栏：
- 点击 **"Variables"** 标签

### Step 3: 添加第一个变量

1. 点击 **"New Variable"** 按钮
2. 填写表单：
   ```
   Key: DEEPSEEK_API_KEY
   Value: sk_xxxxxxxxxxxxxxxxxxxx (替换为你的真实 API Key)
   ```
3. 点击 **"Add Variable"** 保存

### Step 4: 添加第二个变量

重复 Step 3：
```
Key: SERVER_PORT
Value: 8080
```

### Step 5: 添加第三个变量

重复 Step 3：
```
Key: JAVA_VERSION
Value: 21
```

### ✅ 完成后的效果

你应该看到 3 个变量列表：
```
┌─────────────────────┬──────────────────────────┐
│ Name                │ Value                    │
├─────────────────────┼──────────────────────────┤
│ DEEPSEEK_API_KEY    │ sk_xxxxxxxxx...          │
│ SERVER_PORT         │ 8080                     │
│ JAVA_VERSION        │ 21                       │
└─────────────────────┴──────────────────────────┘
```

---

## 🚀 方法二：使用 Railway CLI

### 安装 CLI（如果还没有）
```bash
npm install -g @railway/cli
```

### 登录 Railway
```bash
railway login
```

### 进入项目目录
```bash
cd E:\chatAI\backend
```

### 初始化项目（首次使用）
```bash
railway init
```

### 逐个添加变量

#### 1. 添加 DeepSeek API Key
```bash
railway variables set DEEPSEEK_API_KEY=sk_xxxxxxxxxxxx
```

#### 2. 添加服务器端口
```bash
railway variables set SERVER_PORT=8080
```

#### 3. 添加 Java 版本
```bash
railway variables set JAVA_VERSION=21
```

### 验证变量已添加
```bash
railway variables list
```

应该显示：
```
✓ DEEPSEEK_API_KEY = sk_xxxxxxxxxxxx
✓ SERVER_PORT = 8080
✓ JAVA_VERSION = 21
```

---

## 📸 图文步骤说明

### Dashboard 界面示意

```
┌─────────────────────────────────────────────────┐
│  LabEscape Backend                      [⚙️]    │
├─────────────────────────────────────────────────┤
│  Overview | Deployments | Variables | Settings  │
├─────────────────────────────────────────────────┤
│                                                 │
│  Variables                                      │
│                                                 │
│  + New Variable                                 │
│                                                 │
│  ┌────────────────────────────────────────┐    │
│  │ Name          │ Value           │ ⋮    │    │
│  ├────────────────────────────────────────┤    │
│  │ DEEPSEEK_...  │ sk_xxxxx...     │ ⋮    │    │
│  │ SERVER_PORT   │ 8080            │ ⋮    │    │
│  │ JAVA_VERSION  │ 21              │ ⋮    │    │
│  └────────────────────────────────────────┘    │
│                                                 │
└─────────────────────────────────────────────────┘
```

### 添加变量弹窗

点击 "New Variable" 后会出现：

```
┌─────────────────────────────────────┐
│  Add Variable                       │
├─────────────────────────────────────┤
│                                     │
│  Key                                │
│  ┌─────────────────────────────┐   │
│  │ DEEPSEEK_API_KEY            │   │
│  └─────────────────────────────┘   │
│                                     │
│  Value                              │
│  ┌─────────────────────────────┐   │
│  │ sk_xxxxxxxxxxxxxxxxxxxx     │   │
│  └─────────────────────────────┘   │
│                                     │
│  [Cancel]          [Add Variable]   │
└─────────────────────────────────────┘
```

---

## ⚠️ 注意事项

### 1. API Key 安全
- ❌ **不要**将 API Key 提交到 Git
- ✅ **使用** Railway 的环境变量功能
- ✅ **定期检查**密钥轮换
- ✅ **设置**变量为私有（Private）

### 2. 变量命名规则
- 只能包含大写字母、数字和下划线
- 不能以数字开头
- 不能包含空格或特殊字符

### 3. 变量作用域
- **Production** 环境：正式部署使用
- **Preview** 环境：预览部署使用
- 确保添加到正确的环境！

### 4. 立即生效
- 添加/修改变量后，Railway 会**自动重新部署**
- 等待 1-2 分钟让新配置生效
- 可以在 Deployments 标签查看部署状态

---

## 🔍 验证环境变量

### 方法一：查看部署日志

1. 在 Railway Dashboard 点击 **"Deployments"**
2. 点击最新的部署记录
3. 查看日志输出
4. 搜索 "Environment" 相关日志

### 方法二：通过健康检查接口

部署完成后测试：
```bash
curl https://your-railway-url.app/api/game/health
```

如果环境变量正确，应该返回：
```json
{
  "status": "UP",
  "timestamp": "2026-03-30T13:13:09.783Z"
}
```

### 方法三：在代码中打印（调试用）

在启动日志中应该能看到：
```
[INFO] Using DeepSeek API Key: sk_xxxxx***
[INFO] Server Port: 8080
[INFO] Java Version: 21
```

---

## 💡 常见问题

### Q1: 找不到 Variables 标签？
**A:** 确保你已经：
1. 创建了项目
2. 连接了 GitHub 仓库
3. 进入了正确的环境（Production/Preview）

### Q2: 变量添加后不生效？
**A:** 
1. 检查是否拼写错误
2. 确认添加了正确的环境
3. 等待自动重新部署完成
4. 查看部署日志是否有错误

### Q3: 如何修改变量？
**A:**
1. 在 Variables 列表找到该变量
2. 点击右侧的 **⋮** 菜单
3. 选择 "Edit"
4. 修改后保存
5. Railway 会自动重新部署

### Q4: 如何删除变量？
**A:**
1. 点击变量右侧的 **⋮** 菜单
2. 选择 "Delete"
3. 确认删除

### Q5: 可以批量导入变量吗？
**A:** 
Railway 目前不支持批量导入，但可以：
- 使用 CLI 脚本批量设置
- 或者手动逐个添加（只有 3 个，很快）

---

## 🎯 下一步

添加完环境变量后：

1. ✅ Railway 会自动开始部署
2. ⏱️ 等待 3-5 分钟
3. 🌐 部署成功后，获取域名 URL
4. 🔗 将该 URL 配置到 Vercel 的 `VITE_API_BASE_URL`

---

## 📞 需要帮助？

如果遇到任何问题：
- 📖 查看 Railway 官方文档：https://docs.railway.app/guides/variables
- 💬 查看部署日志
- 🔍 搜索错误信息

---

**准备好了吗？现在就去 Railway Dashboard 添加变量吧！** 🚀

快速链接：https://railway.app/dashboard → 选择项目 → Variables → New Variable
