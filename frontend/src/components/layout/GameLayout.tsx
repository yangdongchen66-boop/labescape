import { ReactNode } from 'react';
import { AnimatePresence } from 'framer-motion';
import { useGameStore } from '../../store/useGameStore';
import { D20Dice } from '../game/D20Dice';
import { GameDashboard } from '../game/GameDashboard';
import { StrategyPanel } from '../game/StrategyPanel';

/**
 * 游戏主布局组件
 * 
 * 左侧 75% 游玩区 + 右侧 25% Agent 监控终端
 * 
 * @author Chrono Engine Team
 */

interface GameLayoutProps {
  header: ReactNode;
  scene: ReactNode;
  hand: ReactNode;
  sidebar: ReactNode;
}

export function GameLayout({ header, scene, hand, sidebar }: GameLayoutProps) {
  const { isRolling } = useGameStore();

  return (
    <div className="h-screen w-screen bg-[#1e1e1e] flex overflow-hidden font-mono">
      {/* 左侧 75% 游玩区 */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* 顶部 HUD */}
        <header className="h-20 border-b border-[#333] bg-[#252526] shrink-0">
          {header}
        </header>

        {/* 中间区域：仪表盘 + 叙事流 */}
        <main className="flex-1 overflow-hidden relative flex">
          {/* 左侧仪表盘 */}
          <div className="w-80 border-r border-[#333] bg-[#1e1e1e] overflow-y-auto p-4 space-y-4">
            <GameDashboard />
            <StrategyPanel />
          </div>
          
          {/* 右侧叙事流 */}
          <div className="flex-1 overflow-hidden">
            {scene}
          </div>
        </main>

        {/* 底部输入框 */}
        <footer className="h-32 border-t border-[#333] bg-[#252526] shrink-0 p-4">
          {hand}
        </footer>
      </div>

      {/* 右侧 25% Agent 监控终端 */}
      <aside className="w-80 border-l border-[#333] bg-[#1e1e1e] shrink-0 flex flex-col">
        {sidebar}
      </aside>

      {/* D20 骰子遮罩 - AnimatePresence 确保彻底卸载 */}
      <AnimatePresence>
        {isRolling && <D20Dice />}
      </AnimatePresence>
    </div>
  );
}

export default GameLayout;
