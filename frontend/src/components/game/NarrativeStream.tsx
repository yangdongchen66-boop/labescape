import { useRef, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useGameStore } from '../../store/useGameStore';
import { Sword, Brain, Eye, Heart, Sparkles, MessageCircle, Coins } from 'lucide-react';

/**
 * 叙事流组件 - V3 LLM版
 * 
 * @author Chrono Engine Team
 */

const attrIcons: Record<string, typeof Sword> = {
  str: Sword, dex: Eye, int: Brain, con: Heart, wis: Sparkles, cha: MessageCircle,
};

export function NarrativeStream() {
  const { chatLog, gameState, isRolling, submitPlayerAction } = useGameStore();
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: 'smooth' });
  }, [chatLog]);

  const getRoleStyle = (role: string) => {
    switch (role) {
      case 'dm': return { container: 'bg-purple-500/10 border-l-2 border-purple-500', name: 'text-purple-400', prefix: '🎲' };
      case 'npc': return { container: 'bg-blue-500/10 border-l-2 border-blue-500', name: 'text-blue-400', prefix: '👤' };
      case 'player': return { container: 'bg-green-500/10 border-l-2 border-green-500 ml-8', name: 'text-green-400', prefix: '>' };
      case 'system': return { container: 'bg-red-500/10 border-l-2 border-red-500', name: 'text-red-400', prefix: '⚠️' };
      default: return { container: 'bg-gray-500/10', name: 'text-gray-400', prefix: '•' };
    }
  };

  const handleHookClick = (hookText: string) => {
    if (isRolling) return;
    submitPlayerAction(hookText);
  };

  return (
    <div ref={scrollRef} className="h-full overflow-y-auto p-4 space-y-3 scroll-smooth" style={{ background: 'linear-gradient(180deg, #1e1e1e 0%, #1a1a1a 100%)' }}>
      <AnimatePresence initial={false}>
        {chatLog.map((msg) => {
          const style = getRoleStyle(msg.role);
          const hasHooks = msg.hooks && msg.hooks.length > 0;
          
          return (
            <motion.div
              key={msg.id}
              className={`p-3 rounded-r-lg ${style.container}`}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3 }}
            >
              <div className="flex items-center gap-2 mb-2">
                <span>{style.prefix}</span>
                <span className={`text-xs font-bold ${style.name}`}>
                  {msg.name || (msg.role === 'dm' ? '命运法则' : msg.role === 'player' ? '你' : '系统')}
                </span>
                <span className="text-[10px] text-gray-600">
                  {new Date(msg.timestamp).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}
                </span>
              </div>
              
              <div className="text-sm leading-relaxed whitespace-pre-wrap text-gray-300 mb-3">
                {msg.text}
                {msg.isStreaming && (
                  <span className="inline-block w-2 h-4 ml-1 bg-blue-400 animate-pulse" />
                )}
              </div>
              
              {hasHooks && !isRolling && (
                <div className="space-y-2 mt-3 pt-3 border-t border-gray-700/50">
                  <div className="text-[10px] text-gray-500 uppercase tracking-wider mb-2">选择行动:</div>
                  {msg.hooks!.map((hook) => {
                    const Icon = hook.attr ? attrIcons[hook.attr] : hook.goldCost ? Coins : hook.isRest ? Heart : MessageCircle;
                    return (
                      <motion.button
                        key={hook.id}
                        onClick={() => handleHookClick(hook.text)}
                        disabled={isRolling}
                        className="w-full flex items-center gap-3 p-2.5 rounded-lg bg-[#252526] border border-[#333] hover:border-blue-500/50 hover:bg-[#2a2a2a] transition-all text-left disabled:opacity-50"
                        whileHover={{ scale: 1.01, x: 4 }}
                        whileTap={{ scale: 0.99 }}
                      >
                        <Icon className={`w-4 h-4 ${hook.attr ? 'text-blue-400' : hook.goldCost ? 'text-yellow-400' : 'text-green-400'}`} />
                        <div className="flex-1">
                          <div className="text-xs text-gray-200">{hook.text}</div>
                        </div>
                        <div className="flex gap-2 text-[10px]">
                          {hook.attr && <span className="text-blue-400">{hook.attr.toUpperCase()} DC{hook.dc}</span>}
                          {hook.hpCost && hook.hpCost > 0 && <span className="text-red-400">-{hook.hpCost}HP</span>}
                          {hook.hpCost && hook.hpCost < 0 && <span className="text-green-400">+{-hook.hpCost}HP</span>}
                          {hook.goldCost && <span className="text-yellow-400">-{hook.goldCost}G</span>}
                        </div>
                      </motion.button>
                    );
                  })}
                </div>
              )}
            </motion.div>
          );
        })}
      </AnimatePresence>

      {gameState !== 'PLAYING' && (
        <motion.div 
          className="fixed inset-0 bg-black/90 flex items-center justify-center z-40" 
          initial={{ opacity: 0 }} 
          animate={{ opacity: 1 }}
        >
          <motion.div 
            className={`relative text-center p-8 rounded-2xl border-2 ${
              gameState === 'VICTORY' 
                ? 'bg-gradient-to-br from-green-900/80 to-emerald-900/80 border-green-500/50 shadow-[0_0_60px_rgba(34,197,94,0.3)]' 
                : 'bg-gradient-to-br from-red-900/80 to-orange-900/80 border-red-500/50 shadow-[0_0_60px_rgba(239,68,68,0.3)]'
            }`}
            initial={{ scale: 0.8, y: 20 }}
            animate={{ scale: 1, y: 0 }}
            transition={{ type: "spring", stiffness: 200, damping: 20 }}
          >
            {/* 成功时的庆祝动画 */}
            {gameState === 'VICTORY' && (
              <motion.div
                className="absolute inset-0 pointer-events-none overflow-hidden rounded-2xl"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
              >
                {[...Array(12)].map((_, i) => (
                  <motion.div
                    key={i}
                    className="absolute w-3 h-3 rounded-full"
                    style={{
                      background: ['#22c55e', '#10b981', '#34d399', '#6ee7b7'][i % 4],
                      left: `${10 + (i * 7)}%`,
                      top: '50%',
                    }}
                    initial={{ y: 0, opacity: 1, scale: 0 }}
                    animate={{
                      y: [-20, -80 - Math.random() * 40],
                      x: [(i % 2 === 0 ? 1 : -1) * (20 + Math.random() * 30)],
                      opacity: [1, 1, 0],
                      scale: [0, 1.2, 0.8],
                    }}
                    transition={{
                      duration: 1.5,
                      delay: i * 0.1,
                      repeat: Infinity,
                      repeatDelay: 0.5,
                    }}
                  />
                ))}
              </motion.div>
            )}
            
            {/* 图标 */}
            <motion.div
              className="text-6xl mb-4"
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              transition={{ type: "spring", stiffness: 300, delay: 0.2 }}
            >
              {gameState === 'VICTORY' ? '🏆' : '💀'}
            </motion.div>
            
            {/* 标题 */}
            <motion.div 
              className={`text-4xl font-bold mb-2 ${
                gameState === 'VICTORY' 
                  ? 'text-green-400 drop-shadow-[0_0_10px_rgba(74,222,128,0.5)]' 
                  : 'text-red-400 drop-shadow-[0_0_10px_rgba(248,113,113,0.5)]'
              }`}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.3 }}
            >
              {gameState === 'VICTORY' ? '恭喜通关！' : '游戏结束'}
            </motion.div>
            
            {/* 副标题 */}
            <motion.div 
              className={`text-lg mb-6 ${
                gameState === 'VICTORY' ? 'text-green-300/80' : 'text-red-300/80'
              }`}
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.4 }}
            >
              {gameState === 'VICTORY' 
                ? '你成功获得了实习 Offer，导师也同意了！' 
                : '这次没能成功，但人生还有无数可能...'}
            </motion.div>
            
            {/* 按钮 */}
            <motion.button 
              onClick={() => useGameStore.getState().resetGame()} 
              className={`px-8 py-3 rounded-lg font-semibold transition-all ${
                gameState === 'VICTORY'
                  ? 'bg-green-500 hover:bg-green-400 text-white shadow-[0_0_20px_rgba(34,197,94,0.4)] hover:shadow-[0_0_30px_rgba(34,197,94,0.6)]'
                  : 'bg-red-500 hover:bg-red-400 text-white shadow-[0_0_20px_rgba(239,68,68,0.4)] hover:shadow-[0_0_30px_rgba(239,68,68,0.6)]'
              }`}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.5 }}
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
            >
              {gameState === 'VICTORY' ? '🎉 再玩一次' : '↺ 重新开始'}
            </motion.button>
          </motion.div>
        </motion.div>
      )}

      <div className="h-4" />
    </div>
  );
}

export default NarrativeStream;
