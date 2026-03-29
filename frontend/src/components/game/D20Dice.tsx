import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { useGameStore } from '../../store/useGameStore';

/**
 * D20 二十面骰子组件 - V2
 * 
 * 显示原始点数、调整值、最终结果
 * 
 * @author Chrono Engine Team
 * @version 2.0.0
 */

export function D20Dice() {
  const { currentRoll, currentModifier } = useGameStore();
  const [displayNumber, setDisplayNumber] = useState<number>(1);
  const [isFinal, setIsFinal] = useState(false);

  useEffect(() => {
    const interval = setInterval(() => {
      setDisplayNumber(Math.floor(Math.random() * 20) + 1);
    }, 80);

    const timeout = setTimeout(() => {
      clearInterval(interval);
      if (currentRoll !== null) {
        setDisplayNumber(currentRoll);
        setIsFinal(true);
      }
    }, 2300);

    return () => {
      clearInterval(interval);
      clearTimeout(timeout);
    };
  }, [currentRoll]);

  const finalResult = currentRoll !== null ? currentRoll + currentModifier : 0;
  
  const getResultStyle = (roll: number) => {
    if (roll === 20) return { color: 'text-green-400', label: '大成功!', glow: 'shadow-green-400/50' };
    if (roll === 1) return { color: 'text-red-500', label: '大失败!', glow: 'shadow-red-500/50' };
    if (roll >= 15) return { color: 'text-blue-400', label: '成功', glow: 'shadow-blue-400/30' };
    if (roll >= 10) return { color: 'text-yellow-400', label: '普通', glow: 'shadow-yellow-400/30' };
    return { color: 'text-orange-400', label: '困难', glow: 'shadow-orange-400/30' };
  };

  const result = currentRoll !== null ? getResultStyle(currentRoll) : getResultStyle(1);

  return (
    <motion.div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/90 backdrop-blur-md"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
    >
      <div className="relative flex flex-col items-center">
        {/* 标题 */}
        <motion.div
          className="text-green-400 text-sm mb-6 tracking-widest font-mono"
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
        >
          &gt; D20 命运检定
        </motion.div>

        {/* 骰子 */}
        <div className="relative w-36 h-36" style={{ perspective: '1000px' }}>
          <motion.div
            className="w-full h-full relative"
            style={{ transformStyle: 'preserve-3d' }}
            animate={{
              rotateX: [0, 360, 720, 1080, 1440],
              rotateY: [0, 180, 360, 540, 720],
              rotateZ: [0, 90, 180, 270, 360],
            }}
            transition={{ duration: isFinal ? 0.5 : 2.5, ease: "easeInOut" }}
          >
            {[...Array(20)].map((_, i) => {
              const angle = (360 / 20) * i;
              return (
                <div
                  key={i}
                  className="absolute w-full h-full flex items-center justify-center"
                  style={{
                    transform: `rotateY(${angle}deg) translateZ(70px)`,
                    backfaceVisibility: 'hidden',
                  }}
                >
                  <div 
                    className={`w-28 h-28 rounded-2xl bg-gradient-to-br from-blue-600 to-purple-700 flex items-center justify-center ${isFinal ? result.glow : ''}`}
                    style={{ boxShadow: isFinal ? `0 0 60px 20px currentColor` : '0 0 40px rgba(59, 130, 246, 0.5)' }}
                  >
                    <span className={`text-5xl font-bold ${isFinal ? result.color : 'text-white'}`}>
                      {displayNumber}
                    </span>
                  </div>
                </div>
              );
            })}
          </motion.div>
        </div>

        {/* 结果详情 */}
        {isFinal && currentRoll !== null && (
          <motion.div
            className="mt-8 text-center"
            initial={{ opacity: 0, scale: 0.5 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ type: "spring", stiffness: 200, damping: 15 }}
          >
            {/* 计算过程 */}
            <div className="flex items-center justify-center gap-3 text-lg font-mono mb-3">
              <span className="text-white">{currentRoll}</span>
              <span className="text-gray-500">+</span>
              <span className={currentModifier >= 0 ? 'text-green-400' : 'text-red-400'}>
                {currentModifier >= 0 ? '+' : ''}{currentModifier}
              </span>
              <span className="text-gray-500">=</span>
              <span className={`text-2xl font-bold ${result.color}`}>{finalResult}</span>
            </div>
            
            <div className={`text-2xl font-bold ${result.color} mb-1`}>
              {result.label}
            </div>
            <div className="text-gray-500 text-xs">
              原始点数 {currentRoll} / 调整值 {currentModifier >= 0 ? '+' : ''}{currentModifier}
            </div>
          </motion.div>
        )}

        {/* 扫描线 */}
        <div className="absolute inset-0 pointer-events-none overflow-hidden opacity-10">
          <motion.div
            className="w-full h-0.5 bg-green-400"
            animate={{ top: ['0%', '100%'] }}
            transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
          />
        </div>
      </div>
    </motion.div>
  );
}

export default D20Dice;
