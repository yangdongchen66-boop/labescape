import { useState, useEffect, useMemo } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useGameStore } from '../../store/useGameStore';

/**
 * D20 二十面骰子组件 - V3 增强版
 * 
 * 显示原始点数、调整值、最终结果
 * 新增大成功/大失败特效
 * 
 * @author Chrono Engine Team
 * @version 3.0.0
 */

// 金色粒子组件
function GoldenParticles({ count = 50 }: { count?: number }) {
  const particles = useMemo(() => 
    Array.from({ length: count }, (_, i) => ({
      id: i,
      x: Math.random() * 100,
      y: Math.random() * 100,
      size: Math.random() * 8 + 4,
      duration: Math.random() * 1.5 + 1,
      delay: Math.random() * 0.5
    })), [count]);

  return (
    <div className="fixed inset-0 pointer-events-none z-60 overflow-hidden">
      {particles.map(p => (
        <motion.div
          key={p.id}
          className="absolute rounded-full"
          style={{
            left: `${p.x}%`,
            top: `${p.y}%`,
            width: p.size,
            height: p.size,
            background: 'linear-gradient(135deg, #ffd700 0%, #ffec4a 50%, #fff 100%)',
            boxShadow: '0 0 10px #ffd700, 0 0 20px #ffd700'
          }}
          initial={{ opacity: 0, scale: 0 }}
          animate={{ 
            opacity: [0, 1, 1, 0], 
            scale: [0, 1.5, 1, 0],
            y: [-20, -100],
            x: [0, (Math.random() - 0.5) * 100]
          }}
          transition={{ 
            duration: p.duration, 
            delay: p.delay,
            ease: "easeOut"
          }}
        />
      ))}
    </div>
  );
}

// 屏幕破碎组件
function ScreenCrack() {
  const cracks = useMemo(() => 
    Array.from({ length: 8 }, (_, i) => ({
      id: i,
      rotate: i * 45 + Math.random() * 20 - 10,
      length: Math.random() * 30 + 20
    })), []);

  return (
    <div className="fixed inset-0 pointer-events-none z-60">
      <motion.div 
        className="absolute inset-0 bg-red-900/30"
        initial={{ opacity: 0 }}
        animate={{ opacity: [0, 0.5, 0.3] }}
        transition={{ duration: 0.5 }}
      />
      <div className="absolute inset-0 flex items-center justify-center">
        {cracks.map(c => (
          <motion.div
            key={c.id}
            className="absolute bg-red-500/80"
            style={{
              width: 2,
              height: `${c.length}%`,
              transformOrigin: 'center center',
              rotate: c.rotate,
            }}
            initial={{ scaleY: 0, opacity: 0 }}
            animate={{ scaleY: 1, opacity: [0, 1, 0.6] }}
            transition={{ duration: 0.3, delay: Math.random() * 0.2 }}
          />
        ))}
      </div>
      <motion.div
        className="absolute inset-0 border-8 border-red-500/50"
        initial={{ opacity: 0 }}
        animate={{ opacity: [0, 1, 0.5] }}
        transition={{ duration: 0.4 }}
      />
    </div>
  );
}

export function D20Dice() {
  const { currentRoll, currentModifier } = useGameStore();
  const [displayNumber, setDisplayNumber] = useState<number>(1);
  const [isFinal, setIsFinal] = useState(false);
  const [showSpecialEffect, setShowSpecialEffect] = useState<'crit' | 'fail' | null>(null);

  // 震动反馈（移动端）
  const triggerVibration = (type: 'success' | 'fail') => {
    if ('vibrate' in navigator) {
      if (type === 'success') {
        navigator.vibrate([100, 50, 100, 50, 200]); // 胜利节奏
      } else {
        navigator.vibrate([300, 100, 300]); // 失败节奏
      }
    }
  };

  useEffect(() => {
    const interval = setInterval(() => {
      setDisplayNumber(Math.floor(Math.random() * 20) + 1);
    }, 80);

    const timeout = setTimeout(() => {
      clearInterval(interval);
      if (currentRoll !== null) {
        setDisplayNumber(currentRoll);
        setIsFinal(true);
        
        // 触发特殊效果
        if (currentRoll === 20) {
          setShowSpecialEffect('crit');
          triggerVibration('success');
          setTimeout(() => setShowSpecialEffect(null), 2000);
        } else if (currentRoll === 1) {
          setShowSpecialEffect('fail');
          triggerVibration('fail');
          setTimeout(() => setShowSpecialEffect(null), 1500);
        }
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
  
  // 大成功/大失败特殊标题
  const getSpecialTitle = () => {
    if (currentRoll === 20) return '✨ NATURAL 20 ✨';
    if (currentRoll === 1) return '💥 CRITICAL FAIL 💥';
    return '> D20 命运检定';
  };

  return (
    <motion.div
      className={`fixed inset-0 z-50 flex items-center justify-center backdrop-blur-md ${
        showSpecialEffect === 'crit' ? 'bg-yellow-900/30' : 
        showSpecialEffect === 'fail' ? 'bg-red-900/40' : 'bg-black/90'
      }`}
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
    >
      {/* 特殊效果层 */}
      <AnimatePresence>
        {showSpecialEffect === 'crit' && <GoldenParticles count={60} />}
        {showSpecialEffect === 'fail' && <ScreenCrack />}
      </AnimatePresence>

      <div className="relative flex flex-col items-center">
        {/* 标题 */}
        <motion.div
          className={`text-sm mb-6 tracking-widest font-mono ${
            showSpecialEffect === 'crit' ? 'text-yellow-300 text-lg font-bold' :
            showSpecialEffect === 'fail' ? 'text-red-400 text-lg font-bold' : 'text-green-400'
          }`}
          initial={{ opacity: 0, y: -20 }}
          animate={{ 
            opacity: 1, 
            y: 0,
            scale: showSpecialEffect ? [1, 1.2, 1] : 1
          }}
          transition={{ delay: 0.2 }}
        >
          {getSpecialTitle()}
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
                    className={`w-28 h-28 rounded-2xl flex items-center justify-center ${
                      showSpecialEffect === 'crit' 
                        ? 'bg-gradient-to-br from-yellow-400 via-yellow-500 to-orange-500' 
                        : showSpecialEffect === 'fail'
                        ? 'bg-gradient-to-br from-red-600 via-red-700 to-red-900'
                        : 'bg-gradient-to-br from-blue-600 to-purple-700'
                    } ${isFinal ? result.glow : ''}`}
                    style={{ 
                      boxShadow: showSpecialEffect === 'crit' 
                        ? '0 0 80px 30px rgba(255, 215, 0, 0.6)' 
                        : showSpecialEffect === 'fail'
                        ? '0 0 60px 20px rgba(239, 68, 68, 0.5)'
                        : isFinal 
                        ? `0 0 60px 20px currentColor` 
                        : '0 0 40px rgba(59, 130, 246, 0.5)' 
                    }}
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
              {currentRoll === 20 ? '🎉 天选之人！效果翻倍！' : 
               currentRoll === 1 ? '💣 运气耗尽...额外惩罚！' : 
               result.label}
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
