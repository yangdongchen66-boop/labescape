import React from 'react';
import { useGameStore } from '../../store/useGameStore';

/**
 * 策略建议面板组件
 * 
 * 基于当前游戏状态提供AI策略建议
 */
export const StrategyPanel: React.FC = () => {
  const {
    gamePhase,
    risk,
    mentorMood,
    preparation,
    currentDay,
    companies,
    sideQuests,
  } = useGameStore();

  // 生成策略建议
  const generateStrategy = () => {
    const priorities: string[] = [];
    let riskWarning = '';
    let recommendedAction = '';

    // 风险警告
    if (risk >= 70) {
      riskWarning = '⚠️ 风险极高！导师可能随时突袭，建议立即降低风险。';
    } else if (risk >= 50) {
      riskWarning = '⚡ 风险较高，注意控制行为。';
    }

    // 根据阶段生成优先级
    if (gamePhase === 'PREP') {
      // 准备期：刷题、社交
      if (!sideQuests.find(q => q.id === 'study_1')?.isCompleted) {
        priorities.push('刷题提升准备度');
      }
      if (!sideQuests.find(q => q.id === 'social_1')?.isCompleted) {
        priorities.push('与师兄交流获取信息');
      }
      if (companies.every(c => c.stage === 'apply')) {
        priorities.push('投递简历开启流程');
      }
    } else if (gamePhase === 'BREAKTHROUGH') {
      // 突破期：推进面试
      const activeCompany = companies.find(c => !c.isCompleted && c.stage !== 'rejected');
      if (activeCompany) {
        if (activeCompany.stage === 'written') {
          priorities.push(`准备${activeCompany.name}笔试`);
        } else if (activeCompany.stage.startsWith('interview')) {
          priorities.push(`准备${activeCompany.name}面试`);
        }
      }
      if (mentorMood < 0) {
        priorities.push('改善导师关系');
      }
    } else {
      // 收官期：冲刺Offer和签字
      const hasOffer = companies.some(c => c.hasOffer);
      if (!hasOffer) {
        priorities.push('全力争取Offer');
      } else if (mentorMood < 0) {
        priorities.push('说服导师签字');
      } else {
        priorities.push('完成最终确认');
      }
    }

    // 通用建议
    if (risk >= 50) {
      priorities.push('降低风险');
    }
    if (preparation < 2) {
      priorities.push('提升准备度');
    }

    // 推荐行动
    if (risk >= 70) {
      recommendedAction = '先休息恢复，避免进一步激怒导师';
    } else if (!sideQuests.find(q => q.id === 'study_1')?.isCompleted && preparation < 3) {
      recommendedAction = '刷题提升准备度';
    } else if (companies.every(c => c.stage === 'apply')) {
      recommendedAction = '投递简历开启求职流程';
    } else {
      const activeCompany = companies.find(c => !c.isCompleted && c.stage !== 'rejected');
      if (activeCompany) {
        if (activeCompany.stage === 'written') {
          recommendedAction = `参加${activeCompany.name}笔试`;
        } else if (activeCompany.stage.startsWith('interview')) {
          recommendedAction = `准备${activeCompany.name}面试`;
        } else {
          recommendedAction = '与导师沟通争取支持';
        }
      } else {
        recommendedAction = '寻找其他机会';
      }
    }

    return { priorities, riskWarning, recommendedAction };
  };

  const strategy = generateStrategy();

  return (
    <div className="bg-gray-900 border border-gray-700 rounded-lg p-4">
      <h3 className="text-lg font-bold text-white mb-3 flex items-center">
        <span className="mr-2">💡</span>
        策略建议
      </h3>

      {/* 风险提示 */}
      {strategy.riskWarning && (
        <div className="bg-red-900/30 border border-red-700 rounded p-3 mb-3">
          <p className="text-red-300 text-sm">{strategy.riskWarning}</p>
        </div>
      )}

      {/* 优先级列表 */}
      <div className="mb-4">
        <h4 className="text-sm font-medium text-gray-400 mb-2">当前优先级：</h4>
        <ol className="space-y-1">
          {strategy.priorities.map((priority, index) => (
            <li
              key={index}
              className="flex items-center text-sm text-gray-300"
            >
              <span className="w-5 h-5 rounded-full bg-cyan-900 text-cyan-400 text-xs flex items-center justify-center mr-2">
                {index + 1}
              </span>
              {priority}
            </li>
          ))}
        </ol>
      </div>

      {/* 推荐行动 */}
      <div className="bg-cyan-900/20 border border-cyan-700/50 rounded p-3">
        <h4 className="text-sm font-medium text-cyan-400 mb-1">推荐行动</h4>
        <p className="text-white font-medium">{strategy.recommendedAction}</p>
      </div>

      {/* 状态摘要 */}
      <div className="mt-4 pt-3 border-t border-gray-700 grid grid-cols-3 gap-2 text-center">
        <div>
          <div className="text-xs text-gray-500">阶段</div>
          <div className="text-sm font-medium text-white">
            {gamePhase === 'PREP' ? '准备期' :
             gamePhase === 'BREAKTHROUGH' ? '突破期' : '收官期'}
          </div>
        </div>
        <div>
          <div className="text-xs text-gray-500">剩余天数</div>
          <div className="text-sm font-medium text-white">{8 - currentDay}天</div>
        </div>
        <div>
          <div className="text-xs text-gray-500">准备度</div>
          <div className={`text-sm font-medium ${preparation >= 3 ? 'text-green-400' : 'text-yellow-400'}`}>
            {preparation}/5
          </div>
        </div>
      </div>
    </div>
  );
};

export default StrategyPanel;
