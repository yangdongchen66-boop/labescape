# 🌐 Railway 域名配置指南 - 快速找到 Domains

## ✅ 当前状态

**部署状态：** ✅ 成功  
**运行时间：** 2026-03-30 14:11:25  
**端口：** 8080  
**健康检查：** http://localhost:8080/api/game/health

---

## 🔍 找不到 Domains？这里有 3 种方法！

### 方法一：使用 CLI（最简单 ⭐）

```bash
cd E:\chatAI\backend
railway domain
```

**输出示例：**
```
✓ https://labescape-production.up.railway.app
```

**复制这个 URL！** 这就是你的后端地址！

---

### 方法二：在 Dashboard 查找（详细步骤）

#### Step 1: 打开项目页面

1. 访问：https://railway.app/dashboard
2. 点击你的 `labescape` 项目卡片
3. 进入项目详情页

#### Step 2: 找到 Settings 标签

在页面**顶部导航栏**，从左到右依次是：
```
Overview | Deployments | Variables | Settings
```

点击 **"Settings"** （最右边）

#### Step 3: 滚动到 Networking 部分

在 Settings 页面，向下滚动，找到 **"Networking"** 区域

#### Step 4: 查看 Domains

在 Networking 下方，你会看到：

```
┌─────────────────────────────────────┐
│ Domains                             │
├─────────────────────────────────────┤
│                                     │
│ 🌐 Auto-Generated Domain            │
│ https://labescape-production...     │
│                      [Generate]     │
│                                     │
│ 🔗 Custom Domain                    │
│ (Not configured)    [Add Domain]    │
│                                     │
└─────────────────────────────────────┘
```

#### Step 5: 生成域名（如果没有）

如果还没有域名，点击 **"Generate"** 按钮

Railway 会自动生成一个类似这样的域名：
```
https://labescape-production.up.railway.app
```

#### Step 6: 复制域名

点击域名右侧的 **复制图标** 📋

---

### 方法三：查看环境变量（备用方案）

Railway 有时会将域名作为环境变量提供：

#### 在 Variables 中查找：

1. 点击 **"Variables"** 标签
2. 查找以下变量：
   ```
   RAILWAY_PUBLIC_DOMAIN
   RAILWAY_PRIVATE_DOMAIN
   ```

3. 如果有，值就是你的域名

---

## 🎯 快速验证域名是否正确

### 方式一：浏览器测试

在浏览器打开：
```
https://your-railway-domain.up.railway.app/api/game/health
```

**预期响应：**
```json
{
  "status": "UP",
  "timestamp": "2026-03-30T14:11:25.889Z"
}
```

### 方式二：命令行测试

```bash
curl https://your-railway-domain.up.railway.app/api/game/health
```

### 方式三：PowerShell 测试

```powershell
Invoke-RestMethod -Uri "https://your-railway-domain.up.railway.app/api/game/health"
```

---

## 📸 图文指引

### Railway Dashboard 布局示意

```
┌─────────────────────────────────────────────────┐
│  LabEscape Backend                       [⚙️]   │
├─────────────────────────────────────────────────┤
│ Overview | Deployments | Variables | Settings  │ ← 点击这里
├─────────────────────────────────────────────────┤
│                                                 │
│ Project Settings                                │
│ ─────────────────                               │
│                                                 │
│ General                                         │
│ • Project Name: labescape                       │
│ • Description: AI 驱动跑团游戏引擎               │
│                                                 │
│ Build                                           │
│ • Root Directory: backend                       │
│ • Builder: DOCKERFILE                           │
│                                                 │
│ Networking  ← 向下滚动到这里                    │
│ ───────────                                     │
│                                                 │
│ Domains                                         │
│ ───────                                         │
│ 🌐 Auto-Generated Domain                        │
│ https://labescape-production.up.railway.app     │ ← 这就是你的域名！
│                                    [Copy]       │
│                                                 │
│ 🔗 Custom Domain                                │
│ Not configured                  [Add Domain]    │
│                                                 │
└─────────────────────────────────────────────────┘
```

---

## 🆘 常见问题

### Q1: Settings 页面只有 "Danger Zone"？

**A:** 你可能在项目概览页，不是项目设置页。

**解决方案：**
1. 确保你点击了项目名称进入详情页
2. 然后点击顶部的 "Settings" 标签

### Q2: 没有 "Networking" 部分？

**A:** Railway 界面可能更新了布局。

**替代方案：**
1. 直接在项目首页查看
2. 或使用 CLI：`railway domain`

### Q3: 生成的域名无法访问？

**A:** 可能需要等待 DNS 生效。

**解决方案：**
1. 等待 2-3 分钟
2. 刷新页面
3. 或重新生成域名

### Q4: 我想使用自定义域名？

**A:** 可以绑定自己的域名！

**步骤：**
1. 点击 "Add Domain"
2. 输入你的域名（如：api.labescape.com）
3. 在域名提供商添加 CNAME 记录
4. SSL 证书会自动签发

---

## 💡 立即执行

### 最快方式（推荐 ⭐）

打开 PowerShell 或 CMD：

```bash
cd E:\chatAI\backend
railway domain
```

**一键获取域名！**

### 手动方式

1. 打开 https://railway.app/dashboard
2. 点击进入 `labescape` 项目
3. 点击顶部 **"Settings"** 标签
4. 向下滚动到 **"Networking"**
5. 复制显示的域名

---

## 🎉 获取域名后的下一步

### Step 1: 复制 Railway URL

示例：
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

在 Vercel 项目页面：
- 点击右上角 **"Redeploy"**
- 确认部署
- 等待 1-2 分钟

### Step 4: 测试游戏

访问：
```
https://frontend-4a29rb70r-yangdongchens-projects.vercel.app
```

**测试清单：**
- [ ] 页面正常加载
- [ ] 能看到开场剧情
- [ ] 行动选项可以点击
- [ ] 骰子动画正常显示
- [ ] HP/MP/金币正确变化

---

## 📞 需要帮助？

如果还是找不到：

1. **截图发给我**
   - Railway Dashboard 页面
   - 我会帮你圈出正确位置

2. **使用 CLI**
   ```bash
   railway domain
   ```

3. **查看项目首页**
   - 有时域名直接显示在项目概览页

---

**现在就去试试 CLI 方式吧！超级简单！** 🚀

```bash
cd E:\chatAI\backend
railway domain
```

一键搞定！✨
