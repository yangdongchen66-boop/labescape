import { create } from 'zustand';

/**
 * Chrono-Agents: 研二求生指南
 * V5: 强制调用后端 DeepSeek API
 * 
 * @author Chrono Engine Team
 * @version 5.0.0
 */

// ===== 类型定义 =====
export type ChatRole = 'dm' | 'npc' | 'player' | 'system';
export type GameState = 'PLAYING' | 'VICTORY' | 'GAME_OVER';

export interface ChatMessage {
  id: string;
  role: ChatRole;
  text: string;
  timestamp: string;
  name?: string;
  hooks?: ActionHook[];
  isStreaming?: boolean; // 是否正在流式输出
}

export interface ActionHook {
  id: string;
  text: string;
  attr?: 'str' | 'dex' | 'int' | 'con' | 'wis' | 'cha';
  dc?: number;
  hpCost?: number;
  mpCost?: number;
  goldCost?: number;
  isRest?: boolean;
  // 后果预览
  successReward?: string;  // 成功后果描述
  failPenalty?: string;    // 失败后果描述
}

export interface AgentLog {
  id: string;
  agentName: string;
  action: string;
  status: 'pending' | 'success' | 'error';
  time: string;
}

// ===== 工具函数 =====
const createId = (prefix: string) => `${prefix}-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`;
const nowIso = () => new Date().toISOString();

// ===== 模块级状态（防止重复提交）=====
let isProcessingAction = false;
let actionCallCount = 0;
let streamingCallCount = 0;

// ===== 意图识别 =====
const INTENT_PATTERNS: { pattern: RegExp; attr: 'str' | 'dex' | 'int' | 'con' | 'wis' | 'cha'; baseDC: number }[] = [
  { pattern: /说服|谈判|沟通|商量|求|讲理|解释|忽悠|谈|劝/i, attr: 'cha', baseDC: 12 },
  { pattern: /硬刚|反抗|怼|顶撞|不服|凭什么|不干|拒绝|骂/i, attr: 'str', baseDC: 16 },
  { pattern: /逃跑|溜走|装病|借口|有事|先走|明天|拖|躲/i, attr: 'dex', baseDC: 10 },
  { pattern: /算法|代码|论文|数据|实验|思路|优化|写|改/i, attr: 'int', baseDC: 14 },
  { pattern: /认错|道歉|低头|服从|好的|明白|遵命|是|听/i, attr: 'wis', baseDC: 8 },
  { pattern: /熬夜|肝|通宵|加班|干完|搞定|坚持|冲/i, attr: 'con', baseDC: 15 },
];

// ===== 初始场景 =====
const INITIAL_CHAT_LOG: ChatMessage[] = [
  {
    id: createId('chat'),
    role: 'dm',
    text: "晚上10点，实验室里只有机箱的嗡鸣。左边屏幕是跑不通的多智能体算法，右边是阿里和CNCERT的内推确认邮件（距截止仅剩7天）。门突然开了，王导夹着公文包走进来，脸色铁青：'那个横向项目的接口写完没？周末加个班。' 他拉开椅子坐在你旁边。",
    timestamp: nowIso(),
    hooks: [
      { id: createId('hook'), text: '低头认错，承诺周末加班', attr: 'wis', dc: 8, successReward: '导师态度+5', failPenalty: 'MP-10' },
      { id: createId('hook'), text: '试图用论文思路说服导师', attr: 'cha', dc: 12, successReward: '导师态度+10, 风险-5', failPenalty: '导师态度-5, MP-10' },
      { id: createId('hook'), text: '当场硬刚，拒绝加班', attr: 'str', dc: 16, successReward: '风险-10, 但需谨慎', failPenalty: '导师态度-15, 风险+20' },
    ],
  }
];

// ===== 新增类型定义 =====
export type GamePhase = 'PREP' | 'BREAKTHROUGH' | 'FINAL';
export type TimeBlock = 0 | 1 | 2 | 3; // 上午/下午/晚上/深夜

export interface Company {
  name: string;
  stage: 'apply' | 'written' | 'interview1' | 'interview2' | 'offer' | 'rejected';
  difficulty: number;
  isCompleted: boolean;
  hasOffer: boolean;
  attemptCount: number;
}

export interface MainQuest {
  id: string;
  description: string;
  isCompleted: boolean;
}

export interface SideQuest {
  id: string;
  description: string;
  type: 'study' | 'social' | 'prep';
  isCompleted: boolean;
  currentProgress: number;
  requiredProgress: number;
  rewardPrep: number;
}

export interface TurnResult {
  narrative: string;
  statChanges: {
    hp: number;
    mp: number;
    risk: number;
    mentorMood: number;
  };
  questProgress: string[];
  suggestion: string;
}

export interface StrategySuggestion {
  currentPhase: string;
  suggestions: Array<{
    id: string;
    type: string;
    title: string;
    description: string;
    priority: number;
    isCompleted: boolean;
    completionReason: string;
  }>;
  lastActionAnalysis?: {
    playerDid: string;
    expectedWas: string;
    isMatch: boolean;
    feedback: string;
  };
  riskWarning: string;
  nextRecommended: string;
  // 兼容旧格式
  priority?: string[];
  recommendedAction?: string;
}

// ===== Store 接口 =====
interface GameStore {
  // 基础状态
  hp: number; maxHp: number; mp: number; maxMp: number; gold: number;
  attributes: { str: number; dex: number; int: number; con: number; wis: number; cha: number };
  inGameHours: number; maxHours: number; gameState: GameState;
  chatLog: ChatMessage[]; agentLogs: AgentLog[];
  isRolling: boolean; currentRoll: number | null; currentModifier: number;
  
  // Phase 3 新增核心状态
  gamePhase: GamePhase;
  risk: number;
  mentorMood: number;
  preparation: number;
  currentDay: number;
  timeBlock: TimeBlock;
  
  // 进度系统
  companies: Company[];
  mainQuests: MainQuest[];
  sideQuests: SideQuest[];
  
  // 回合结果与建议
  turnResult: TurnResult | null;
  strategySuggestion: StrategySuggestion | null;
  
  // 流式输出
  streamingText: string;
  isStreaming: boolean;
  
  // 游戏结束状态
  isGameOver: boolean;
  endingType: string | null;
  
  // 基础方法
  addChat: (msg: Omit<ChatMessage, 'id' | 'timestamp'>) => void;
  addAgentLog: (agentName: string, action: string, status?: AgentLog['status']) => void;
  updateStats: (stats: Partial<{ hp: number; mp: number; gold: number }>) => void;
  advanceTime: (hours: number) => void;
  setRolling: (rolling: boolean) => void;
  updateRollResult: (roll: number, modifier?: number) => void;
  setStreamingText: (text: string) => void;
  appendStreamingText: (chunk: string) => void;
  setIsStreaming: (streaming: boolean) => void;
  
  // Phase 3 新增方法
  updateGamePhase: (phase: GamePhase) => void;
  updateRisk: (delta: number) => void;
  updateMentorMood: (delta: number) => void;
  updatePreparation: (delta: number) => void;
  advanceGameTime: (blocks: number) => void;
  updateCompanyProgress: (companyName: string, stage: Company['stage']) => void;
  completeQuest: (questId: string) => void;
  setTurnResult: (result: TurnResult | null) => void;
  setStrategySuggestion: (suggestion: StrategySuggestion | null) => void;
  
  // API 方法
  callBackendAPI: (playerInput: string) => Promise<string>;
  callBackendAPIStreaming: (playerInput: string, onChunk: (chunk: string) => void) => Promise<void>;
  submitPlayerAction: (text: string) => Promise<void>;
  generateHooks: (context: string) => ActionHook[];
  resetGame: () => void;
}

export const useGameStore = create<GameStore>((set, get) => ({
  // 基础状态
  hp: 100, maxHp: 100, mp: 100, maxMp: 100, gold: 800,
  attributes: { str: 14, dex: 16, int: 15, con: 12, wis: 13, cha: 14 },
  inGameHours: 22, maxHours: 168, gameState: 'PLAYING',
  chatLog: [...INITIAL_CHAT_LOG], agentLogs: [],
  isRolling: false, currentRoll: null, currentModifier: 0,
  streamingText: '',
  isStreaming: false,
  
  // Phase 3 新增核心状态
  gamePhase: 'PREP' as GamePhase,
  risk: 0,
  mentorMood: -10,
  preparation: 0,
  currentDay: 1,
  timeBlock: 2 as TimeBlock,  // 晚上
  
  // 进度系统
  companies: [
    { name: '阿里', stage: 'apply', difficulty: 15, isCompleted: false, hasOffer: false, attemptCount: 0 }
  ],
  mainQuests: [
    { id: 'pass_written_test', description: '通过一次笔试', isCompleted: false },
    { id: 'pass_interview', description: '完成至少一次面试', isCompleted: false },
    { id: 'get_offer', description: '获得实习Offer', isCompleted: false },
    { id: 'mentor_approval', description: '获得导师签字', isCompleted: false },
  ],
  sideQuests: [
    { id: 'study_1', description: '刷题准备笔试', type: 'study', isCompleted: false, currentProgress: 0, requiredProgress: 3, rewardPrep: 1 },
    { id: 'social_1', description: '与师兄交流获取信息', type: 'social', isCompleted: false, currentProgress: 0, requiredProgress: 1, rewardPrep: 1 },
    { id: 'risk_control', description: '控制risk < 50', type: 'prep', isCompleted: false, currentProgress: 0, requiredProgress: 1, rewardPrep: 1 },
  ],
  
  // 回合结果与建议
  turnResult: null,
  strategySuggestion: null,
  
  // 游戏结束状态
  isGameOver: false,
  endingType: null,
  
  // 内部状态
  _isProcessingAction: false,

  addChat: (msg) => {
    const message: ChatMessage = { ...msg, id: createId('chat'), timestamp: nowIso() };
    set((state) => ({ chatLog: [...state.chatLog, message] }));
  },

  addAgentLog: (agentName, action, status = 'pending') => {
    const log: AgentLog = { id: createId('agent'), agentName, action, status, time: nowIso() };
    set((state) => ({ agentLogs: [...state.agentLogs, log] }));
  },

  updateStats: (stats) => {
    set((state) => ({
      hp: stats.hp !== undefined ? Math.max(0, Math.min(stats.hp, state.maxHp)) : state.hp,
      mp: stats.mp !== undefined ? Math.max(0, Math.min(stats.mp, state.maxMp)) : state.mp,
      gold: stats.gold !== undefined ? Math.max(0, stats.gold) : state.gold,
    }));
  },

  advanceTime: (hours) => {
    set((state) => {
      const newHours = state.inGameHours + hours;
      if (state.inGameHours < 24 && newHours >= 24) {
        setTimeout(() => {
          get().addChat({ role: 'system', text: '【时间事件】组会\n被迫参加组会汇报进度\n(HP-10 MP-15)' });
          get().updateStats({ hp: state.hp - 10, mp: state.mp - 15 });
        }, 100);
      }
      if (newHours >= 168) {
        setTimeout(() => {
          set({ gameState: 'GAME_OVER' });
          get().addChat({ role: 'system', text: '【游戏结束】秋招内推已截止...' });
        }, 100);
      }
      return { inGameHours: newHours };
    });
  },

  setRolling: (rolling) => set({ isRolling: rolling }),
  updateRollResult: (roll, modifier = 0) => set({ currentRoll: roll, currentModifier: modifier }),
  setStreamingText: (text) => set({ streamingText: text }),
  appendStreamingText: (chunk) => set((state) => ({ streamingText: state.streamingText + chunk })),
  setIsStreaming: (streaming) => set({ isStreaming: streaming }),

  // Phase 3 新增方法实现
  updateGamePhase: (phase) => set({ gamePhase: phase }),
  
  updateRisk: (delta) => set((state) => ({ 
    risk: Math.max(0, Math.min(100, state.risk + delta)) 
  })),
  
  updateMentorMood: (delta) => set((state) => ({ 
    mentorMood: Math.max(-100, Math.min(100, state.mentorMood + delta)) 
  })),
  
  updatePreparation: (delta) => set((state) => ({ 
    preparation: Math.max(0, Math.min(5, state.preparation + delta)) 
  })),
  
  advanceGameTime: (blocks) => set((state) => {
    const totalBlocks = state.timeBlock + blocks;
    const newDay = state.currentDay + Math.floor(totalBlocks / 4);
    const newBlock = (totalBlocks % 4) as TimeBlock;
    
    // 计算游戏阶段
    let newPhase = state.gamePhase;
    if (newDay <= 2) newPhase = 'PREP';
    else if (newDay <= 5) newPhase = 'BREAKTHROUGH';
    else newPhase = 'FINAL';
    
    return {
      currentDay: Math.min(7, newDay),
      timeBlock: newBlock,
      inGameHours: newDay * 24 + newBlock * 6,
      gamePhase: newPhase,
    };
  }),
  
  updateCompanyProgress: (companyName, stage) => set((state) => ({
    companies: state.companies.map(c => 
      c.name === companyName ? { ...c, stage } : c
    )
  })),
  
  completeQuest: (questId) => set((state) => ({
    mainQuests: state.mainQuests.map(q => 
      q.id === questId ? { ...q, isCompleted: true } : q
    ),
    sideQuests: state.sideQuests.map(q => 
      q.id === questId ? { ...q, isCompleted: true } : q
    ),
  })),
  
  setTurnResult: (result) => set({ turnResult: result }),
  setStrategySuggestion: (suggestion) => set({ strategySuggestion: suggestion }),

  /**
   * 调用后端 API（强制）- 使用正确的 GET SSE 端点
   */
  callBackendAPI: async (playerInput: string) => {
    return new Promise((resolve, reject) => {
      // 从环境变量获取后端 API 地址
      const apiUrl = (import.meta as any).env.VITE_API_BASE_URL || 'http://localhost:8080';
      
      // 使用 EventSource 连接 SSE 端点
      const encodedInput = encodeURIComponent(playerInput);
      const url = `${apiUrl}/api/game/action?sessionId=player-session&input=${encodedInput}`;
      
      console.log('[Frontend] Connecting to SSE:', url);
      const eventSource = new EventSource(url);
      let narrativeText = '';
      let hasReceivedData = false;
      let isCompleted = false;
      
      // 监听叙事片段
      eventSource.addEventListener('narrative-chunk', (event) => {
        console.log('[Frontend] Received narrative-chunk:', event.data);
        try {
          const data = JSON.parse(event.data);
          if (data.content) {
            narrativeText += data.content;
            hasReceivedData = true;
          }
        } catch (e) {
          // 如果不是 JSON，直接追加
          narrativeText += event.data;
          hasReceivedData = true;
        }
      });
      
      // 监听 Agent 事件（日志）
      eventSource.addEventListener('agent-event', (event) => {
        console.log('[Frontend] Agent event:', event.data);
        hasReceivedData = true;
      });
      
      // 连接打开
      eventSource.onopen = () => {
        console.log('[Frontend] SSE connection opened');
      };
      
      // 错误处理 - 区分真正的错误和正常关闭
      eventSource.onerror = (_error) => {
        console.log('[Frontend] SSE error/close event, readyState:', eventSource.readyState);
        // EventSource.CLOSED = 2
        if (eventSource.readyState === EventSource.CLOSED) {
          eventSource.close();
          if (!isCompleted) {
            isCompleted = true;
            if (hasReceivedData) {
              console.log('[Frontend] SSE closed, returning collected text');
              resolve(narrativeText || '王导看着你，没有说话。');
            } else {
              reject(new Error('SSE 连接已关闭，但未收到任何数据'));
            }
          }
        } else if (eventSource.readyState === EventSource.CONNECTING) {
          // 正在重连，不处理
          console.log('[Frontend] SSE reconnecting...');
        } else {
          // 其他错误
          eventSource.close();
          if (!isCompleted) {
            isCompleted = true;
            reject(new Error('SSE 连接失败，请检查后端是否正常运行'));
          }
        }
      };
      
      // 超时处理（45秒后自动关闭，给LLM足够时间）
      setTimeout(() => {
        if (!isCompleted) {
          isCompleted = true;
          eventSource.close();
          if (hasReceivedData) {
            console.log('[Frontend] Timeout but has data, resolving');
            resolve(narrativeText || '王导看着你，没有说话。');
          } else {
            reject(new Error('等待后端响应超时（45秒），可能是DeepSeek API连接问题'));
          }
        }
      }, 45000);
      
      // 监听 onmessage 作为备选（处理没有 event 字段的消息）
      eventSource.onmessage = (event) => {
        console.log('[Frontend] Default message received:', event.data);
        if (event.data && event.data.trim()) {
          narrativeText += event.data;
          hasReceivedData = true;
        }
      };
    });
  },

  /**
   * 核心：提交玩家动作 - 骰子动画 + 流式输出
   */
  submitPlayerAction: async (text: string) => {
    actionCallCount++;
    const callId = actionCallCount;
    console.log(`[Frontend] submitPlayerAction #${callId} 被调用:`, text);
    
    // 防止重复提交
    if (isProcessingAction) {
      console.log(`[Frontend] #${callId} 正在处理中，忽略重复提交:`, text);
      return;
    }
    isProcessingAction = true;
    console.log(`[Frontend] #${callId} 开始处理动作:`, text, '时间:', Date.now());
    
    const { 
      addChat, addAgentLog, setRolling, updateRollResult,
      generateHooks, setStreamingText, attributes,
      setIsStreaming
    } = get();
    
    // 1. 显示玩家行动
    addChat({ role: 'player', text });
    
    // 2. 意图识别
    let intent = INTENT_PATTERNS.find(i => i.pattern.test(text));
    if (!intent) intent = { pattern: /.*/i, attr: 'int', baseDC: 12 };
    
    const modifier = Math.floor((attributes[intent.attr] - 10) / 2);
    
    // 3. 触发骰子动画
    setRolling(true);
    addAgentLog('Manager', `${intent.attr.toUpperCase()}检定 | DC${intent.baseDC} | 调整${modifier >= 0 ? '+' : ''}${modifier}`, 'pending');
    
    const rawRoll = Math.floor(Math.random() * 20) + 1;
    const finalRoll = rawRoll + modifier;
    const isSuccess = finalRoll >= intent.baseDC;
    const isCrit = rawRoll === 20;
    const isFumble = rawRoll === 1;
    
    updateRollResult(rawRoll, modifier);
    addAgentLog('Dice', `🎲 ${rawRoll}${modifier >= 0 ? '+' : ''}${modifier}=${finalRoll} | ${isSuccess ? '成功' : '失败'}`, isSuccess ? 'success' : 'error');

    // 4. 等待骰子动画完成（2.5 秒后）
    await new Promise(resolve => setTimeout(resolve, 2500));
        
    // 5. 关闭骰子，回到对话页
    setRolling(false);
        
    // 6. 应用效果 - 移除本地状态更新，完全依赖后端 SSE state-update
    // 注意：所有状态变化由后端 processGameAction 计算并通过 SSE 推送
    // 前端本地更新会导致状态冲突和不一致
    
    // 7. 开始流式输出 - 先添加一个空的NPC消息
    const resultText = isCrit ? '🎉 大成功！' : isFumble ? '💀 大失败！' : isSuccess ? '✅ 成功' : '❌ 失败';
    const streamingMsgId = createId('chat');
    setStreamingText('');
    setIsStreaming(true);
    
    // 添加一个正在流式输出的消息
    set((state) => ({
      chatLog: [...state.chatLog, {
        id: streamingMsgId,
        role: 'npc',
        name: '王导',
        text: '',
        timestamp: nowIso(),
        hooks: [],
        isStreaming: true,
      }]
    }));
    
    // 8. 调用后端 API 进行流式输出
    let fullText = '';
    try {
      await get().callBackendAPIStreaming(text, (chunk: string) => {
        fullText += chunk;
        // 更新正在流式输出的消息
        set((state) => ({
          chatLog: state.chatLog.map(msg => 
            msg.id === streamingMsgId 
              ? { ...msg, text: fullText }
              : msg
          )
        }));
      });
      addAgentLog('LLM', 'DeepSeek API 调用成功', 'success');
    } catch (apiError) {
      addAgentLog('LLM', 'DeepSeek API 调用失败', 'error');
      fullText = `【系统错误】无法连接到AI服务。\n错误信息：${apiError instanceof Error ? apiError.message : '未知错误'}`;
      set((state) => ({
        chatLog: state.chatLog.map(msg => 
          msg.id === streamingMsgId 
            ? { ...msg, text: fullText, isStreaming: false }
            : msg
        )
      }));
      // 错误时也要重置标志
      isProcessingAction = false;
      return;
    }
    
    // 9. 流式输出完成，生成行动选项并更新消息
    const hooks = generateHooks(fullText);
    
    set((state) => ({
      chatLog: state.chatLog.map(msg => 
        msg.id === streamingMsgId 
          ? { 
              ...msg, 
              text: `${fullText}\n\n**【${resultText}】**${isSuccess ? '局势对你有利' : '压力倍增'}`,
              hooks,
              isStreaming: false,
            }
          : msg
      ),
      streamingText: '',
      isStreaming: false,
    }));
    
    // 注意：时间推进由后端控制，前端不再调用 advanceTime
    
    // 重置处理标志
    isProcessingAction = false;
    console.log(`[Frontend] #${callId} 动作处理完成:`, text);
  },

  /**
   * 流式调用后端 API
   */
  callBackendAPIStreaming: async (playerInput: string, onChunk: (chunk: string) => void) => {
    streamingCallCount++;
    const callId = streamingCallCount;
    
    // 生成唯一请求 ID，防止 EventSource 重连导致的重复处理
    const requestId = `req-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
    
    return new Promise((resolve, reject) => {
      // 从环境变量获取后端 API 地址
      const apiUrl = (import.meta as any).env.VITE_API_BASE_URL || 'http://localhost:8080';
          
      const encodedInput = encodeURIComponent(playerInput);
      // 添加 requestId 参数，后端可以用来去重
      const url = `${apiUrl}/api/game/action?sessionId=player-session&input=${encodedInput}&requestId=${requestId}`;
      
      console.log(`[Frontend] #${callId} Connecting to SSE:`, url, '时间:', Date.now());
      const eventSource = new EventSource(url);
      let stateUpdateCount = 0;
      let isCompleted = false;
      let hasReceivedChunk = false;
      
      eventSource.addEventListener('narrative-chunk', (event) => {
        hasReceivedChunk = true;
        try {
          const data = JSON.parse(event.data);
          // 后端使用 contentChunk 字段
          const chunk = data.contentChunk || data.content;
          if (chunk && !data.isFinished) {
            onChunk(chunk);
          }
        } catch (e) {
          onChunk(event.data);
        }
      });
      
      eventSource.addEventListener('agent-event', (event) => {
        console.log('[Frontend] Agent event:', event.data);
      });
      
      // 监听状态更新事件
      eventSource.addEventListener('state-update', (event) => {
        stateUpdateCount++;
        console.log(`[Frontend] #${callId} State update #${stateUpdateCount}:`, event.data);
        try {
          const stateData = JSON.parse(event.data);
          // 更新游戏状态
          set((state) => ({
            gamePhase: stateData.gamePhase ?? state.gamePhase,
            currentDay: stateData.currentDay ?? state.currentDay,
            timeBlock: stateData.timeBlock !== undefined ? stateData.timeBlock : state.timeBlock,
            risk: stateData.risk !== undefined ? stateData.risk : state.risk,
            mentorMood: stateData.mentorMood !== undefined ? stateData.mentorMood : state.mentorMood,
            preparation: stateData.preparation !== undefined ? stateData.preparation : state.preparation,
            hp: stateData.hp !== undefined ? stateData.hp : state.hp,
            mp: stateData.mp !== undefined ? stateData.mp : state.mp,
            gold: stateData.gold !== undefined ? stateData.gold : state.gold,
            companies: stateData.companies || state.companies,
            mainQuests: stateData.mainQuests ? 
              state.mainQuests.map(q => ({
                ...q,
                isCompleted: stateData.mainQuests[q.id] || q.isCompleted
              })) : state.mainQuests,
          }));
          
          // 显示状态变化提示
          if (stateData.changes) {
            const changes = stateData.changes;
            const changeTexts = [];
            if (changes.risk) changeTexts.push(`风险${changes.risk > 0 ? '+' : ''}${changes.risk}`);
            if (changes.mentorMood) changeTexts.push(`导师态度${changes.mentorMood > 0 ? '+' : ''}${changes.mentorMood}`);
            if (changes.preparation) changeTexts.push(`准备度+${changes.preparation}`);
            if (changes.hp) changeTexts.push(`HP${changes.hp > 0 ? '+' : ''}${changes.hp}`);
            if (changes.mp) changeTexts.push(`MP${changes.mp > 0 ? '+' : ''}${changes.mp}`);
            
            if (changeTexts.length > 0) {
              get().addChat({
                role: 'system',
                text: `【状态变化】${changeTexts.join(' ')}`
              });
            }
          }
          
          // 显示任务完成提示
          if (stateData.questCompleted) {
            get().addChat({
              role: 'system',
              text: `【任务完成】${stateData.questCompleted}`
            });
          }
          
          // 显示公司进度更新
          if (stateData.companyUpdate) {
            get().addChat({
              role: 'system',
              text: `【求职进度】${stateData.companyUpdate}`
            });
          }
        } catch (e) {
          console.error('[Frontend] 解析状态更新失败:', e);
        }
      });
      
      // 监听策略更新事件
      eventSource.addEventListener('strategy-update', (event) => {
        console.log('[Frontend] Strategy update:', event.data);
        try {
          const strategyData = JSON.parse(event.data);
          set({ strategySuggestion: strategyData });
        } catch (e) {
          console.error('[Frontend] 解析策略更新失败:', e);
        }
      });
      
      // 监听游戏结束事件
      eventSource.addEventListener('game-end', (event) => {
        console.log('[Frontend] Game end:', event.data);
        try {
          const endData = JSON.parse(event.data);
          
          // 添加结局消息
          get().addChat({
            role: 'system',
            text: `━━━━━━━━━━━━━━━━━━━━━━━━\n${endData.endingTitle}\n\n${endData.endingDesc}\n━━━━━━━━━━━━━━━━━━━━━━━━`
          });
          
          // 设置游戏结束状态
          const gameState = endData.gameState || (endData.endingType?.includes('ENDING') && !endData.endingType?.includes('SUCCESS') ? 'GAME_OVER' : 'VICTORY');
          set({ 
            isGameOver: true, 
            endingType: endData.endingType,
            gameState: gameState
          });
          
          // 延迟后关闭连接
          setTimeout(() => {
            if (!isCompleted) {
              isCompleted = true;
              eventSource.close();
              resolve(undefined);
            }
          }, 2000);
        } catch (e) {
          console.error('[Frontend] 解析游戏结局失败:', e);
        }
      });
      
      // 监听完成事件
      eventSource.addEventListener('narrative-chunk', (event) => {
        try {
          const data = JSON.parse(event.data);
          if (data.isFinished) {
            console.log('[Frontend] Received finish signal');
            if (!isCompleted) {
              isCompleted = true;
              eventSource.close();
              resolve(undefined);
            }
          }
        } catch (e) {
          // ignore
        }
      });
      
      eventSource.onopen = () => {
        console.log('[Frontend] SSE connection opened');
      };
      
      eventSource.onerror = (_error) => {
        console.log('[Frontend] SSE onerror, readyState:', eventSource.readyState);
        // 重要：立即关闭连接，防止EventSource自动重连导致重复请求
        eventSource.close();
        
        // 如果已经收到过数据，说明是正常结束
        if (hasReceivedChunk) {
          if (!isCompleted) {
            isCompleted = true;
            resolve(undefined);
          }
        } else {
          // 没收到数据就断开，返回错误
          if (!isCompleted) {
            isCompleted = true;
            reject(new Error('连接已关闭但未收到数据'));
          }
        }
      };
      
      setTimeout(() => {
        if (!isCompleted) {
          isCompleted = true;
          eventSource.close();
          resolve(undefined);
        }
      }, 45000);
    });
  },

  /**
   * 生成行动选项
   */
  generateHooks: (context: string) => {
    const { hp, mp, preparation, companies, sideQuests } = get();
    const hooks: ActionHook[] = [];
    
    // 求职相关选项
    const activeCompany = companies.find(c => !c.isCompleted);
    if (activeCompany) {
      if (activeCompany.stage === 'apply') {
        hooks.push({ id: createId('hook'), text: `投递${activeCompany.name}简历`, attr: 'int', dc: 10, hpCost: 5 });
      } else if (activeCompany.stage === 'written') {
        hooks.push({ id: createId('hook'), text: `参加${activeCompany.name}笔试 [准备度${preparation}]`, attr: 'int', dc: activeCompany.difficulty, hpCost: 15 });
      } else if (activeCompany.stage.startsWith('interview')) {
        hooks.push({ id: createId('hook'), text: `${activeCompany.name}面试 [准备度${preparation}]`, attr: 'cha', dc: activeCompany.difficulty, hpCost: 20 });
      }
    }
    
    // 子任务选项
    const activeQuest = sideQuests.find(q => !q.isCompleted);
    if (activeQuest) {
      if (activeQuest.type === 'study') {
        hooks.push({ id: createId('hook'), text: `刷题 [${activeQuest.currentProgress}/${activeQuest.requiredProgress}]`, attr: 'int', dc: 12, hpCost: 10 });
      } else if (activeQuest.type === 'social') {
        hooks.push({ id: createId('hook'), text: `找李师弟交流`, attr: 'wis', dc: 10, hpCost: 5 });
      }
    }
    
    // 导师相关选项
    if (context.includes('导师') || context.includes('王导')) {
      hooks.push(
        { id: createId('hook'), text: '尝试说服导师', attr: 'cha', dc: 12 },
        { id: createId('hook'), text: '硬刚到底', attr: 'str', dc: 16 },
      );
    }
    
    // 恢复选项
    if (hp < 60) {
      hooks.push({ id: createId('hook'), text: '买咖啡恢复精力 [HP+20, -¥25]', hpCost: -20, goldCost: 25, isRest: true });
    }
    if (mp < 60) {
      hooks.push({ id: createId('hook'), text: '休息调整心态 [MP+20]', mpCost: -20, isRest: true });
    }
    
    return hooks.slice(0, 4);
  },

  resetGame: async () => {
    // 从环境变量获取后端 API 地址
    const apiUrl = (import.meta as any).env.VITE_API_BASE_URL || 'http://localhost:8080';
    
    // 调用后端重置 API
    try {
      await fetch(`${apiUrl}/api/game/reset?sessionId=player-session`, {
        method: 'POST',
      });
      console.log('[Frontend] 后端会话已重置');
    } catch (e) {
      console.error('[Frontend] 重置后端会话失败:', e);
    }
    
    set({
      // 基础状态
      hp: 100, maxHp: 100, mp: 100, maxMp: 100, gold: 800,
      attributes: { str: 14, dex: 16, int: 15, con: 12, wis: 13, cha: 14 },
      inGameHours: 22, maxHours: 168, gameState: 'PLAYING',
      chatLog: [...INITIAL_CHAT_LOG],
      agentLogs: [],
      isRolling: false, currentRoll: null, currentModifier: 0,
      streamingText: '',
      isStreaming: false,
      
      // Phase 3 核心状态
      gamePhase: 'PREP',
      risk: 0,
      mentorMood: -10,
      preparation: 0,
      currentDay: 1,
      timeBlock: 2,
      
      // 进度系统
      companies: [
        { name: '阿里', stage: 'apply', difficulty: 15, isCompleted: false, hasOffer: false, attemptCount: 0 }
      ],
      mainQuests: [
        { id: 'pass_written_test', description: '通过一次笔试', isCompleted: false },
        { id: 'pass_interview', description: '完成至少一次面试', isCompleted: false },
        { id: 'get_offer', description: '获得实习Offer', isCompleted: false },
        { id: 'mentor_approval', description: '获得导师签字', isCompleted: false },
      ],
      sideQuests: [
        { id: 'study_1', description: '刷题准备笔试', type: 'study', isCompleted: false, currentProgress: 0, requiredProgress: 3, rewardPrep: 1 },
        { id: 'social_1', description: '与师兄交流获取信息', type: 'social', isCompleted: false, currentProgress: 0, requiredProgress: 1, rewardPrep: 1 },
        { id: 'risk_control', description: '控制risk < 50', type: 'prep', isCompleted: false, currentProgress: 0, requiredProgress: 1, rewardPrep: 1 },
      ],
      
      // 回合结果与建议
      turnResult: null,
      strategySuggestion: null,
      
      // 游戏结束状态
      isGameOver: false,
      endingType: null,
    });
  },
}));

export default useGameStore;
