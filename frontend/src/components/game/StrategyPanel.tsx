import React from 'react';
import { useGameStore } from '../../store/useGameStore';
import { CheckCircle, AlertCircle } from 'lucide-react';

interface StrategySuggestion {
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
}

/**
 * 策略建议面板组件
 * 
 * 基于当前游戏状态提供AI策略建议，并显示完成状态
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
    strategySuggestion,
  } = useGameStore();

  // 解析后端传来的策略建议
  const parsedStrategy: StrategySuggestion | null = strategySuggestion ? {
    currentPhase: strategySuggestion.currentPhase || '',
    suggestions: strategySuggestion.suggestions || [],
    lastActionAnalysis: strategySuggestion.lastActionAnalysis,
    riskWarning: strategySuggestion.riskWarning || '',
    nextRecommended: strategySuggestion.nextRecommended || '',
  } : null;

  // 生成本地策略（作为后备）
  const generateLocalStrategy = () => {
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
      const hasOffer = companies.some(c => c.hasOffer);
      if (!hasOffer) {
        priorities.push('全力争取Offer');
      } else if (mentorMood < 0) {
        priorities.push('说服导师签字');
      } else {
        priorities.push('完成最终确认');
      }
    }

    if (risk >= 50) priorities.push('降低风险');
    if (preparation < 2) priorities.push('提升准备度');

    return { priorities, riskWarning, recommendedAction };
  };

  const localStrategy = generateLocalStrategy();

  // 获取建议类型图标
  const getSuggestionIcon = (type: string) => {
    switch (type) {
      case 'STUDY': return '📚';
      case 'SOCIAL': return '💬';
      case 'APPLY': return '📄';
      case 'INTERVIEW': return '🎯';
      case 'PERSUADE': return '🗣️';
      case 'REST': return '☕';
      default: return '💡';
    }
  };

  return (
    <div className="bg-gray-900 border border-gray-700 rounded-lg p-4">
      <h3 className="text-lg font-bold text-white mb-3 flex items-center">
        <span className="mr-2">💡</span>
        策略建议
        {parsedStrategy && (
          <span className="ml-2 text-xs font-normal text-cyan-400 bg-cyan-900/30 px-2 py-0.5 rounded">
            AI 生成
          </span>
        )}
      </h3>

      {/* 上轮行动分析 */}
      {parsedStrategy?.lastActionAnalysis && parsedStrategy.lastActionAnalysis.playerDid !== '无（游戏刚开始）' && (
        <div className={`rounded p-3 mb-3 ${
          parsedStrategy.lastActionAnalysis.isMatch 
            ? 'bg-green-900/30 border border-green-700' 
            : 'bg-yellow-900/30 border border-yellow-700'
        }`}>
          <div className="flex items-start gap-2">
            {parsedStrategy.lastActionAnalysis.isMatch ? (
              <CheckCircle className="w-4 h-4 text-green-400 mt-0.5 flex-shrink-0" />
            ) : (
              <AlertCircle className="w-4 h-4 text-yellow-400 mt-0.5 flex-shrink-0" />
            )}
            <div>
              <p className={`text-sm font-medium ${
                parsedStrategy.lastActionAnalysis.isMatch ? 'text-green-400' : 'text-yellow-400'
              }`}>
                {parsedStrategy.lastActionAnalysis.isMatch ? '✅ 建议目标达成' : '⚠️ 偏离建议目标'}
              </p>
              <p className="text-gray-400 text-xs mt-1">
                {parsedStrategy.lastActionAnalysis.feedback}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* 风险提示 */}
      {(parsedStrategy?.riskWarning || localStrategy.riskWarning) && (
        <div className="bg-red-900/30 border border-red-700 rounded p-3 mb-3">
          <p className="text-red-300 text-sm">
            {parsedStrategy?.riskWarning || localStrategy.riskWarning}
          </p>
        </div>
      )}

      {/* AI 策略建议列表 */}
      {parsedStrategy?.suggestions && parsedStrategy.suggestions.length > 0 ? (
        <div className="mb-4">
          <h4 className="text-sm font-medium text-gray-400 mb-2">当前优先级：</h4>
          <ol className="space-y-2">
            {parsedStrategy.suggestions
              .sort((a, b) => a.priority - b.priority)
              .map((suggestion, index) => (
              <li
                key={suggestion.id}
                className={`flex items-start p-2 rounded ${
                  suggestion.isCompleted 
                    ? 'bg-green-900/20 border border-green-700/50' 
                    : 'bg-gray-800/50 border border-gray-700'
                }`}
              >
                <span className="w-6 h-6 rounded-full bg-cyan-900 text-cyan-400 text-xs flex items-center justify-center mr-2 flex-shrink-0">
                  {index + 1}
                </span>
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    <span className="text-lg">{getSuggestionIcon(suggestion.type)}</span>
                    <span className={`text-sm font-medium ${
                      suggestion.isCompleted ? 'text-green-400 line-through' : 'text-gray-300'
                    }`}>
                      {suggestion.title}
                    </span>
                    {suggestion.isCompleted && (
                      <CheckCircle className="w-4 h-4 text-green-400" />
                    )}
                  </div>
                  <p className="text-gray-500 text-xs mt-1">{suggestion.description}</p>
                  {suggestion.isCompleted && suggestion.completionReason && (
                    <p className="text-green-500/70 text-xs mt-1">
                      ✓ {suggestion.completionReason}
                    </p>
                  )}
                </div>
              </li>
            ))}
          </ol>
        </div>
      ) : (
        /* 本地生成的策略（后备） */
        <div className="mb-4">
          <h4 className="text-sm font-medium text-gray-400 mb-2">当前优先级：</h4>
          <ol className="space-y-1">
            {localStrategy.priorities.map((priority, index) => (
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
      )}

      {/* 推荐行动 */}
      <div className="bg-cyan-900/20 border border-cyan-700/50 rounded p-3">
        <h4 className="text-sm font-medium text-cyan-400 mb-1">推荐行动</h4>
        <p className="text-white font-medium">
          {parsedStrategy?.suggestions?.find(s => s.id === parsedStrategy.nextRecommended)?.title 
            || localStrategy.recommendedAction 
            || '根据当前状态选择行动'}
        </p>
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
