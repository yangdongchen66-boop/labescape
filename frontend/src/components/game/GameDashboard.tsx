import React from 'react';
import { useGameStore } from '../../store/useGameStore';

/**
 * 游戏仪表盘组件
 * 
 * 显示三大进度条、主线任务、当前阶段等核心信息
 */
export const GameDashboard: React.FC = () => {
  const {
    gamePhase,
    risk,
    mentorMood,
    preparation,
    currentDay,
    companies,
    mainQuests,
    sideQuests,
  } = useGameStore();
  
  // 阶段名称
  const phaseNames: Record<string, string> = {
    PREP: '准备期',
    BREAKTHROUGH: '突破期',
    FINAL: '收官期',
  };

  // 计算求职进度
  const getJobProgress = () => {
    const totalStages = companies.length * 5; // 5个阶段 per company
    let completedStages = 0;
    companies.forEach(c => {
      switch (c.stage) {
        case 'apply': completedStages += 0; break;
        case 'written': completedStages += 1; break;
        case 'interview1': completedStages += 2; break;
        case 'interview2': completedStages += 3; break;
        case 'offer': completedStages += 5; break;
        case 'rejected': completedStages += 0; break;
      }
    });
    return Math.round((completedStages / totalStages) * 100);
  };

  // 计算导师关系进度（-100~100映射到0~100）
  const getMentorProgress = () => {
    return Math.round((mentorMood + 100) / 2);
  };

  // 获取风险颜色
  const getRiskColor = () => {
    if (risk >= 70) return 'bg-red-500';
    if (risk >= 50) return 'bg-yellow-500';
    return 'bg-green-500';
  };

  // 获取导师关系颜色
  const getMentorColor = () => {
    if (mentorMood >= 20) return 'bg-green-500';
    if (mentorMood >= -20) return 'bg-yellow-500';
    return 'bg-red-500';
  };

  return (
    <div className="bg-gray-900 border border-gray-700 rounded-lg p-4 space-y-4">
      {/* 顶部信息栏 */}
      <div className="flex justify-between items-center border-b border-gray-700 pb-3">
        <div className="flex items-center space-x-4">
          <span className="text-2xl font-bold text-white">
            Day {currentDay}/7
          </span>
          <span className={`px-2 py-1 rounded text-xs font-medium ${
            gamePhase === 'PREP' ? 'bg-blue-900 text-blue-200' :
            gamePhase === 'BREAKTHROUGH' ? 'bg-purple-900 text-purple-200' :
            'bg-red-900 text-red-200'
          }`}>
            {phaseNames[gamePhase]}
          </span>
        </div>
        <div className="text-sm text-gray-400">
          准备度: <span className="text-cyan-400 font-bold">{preparation}</span>/5
        </div>
      </div>

      {/* 三大进度条 */}
      <div className="space-y-3">
        {/* 求职进度 */}
        <div>
          <div className="flex justify-between text-sm mb-1">
            <span className="text-gray-300">求职进度</span>
            <span className="text-cyan-400">{getJobProgress()}%</span>
          </div>
          <div className="h-2 bg-gray-800 rounded-full overflow-hidden">
            <div
              className="h-full bg-gradient-to-r from-blue-500 to-cyan-400 transition-all duration-500"
              style={{ width: `${getJobProgress()}%` }}
            />
          </div>
          <div className="flex flex-wrap gap-2 mt-2">
            {companies.map(c => (
              <span
                key={c.name}
                className={`text-xs px-2 py-1 rounded ${
                  c.hasOffer ? 'bg-green-900 text-green-200' :
                  c.stage === 'rejected' ? 'bg-red-900 text-red-200' :
                  'bg-gray-800 text-gray-400'
                }`}
              >
                {c.name}: {c.stage === 'apply' ? '投递' :
                         c.stage === 'written' ? '笔试' :
                         c.stage === 'interview1' ? '一面' :
                         c.stage === 'interview2' ? '二面' :
                         c.stage === 'offer' ? 'Offer' : '被拒'}
              </span>
            ))}
          </div>
        </div>

        {/* 导师关系 */}
        <div>
          <div className="flex justify-between text-sm mb-1">
            <span className="text-gray-300">导师关系</span>
            <span className={mentorMood >= 0 ? 'text-green-400' : 'text-red-400'}>
              {mentorMood > 0 ? '+' : ''}{mentorMood}
            </span>
          </div>
          <div className="h-2 bg-gray-800 rounded-full overflow-hidden">
            <div
              className={`h-full ${getMentorColor()} transition-all duration-500`}
              style={{ width: `${getMentorProgress()}%` }}
            />
          </div>
          <div className="text-xs text-gray-500 mt-1">
            {mentorMood >= 20 ? '关系良好' :
             mentorMood >= -20 ? '关系一般' :
             mentorMood >= -50 ? '关系紧张' : '关系破裂'}
          </div>
        </div>

        {/* 风险指数 */}
        <div>
          <div className="flex justify-between text-sm mb-1">
            <span className="text-gray-300">风险指数</span>
            <span className={risk >= 70 ? 'text-red-400' : risk >= 50 ? 'text-yellow-400' : 'text-green-400'}>
              {risk}/100
            </span>
          </div>
          <div className="h-2 bg-gray-800 rounded-full overflow-hidden">
            <div
              className={`h-full ${getRiskColor()} transition-all duration-500`}
              style={{ width: `${risk}%` }}
            />
          </div>
          {risk >= 70 && (
            <div className="text-xs text-red-400 mt-1 animate-pulse">
              ⚠️ 高风险！导师可能突袭检查
            </div>
          )}
        </div>
      </div>

      {/* 主线任务 */}
      <div className="border-t border-gray-700 pt-3">
        <h3 className="text-sm font-medium text-gray-300 mb-2">主线任务</h3>
        <div className="space-y-1">
          {mainQuests.map(quest => (
            <div
              key={quest.id}
              className="flex items-center space-x-2 text-sm"
            >
              <span className={quest.isCompleted ? 'text-green-400' : 'text-gray-500'}>
                {quest.isCompleted ? '✓' : '○'}
              </span>
              <span className={quest.isCompleted ? 'text-gray-400 line-through' : 'text-gray-300'}>
                {quest.description}
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* 子任务 */}
      {sideQuests.some(q => !q.isCompleted) && (
        <div className="border-t border-gray-700 pt-3">
          <h3 className="text-sm font-medium text-gray-300 mb-2">子任务</h3>
          <div className="space-y-1">
            {sideQuests.filter(q => !q.isCompleted).map(quest => (
              <div
                key={quest.id}
                className="flex items-center justify-between text-sm"
              >
                <span className="text-gray-400">{quest.description}</span>
                <span className="text-xs text-cyan-400">
                  {quest.currentProgress}/{quest.requiredProgress}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default GameDashboard;
