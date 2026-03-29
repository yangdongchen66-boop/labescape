import { useState, useRef, useEffect } from 'react';
import { motion } from 'framer-motion';
import { useGameStore } from '../../store/useGameStore';
import { Send, Coffee, Pizza, RotateCcw } from 'lucide-react';

/**
 * 行动输入组件 - 终端风格
 * 
 * user@lab:~$ [输入行动]
 * 
 * @author Chrono Engine Team
 */

export function ActionInput() {
  const [input, setInput] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);
  
  // 使用选择器避免不必要的重新渲染
  const submitPlayerAction = useGameStore((state) => state.submitPlayerAction);
  const gold = useGameStore((state) => state.gold);
  const updateStats = useGameStore((state) => state.updateStats);
  const addChat = useGameStore((state) => state.addChat);
  const isGameOver = useGameStore((state) => state.isGameOver);
  const resetGame = useGameStore((state) => state.resetGame);

  useEffect(() => {
    inputRef.current?.focus();
  }, []);

  const handleSubmit = async (e?: React.FormEvent) => {
    e?.preventDefault();
    
    if (!input.trim() || isSubmitting) {
      console.log(`[ActionInput] 提交被阻止: input=${input.trim()}, isSubmitting=${isSubmitting}`);
      return;
    }
    
    const text = input;
    console.log(`[ActionInput] 提交: "${text}"`);
    setIsSubmitting(true);
    setInput('');
    
    await submitPlayerAction(text);
    setIsSubmitting(false);
  };

  // 快捷操作
  const quickActions = [
    { 
      icon: Coffee, 
      label: '买美式', 
      action: () => {
        if (gold >= 25) {
          updateStats({ hp: Math.min(100, useGameStore.getState().hp + 15) });
          addChat({ role: 'player', text: '我去楼下瑞幸买了杯冰美式 (+15 HP)' });
        }
      },
      disabled: gold < 25,
      cost: 25
    },
    { 
      icon: Pizza, 
      label: '请烧烤', 
      action: () => {
        if (gold >= 100) {
          updateStats({ mp: Math.min(100, useGameStore.getState().mp + 20), gold: gold - 100 });
          addChat({ role: 'player', text: '我请师弟吃烧烤，让他帮忙代写周报 (+20 MP)' });
        }
      },
      disabled: gold < 100,
      cost: 100
    },
  ];

  // 游戏结束状态
  if (isGameOver) {
    return (
      <div className="h-full flex flex-col justify-center items-center">
        <motion.button
          onClick={resetGame}
          className="flex items-center gap-2 px-6 py-3 bg-green-600 hover:bg-green-500 text-white rounded-lg font-medium transition-colors"
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
        >
          <RotateCcw className="w-5 h-5" />
          <span>重开一局</span>
        </motion.button>
        <p className="text-gray-500 text-sm mt-3">
          命运之轮再次转动...
        </p>
      </div>
    );
  }

  return (
    <div className="h-full flex flex-col justify-center">
      {/* 快捷操作按钮 */}
      <div className="flex items-center gap-2 mb-3">
        {quickActions.map((action) => {
          const Icon = action.icon;
          return (
            <motion.button
              key={action.label}
              onClick={action.action}
              disabled={action.disabled || isSubmitting}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded text-[11px] font-medium transition-colors border ${
                action.disabled 
                  ? 'bg-[#1a1a1a] text-gray-600 border-[#2a2a2a] cursor-not-allowed' 
                  : 'bg-[#252526] text-gray-300 hover:bg-[#2a2a2a] border-[#3a3a3a] hover:border-[#4a4a4a]'
              }`}
              whileHover={!action.disabled ? { scale: 1.02 } : {}}
              whileTap={!action.disabled ? { scale: 0.98 } : {}}
            >
              <Icon className="w-3.5 h-3.5" />
              <span>{action.label}</span>
              <span className="text-yellow-500/80">¥{action.cost}</span>
            </motion.button>
          );
        })}
        
        <div className="flex-1" />
        
        <div className="text-[11px] text-gray-500">
          余额: <span className="text-yellow-400 font-mono">¥{gold}</span>
        </div>
      </div>

      {/* 输入框 */}
      <form onSubmit={handleSubmit} className="relative">
        <div className="flex items-center bg-[#1a1a1a] rounded-lg border border-[#333] focus-within:border-green-500/50 transition-colors">
          <div className="pl-3 pr-2 text-green-400 font-mono text-sm shrink-0 select-none">
            user@lab:~$
          </div>
          
          <input
            ref={inputRef}
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder={isSubmitting ? "等待命运掷骰..." : "输入你的行动..."}
            className="flex-1 bg-transparent text-gray-200 placeholder-gray-600 py-3 pr-10 text-sm focus:outline-none font-mono disabled:opacity-50"
            autoComplete="off"
            spellCheck={false}
            disabled={isSubmitting}
          />
          
          <motion.button
            type="submit"
            disabled={!input.trim() || isSubmitting}
            className={`absolute right-2 p-2 rounded transition-colors ${
              input.trim() && !isSubmitting
                ? 'text-green-400 hover:bg-green-500/10' 
                : 'text-gray-600 cursor-not-allowed'
            }`}
            whileHover={input.trim() && !isSubmitting ? { scale: 1.1 } : {}}
            whileTap={input.trim() && !isSubmitting ? { scale: 0.9 } : {}}
          >
            <Send className="w-4 h-4" />
          </motion.button>
        </div>
      </form>

      {/* 提示文字 */}
      <div className="flex items-center gap-4 text-[10px] text-gray-600 mt-2">
        <span className="text-green-500/60">[Enter] 发送</span>
        <span>•</span>
        <span className="text-gray-500">试试: "说服导师让我实习" / "假装肚子疼溜走"</span>
      </div>
    </div>
  );
}

export default ActionInput;
