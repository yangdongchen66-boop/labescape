import { motion, AnimatePresence } from 'framer-motion';
import { useGameStore } from '../../store/useGameStore';
import { Heart, Brain, Wallet, Dumbbell, Eye, BrainCircuit, Shield, Sparkles, MessageCircle, AlertTriangle } from 'lucide-react';

/**
 * 学生档案 HUD - 电子工牌风格
 * 
 * 显示 HP、MP、Gold 和六维属性
 * 新增“濒临崩溃”危机状态警告
 * 
 * @author Chrono Engine Team
 */

export function StudentProfileHUD() {
  const { hp, maxHp, mp, maxMp, gold, attributes, currentDay, risk } = useGameStore();
  
  // 危机状态检测
  const isCritical = hp <= 20 || mp <= 20;
  
  const attrs = [
    { key: 'str', name: '肝力', icon: Dumbbell, value: attributes.str, color: 'text-red-400' },
    { key: 'dex', name: '摸鱼', icon: Eye, value: attributes.dex, color: 'text-green-400' },
    { key: 'int', name: '算法', icon: BrainCircuit, value: attributes.int, color: 'text-blue-400' },
    { key: 'con', name: '发际线', icon: Shield, value: attributes.con, color: 'text-amber-400' },
    { key: 'wis', name: '情商', icon: Sparkles, value: attributes.wis, color: 'text-purple-400' },
    { key: 'cha', name: '忽悠', icon: MessageCircle, value: attributes.cha, color: 'text-pink-400' },
  ];

  const getBarColor = (value: number, max: number, isCriticalValue: boolean) => {
    if (isCriticalValue) return 'bg-red-500 animate-pulse';
    const ratio = value / max;
    if (ratio > 0.6) return 'bg-green-500';
    if (ratio > 0.3) return 'bg-yellow-500';
    return 'bg-red-500';
  };

  // 计算导师“距离”（基于风险值）
  const mentorDistance = Math.max(1, Math.floor((100 - risk) / 20));
  const getMentorStatus = () => {
    if (risk >= 80) return { text: '导师就在门口！', color: 'text-red-500', bgColor: 'bg-red-500/20' };
    if (risk >= 60) return { text: `导师距离 ${mentorDistance} 步`, color: 'text-orange-400', bgColor: 'bg-orange-500/20' };
    if (risk >= 40) return { text: '导师在楼下', color: 'text-yellow-400', bgColor: 'bg-yellow-500/10' };
    return null;
  };
  const mentorStatus = getMentorStatus();

  return (
    <>
      {/* 危机状态全屏警告 */}
      <AnimatePresence>
        {isCritical && (
          <motion.div 
            className="fixed inset-0 pointer-events-none z-40"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
          >
            <div className="absolute inset-0 border-4 border-red-500/60 animate-pulse" />
            <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-transparent via-red-500 to-transparent animate-pulse" />
            <div className="absolute bottom-0 left-0 right-0 h-1 bg-gradient-to-r from-transparent via-red-500 to-transparent animate-pulse" />
          </motion.div>
        )}
      </AnimatePresence>

      {/* 导师追踪警告条 */}
      <AnimatePresence>
        {mentorStatus && (
          <motion.div
            className={`fixed top-0 left-0 right-0 z-30 ${mentorStatus.bgColor} border-b border-red-500/30 py-1 px-4 flex items-center justify-center gap-2`}
            initial={{ y: -40, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            exit={{ y: -40, opacity: 0 }}
          >
            <AlertTriangle className={`w-4 h-4 ${mentorStatus.color} ${risk >= 80 ? 'animate-bounce' : ''}`} />
            <span className={`text-xs font-bold ${mentorStatus.color}`}>
              {mentorStatus.text}
            </span>
            {risk >= 60 && (
              <div className="flex gap-1 ml-2">
                {[...Array(5)].map((_, i) => (
                  <div 
                    key={i} 
                    className={`w-2 h-2 rounded-full ${i < Math.ceil(risk / 20) ? 'bg-red-500' : 'bg-gray-600'}`}
                  />
                ))}
              </div>
            )}
          </motion.div>
        )}
      </AnimatePresence>

      <div className={`flex h-full items-center gap-4 px-4 ${mentorStatus ? 'mt-6' : ''}`}>
      {/* 头像 */}
      <motion.div 
        className="relative shrink-0"
        whileHover={{ scale: 1.05 }}
      >
        <div className="w-14 h-14 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center text-2xl border-2 border-[#333]">
          👨‍💻
        </div>
        <div className="absolute -bottom-0.5 -right-0.5 w-4 h-4 bg-green-500 rounded-full border-2 border-[#252526]" />
      </motion.div>

      {/* 基本信息 */}
      <div className="flex flex-col min-w-[120px]">
        <div className="flex items-center gap-2">
          <span className="text-white font-semibold text-sm">研二求生者</span>
          <span className="text-[10px] px-1.5 py-0.5 rounded bg-blue-500/20 text-blue-400 border border-blue-500/30">
            研二
          </span>
        </div>
        <span className="text-[10px] text-gray-400">计算机科学与技术</span>
        <div className={`text-[10px] mt-1 font-mono ${currentDay >= 6 ? 'text-red-400 animate-pulse' : 'text-yellow-400'}`}>
          ⏱️ Day {currentDay}/7
        </div>
      </div>

      {/* 分隔线 */}
      <div className="w-px h-10 bg-[#333]" />

      {/* 生存三围 */}
      <div className="flex flex-col gap-1.5 min-w-[100px]">
        {/* HP */}
        <div className="flex items-center gap-2">
          <Heart className={`w-3 h-3 shrink-0 ${hp <= 20 ? 'text-red-500 animate-pulse' : 'text-red-400'}`} />
          <div className={`flex-1 h-1.5 bg-[#333] rounded-full overflow-hidden ${hp <= 20 ? 'ring-1 ring-red-500/50' : ''}`}>
            <motion.div 
              className={`h-full ${getBarColor(hp, maxHp, hp <= 20)}`}
              initial={{ width: 0 }}
              animate={{ width: `${(hp / maxHp) * 100}%` }}
              transition={{ duration: 0.5 }}
            />
          </div>
          <span className={`text-[10px] w-10 text-right font-mono ${hp <= 20 ? 'text-red-400 font-bold' : 'text-gray-400'}`}>
            {hp}/{maxHp}
          </span>
        </div>

        {/* MP */}
        <div className="flex items-center gap-2">
          <Brain className={`w-3 h-3 shrink-0 ${mp <= 20 ? 'text-blue-300 animate-pulse' : 'text-blue-400'}`} />
          <div className={`flex-1 h-1.5 bg-[#333] rounded-full overflow-hidden ${mp <= 20 ? 'ring-1 ring-blue-500/50' : ''}`}>
            <motion.div 
              className={`h-full ${getBarColor(mp, maxMp, mp <= 20)}`}
              initial={{ width: 0 }}
              animate={{ width: `${(mp / maxMp) * 100}%` }}
              transition={{ duration: 0.5 }}
            />
          </div>
          <span className={`text-[10px] w-10 text-right font-mono ${mp <= 20 ? 'text-blue-300 font-bold' : 'text-gray-400'}`}>
            {mp}/{maxMp}
          </span>
        </div>

        {/* Gold */}
        <div className="flex items-center gap-2">
          <Wallet className={`w-3 h-3 shrink-0 ${gold < 50 ? 'text-yellow-600' : 'text-yellow-400'}`} />
          <span className={`text-[10px] font-mono ${gold < 50 ? 'text-yellow-600' : 'text-yellow-400'}`}>
            ¥{gold}
          </span>
          {gold < 50 && <span className="text-[8px] text-yellow-600">(穷)</span>}
        </div>
      </div>

      {/* 分隔线 */}
      <div className="w-px h-10 bg-[#333]" />

      {/* 六维属性 */}
      <div className="grid grid-cols-6 gap-1.5">
        {attrs.map((attr) => {
          const Icon = attr.icon;
          return (
            <motion.div 
              key={attr.key}
              className="flex flex-col items-center p-1 rounded bg-[#1e1e1e] border border-[#333]"
              whileHover={{ scale: 1.05, borderColor: '#555' }}
            >
              <Icon className={`w-3 h-3 ${attr.color}`} />
              <span className="text-[9px] text-gray-500 mt-0.5">{attr.name}</span>
              <span className={`text-[10px] font-bold ${attr.color}`}>{attr.value}</span>
            </motion.div>
          );
        })}
      </div>
    </div>
    </>
  );
}

export default StudentProfileHUD;
