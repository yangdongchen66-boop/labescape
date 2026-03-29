import { motion } from 'framer-motion';
import { useGameStore } from '../../store/useGameStore';
import { Heart, Brain, Wallet, Dumbbell, Eye, BrainCircuit, Shield, Sparkles, MessageCircle } from 'lucide-react';

/**
 * 学生档案 HUD - 电子工牌风格
 * 
 * 显示 HP、MP、Gold 和六维属性
 * 
 * @author Chrono Engine Team
 */

export function StudentProfileHUD() {
  const { hp, maxHp, mp, maxMp, gold, attributes, currentDay } = useGameStore();
  
  const attrs = [
    { key: 'str', name: '肝力', icon: Dumbbell, value: attributes.str, color: 'text-red-400' },
    { key: 'dex', name: '摸鱼', icon: Eye, value: attributes.dex, color: 'text-green-400' },
    { key: 'int', name: '算法', icon: BrainCircuit, value: attributes.int, color: 'text-blue-400' },
    { key: 'con', name: '发际线', icon: Shield, value: attributes.con, color: 'text-amber-400' },
    { key: 'wis', name: '情商', icon: Sparkles, value: attributes.wis, color: 'text-purple-400' },
    { key: 'cha', name: '忽悠', icon: MessageCircle, value: attributes.cha, color: 'text-pink-400' },
  ];

  const getBarColor = (value: number, max: number) => {
    const ratio = value / max;
    if (ratio > 0.6) return 'bg-green-500';
    if (ratio > 0.3) return 'bg-yellow-500';
    return 'bg-red-500';
  };

  return (
    <div className="flex h-full items-center gap-4 px-4">
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
          <Heart className="w-3 h-3 text-red-400 shrink-0" />
          <div className="flex-1 h-1.5 bg-[#333] rounded-full overflow-hidden">
            <motion.div 
              className={`h-full ${getBarColor(hp, maxHp)}`}
              initial={{ width: 0 }}
              animate={{ width: `${(hp / maxHp) * 100}%` }}
              transition={{ duration: 0.5 }}
            />
          </div>
          <span className="text-[10px] text-gray-400 w-10 text-right font-mono">
            {hp}/{maxHp}
          </span>
        </div>

        {/* MP */}
        <div className="flex items-center gap-2">
          <Brain className="w-3 h-3 text-blue-400 shrink-0" />
          <div className="flex-1 h-1.5 bg-[#333] rounded-full overflow-hidden">
            <motion.div 
              className={`h-full ${getBarColor(mp, maxMp)}`}
              initial={{ width: 0 }}
              animate={{ width: `${(mp / maxMp) * 100}%` }}
              transition={{ duration: 0.5 }}
            />
          </div>
          <span className="text-[10px] text-gray-400 w-10 text-right font-mono">
            {mp}/{maxMp}
          </span>
        </div>

        {/* Gold */}
        <div className="flex items-center gap-2">
          <Wallet className="w-3 h-3 text-yellow-400 shrink-0" />
          <span className="text-[10px] text-yellow-400 font-mono">
            ¥{gold}
          </span>
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
  );
}

export default StudentProfileHUD;
