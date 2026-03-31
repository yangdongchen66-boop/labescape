# 🎯 Railway 域名获取 - 三步搞定！

## ✅ 后端已成功运行！

从您的日志看到：
```
✅ Chrono Engine 启动成功！
🔗 健康检查：http://localhost:8080/api/game/health
Tomcat started on port 8080 (http)
```

**太棒了！** 🎉 后端已经在 Railway 上运行了！

---

## 🚀 快速获取域名的三种方法

### 方法一：CLI 命令行（推荐 ⭐）

#### Step 1: 链接项目

在 PowerShell 中执行：
```bash
cd E:\chatAI\backend
railway link
```

**会出现选项：**
```
? Select a workspace  
> old_fish's Projects
  Your Projects
```

**操作：**
- 使用 **↑↓ 方向键** 选择你的项目所在的工作区
- 按 **Enter** 确认

#### Step 2: 选择项目

```
? Select a project  
> labescape
  other-project
```

**操作：**
- 使用 **↑↓ 方向键** 选择 `labescape`
- 按 **Enter** 确认

#### Step 3: 获取域名

```bash
railway domain
```

**输出示例：**
```
✓ https://labescape-production.up.railway.app
```

**完成！** 🎉 复制这个 URL！

---

### 方法二：网页版 Dashboard（可视化）

#### 详细步骤：

1. **打开 Railway Dashboard**
   ```
   https://railway.app/dashboard
   ```

2. **进入项目页面**
   - 找到 `labescape` 项目卡片
   - 点击进入详情页

3. **查看概览页**
   
   在项目首页，通常会直接显示：
   ```
   ┌─────────────────────────────────────┐
   │ Deployments                         │
   ├─────────────────────────────────────┤
   │ ✅ Deployed                         │
   │                                     │
   │ 🌐 https://labescape-production...  │
   │                        [Visit]      │
   │                                     │
   └─────────────────────────────────────┘
   ```

4. **如果首页没有，点击 Settings**
   
   顶部导航栏：
   ```
   Overview | Deployments | Variables | Settings
   ```
   
   点击 **"Settings"** → 向下滚动到 **"Networking"**

5. **复制域名**
   
   在 Domains 区域点击复制按钮

---

### 方法三：直接在浏览器测试

既然应用已经启动，可以直接尝试访问：

#### 尝试这些可能的 URL 格式：

```
https://labescape-production.up.railway.app/api/game/health
https://labescape.onrender.com/api/game/health
https://your-railway-name.railway.app/api/game/health
```

在浏览器打开，看哪个能访问！

---

## 💡 快速验证

获取到 URL 后，立即测试：

### 浏览器测试

打开：
```
https://YOUR-RAILWAY-DOMAIN.up.railway.app/api/game/health
```

**预期看到：**
```json
{
  "status": "UP",
  "timestamp": "2026-03-30T14:11:25.889Z"
}
```

### 命令行测试

```bash
curl https://YOUR-RAILWAY-DOMAIN.up.railway.app/api/game/health
```

---

## 📸 界面参考

### Railway Dashboard 项目首页

```
┌─────────────────────────────────────────────────┐
│  LabEscape Backend                       [⚙️]   │
├─────────────────────────────────────────────────┤
│ Overview | Deployments | Variables | Settings  │
├─────────────────────────────────────────────────┤
│                                                 │
│ 🚀 Deployments                                  │
│ ────────────                                    │
│                                                 │
│ Latest Deployment                               │
│ ┌─────────────────────────────────────────┐    │
│ │ ✅ production                           │    │
│ │                                         │    │
│ │ Status: Active                          │    │
│ │ Domain: labescape-production.up...      │ ← 这里！
│ │                                         │    │
│ │ [View Logs] [Visit Site]                │    │
│ └─────────────────────────────────────────┘    │
│                                                 │
└─────────────────────────────────────────────────┘
```

### Settings → Networking

```
┌─────────────────────────────────────────────────┐
│ Project Settings                                │
├─────────────────────────────────────────────────┤
│                                                 │
│ Networking                                      │
│ ────────────                                    │
│                                                 │
│ Domains                                         │
│ ───────                                         │
│                                                 │
│ 🌐 Auto-Generated Domain                        │
│ ┌─────────────────────────────────────────┐    │
│ │ https://labescape-production.up.rail... │    │
│ │                               [Copy]    │    │
│ └─────────────────────────────────────────┘    │
│                                                 │
│ 🔗 Custom Domain                                │
│ Not configured                  [Add Domain]    │
│                                                 │
└─────────────────────────────────────────────────┘
```

---

## 🎯 现在立即执行

### 方案 A：使用 CLI（最快）

```bash
# 1. 链接项目
cd E:\chatAI\backend
railway link

# 选择工作区（用方向键和 Enter）
# 选择项目（用方向键和 Enter）

# 2. 获取域名
railway domain
```

### 方案 B：网页版（可视化）

1. 打开 https://railway.app/dashboard
2. 点击进入 `labescape` 项目
3. 在首页查找域名，或点击 Settings → Networking
4. 复制显示的域名

---

## 🎉 获取域名后的下一步

### Step 1: 复制 Railway URL

假设你得到的域名是：
```
https://labescape-production.up.railway.app
```

### Step 2: 更新 Vercel 环境变量

1. 访问 https://vercel.com/dashboard
2. 选择 `frontend` 项目
3. **Settings → Environment Variables**
4. 编辑 `VITE_API_BASE_URL`
5. 设置为：
   ```
   https://labescape-production.up.railway.app
   ```
6. 保存

### Step 3: 重新部署前端

在 Vercel 项目页面点击 **"Redeploy"**

### Step 4: 测试游戏

访问：
```
https://frontend-4a29rb70r-yangdongchens-projects.vercel.app
```

---

## ⚠️ 注意事项

### 域名格式可能不同

Railway 有几种域名格式：

**旧格式：**
```
https://labescape-production.up.railway.app
```

**新格式：**
```
https://labescape-production.railway.app
```

**自定义格式：**
```
https://api.labescape.com
```

无论哪种格式，只要能访问就行！

---

## 🆘 还是找不到？

### 终极解决方案

1. **查看部署日志**
   ```bash
   railway logs --lines 50
   ```
   
   日志中可能会显示访问地址

2. **查看项目状态**
   ```bash
   railway status
   ```
   
   会显示所有相关信息

3. **截图求助**
   - 截取 Railway Dashboard 页面
   - 发给我，我会帮你圈出正确位置

---

**现在就试试 CLI 方式吧！** 🚀

```bash
cd E:\chatAI\backend
railway link
# 选择项目后
railway domain
```

一键获取域名！✨
