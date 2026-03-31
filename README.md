# LabEscape 🎮

> **研究生实习求生游戏** | A TRPG-style game about surviving grad school and securing an internship

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)](https://spring.io/)
[![React](https://img.shields.io/badge/React-18-blue)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.0-blue)](https://www.typescriptlang.org/)
[![DeepSeek](https://img.shields.io/badge/LLM-DeepSeek-orange)](https://deepseek.com/)

## 🌐 在线演示

访问在线演示：**[https://labescape.vercel.app](https://frontend-sigma-gray-ncizc6gc4c.vercel.app?_vercel_share=bf58CSZM4OV7ZJirH0KSArE7tB5nlifa)**

## 🎬 游戏简介

你是一名研二学生，面临找实习和毕业的双重压力。秋招季仅剩 7 天，导师却不放人做实习，要求先完成横向项目。你该如何在有限的时间内，平衡导师关系、准备笔试面试、最终拿到心仪的 Offer？

**LabEscape** 是一款基于大语言模型的 TRPG（桌面角色扮演游戏），融合了：
- 🎲 D&D 风格的检定系统
- 🤖 多智能体 NPC 交互
- 🎯 策略决策与资源管理
- 🎭 丰富的剧情分支和彩蛋结局

## ✨ 核心特性

### 游戏机制
- **时间系统**：7 天倒计时，每天只能进行一次重要行动
- **属性检定**：STR/DEX/CON/INT/WIS/CHA 六维属性，影响不同行动的成功率
- **NPC 记忆**：每个 NPC 都有对玩家的态度值，会影响他们的反应
- **多结局设计**：包含完美结局、偷跑结局、以及 5 种特殊彩蛋结局

### 特殊结局（彩蛋）
| 结局 | 触发条件 | 结果 |
|------|---------|------|
| 🚔 **暴力结局** | 拿枪/拿刀/打人等暴力行为 | 警察到场，被带走 |
| 🚨 **侮辱结局** | 严重辱骂导师 | 被开除学籍 |
| 🧠 **天才说服** | 高情商话术/提供实际利益 | 导师立即同意 |
| 🚪 **退学结局** | 明确表示不读了/退学 | 游戏结束 |
| 💰 **贿赂结局** | 尝试贿赂导师 | 上报学术委员会 |

### 技术架构
```
┌─────────────────────────────────────────────────────────┐
│                      Frontend                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   React 18   │  │   Vite       │  │  Zustand     │  │
│  │   TypeScript │  │   Tailwind   │  │  Framer      │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────┬───────────────────────────────┘
                          │ SSE (Server-Sent Events)
┌─────────────────────────┴───────────────────────────────┐
│                      Backend                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Spring Boot  │  │ Java 21      │  │ DeepSeek API │  │
│  │ Virtual      │  │ Multi-Agent  │  │ LLM Service  │  │
│  │ Threads      │  │ System       │  │              │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
```

## 🚀 快速开始

### 环境要求
- Java 21+
- Node.js 18+
- Maven 3.8+
- DeepSeek API Key

### 1. 克隆项目
```bash
git clone https://github.com/yangdongchen66-boop/labescape.git
cd labescape
```

### 2. 配置后端
```bash
cd backend

# 复制环境变量模板
cp .env.example .env

# 编辑 .env 文件，填入你的 DeepSeek API Key
# DEEPSEEK_API_KEY=your_api_key_here
```

### 3. 启动后端
```bash
mvn spring-boot:run
```
后端服务将在 http://localhost:8080 启动

### 4. 启动前端
```bash
cd ../frontend
npm install
npm run dev
```
前端将在 http://localhost:5173 启动

### 5. 开始游戏
打开浏览器访问 http://localhost:5173，开始你的研究生求生之旅！

## 🎮 游戏玩法

### 基础操作
1. **输入行动**：在底部输入框输入你想做的事情（如"尝试说服导师"、"刷题准备笔试"）
2. **查看状态**：左侧面板显示 HP/MP、导师态度、风险值等关键指标
3. **阅读剧情**：中间区域显示游戏剧情和 NPC 对话
4. **观察系统**：右侧面板显示 AI Agent 的运作日志

### 策略建议
- **准备期**（Day 1-2）：优先刷题和社交，提升准备度
- **突破期**（Day 3-5）：开始投递简历，参加面试
- **收官期**（Day 6-7）：争取 Offer 和导师签字

### 注意事项
- 每次对话消耗 1 天时间
- 导师态度为负时会更严厉
- 风险值过高会触发负面事件
- 保持 HP/MP 健康，避免心态崩溃

## 🏗️ 项目结构

```
labescape/
├── frontend/                 # 前端项目
│   ├── src/
│   │   ├── components/      # React 组件
│   │   │   ├── game/        # 游戏相关组件
│   │   │   ├── layout/      # 布局组件
│   │   │   └── ui/          # UI 组件
│   │   ├── store/           # Zustand 状态管理
│   │   └── App.tsx          # 应用入口
│   └── package.json
│
├── backend/                  # 后端项目
│   ├── src/main/java/
│   │   └── com/chrono/engine/
│   │       ├── controller/  # API 控制器
│   │       ├── service/     # 业务逻辑
│   │       │   └── impl/    # Agent 实现
│   │       ├── domain/      # 领域模型
│   │       └── dto/         # 数据传输对象
│   └── pom.xml
│
└── README.md
```

## 🤖 AI Agent 系统

游戏采用多智能体架构，每个 Agent 负责不同职能：

| Agent | 职责 | 说明 |
|-------|------|------|
| **Manager** | 意图解析 | 分析玩家输入，判断行动类型 |
| **Judge** | 特殊判定 | 检测是否触发彩蛋结局 |
| **Supervisor** | 命运裁判 | 执行 D20 检定 |
| **Executor** | 角色化身 | 生成剧情和 NPC 对话 |
| **Event** | 事件触发 | 处理随机事件 |

## 🛠️ 技术亮点

- **SSE 流式通信**：实现实时剧情推送，减少等待感
- **请求去重机制**：防止 EventSource 重连导致的重复处理
- **NPC 记忆系统**：基于态度值的动态对话生成
- **虚拟线程**：Java 21 Virtual Threads 处理高并发

## 📄 许可证

MIT License

## 🙏 致谢

- [DeepSeek](https://deepseek.com/) - 提供大语言模型支持
- [Spring Boot](https://spring.io/) - 后端框架
- [React](https://react.dev/) - 前端框架
- [Framer Motion](https://www.framer.com/motion/) - 动画库

---

> 💡 **提示**：这是一款实验性的 AI 驱动游戏，剧情由大语言模型实时生成，每次游玩体验都可能不同！
