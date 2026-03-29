import { AnimatePresence, motion } from 'framer-motion';
import { Scroll, Hourglass, CheckCircle, AlertCircle, Sparkles, BookOpen } from 'lucide-react';
import { useGameStore } from '../../store/useGameStore';

// Fantasy RPG themed status styles
const statusStyles = {
  pending: {
    label: '运转中',
    icon: Hourglass,
    tone: 'text-blue-300',
    glow: 'shadow-[0_0_15px_rgba(100,149,237,0.2)]',
    border: 'border-blue-700/40',
    bg: 'bg-blue-900/10',
  },
  success: {
    label: '已完成',
    icon: CheckCircle,
    tone: 'text-amber-300',
    glow: 'shadow-[0_0_15px_rgba(180,140,80,0.2)]',
    border: 'border-amber-700/40',
    bg: 'bg-amber-900/10',
  },
  error: {
    label: '异常',
    icon: AlertCircle,
    tone: 'text-red-300',
    glow: 'shadow-[0_0_15px_rgba(200,80,80,0.2)]',
    border: 'border-red-700/40',
    bg: 'bg-red-900/10',
  },
} as const;

// Agent name mapping to Chinese fantasy roles
const agentNameMap: Record<string, { name: string; color: string; icon: typeof Sparkles }> = {
  'Manager': { name: '调度者', color: 'text-blue-400', icon: BookOpen },
  'Supervisor': { name: '命运裁判', color: 'text-amber-400', icon: Sparkles },
  'Executor-NPC': { name: '角色化身', color: 'text-emerald-400', icon: Scroll },
  'Director': { name: '幻境织工', color: 'text-purple-400', icon: Sparkles },
  'Vector-7': { name: '星象师', color: 'text-blue-400', icon: BookOpen },
  'Ghostline': { name: '影行者', color: 'text-purple-400', icon: Scroll },
  'Judge': { name: '命运裁决', color: 'text-rose-400', icon: Sparkles },
  'Event': { name: '事件触发', color: 'text-cyan-400', icon: Scroll },
};

const formatLogTime = (value: string) =>
  new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  }).format(new Date(value));

/**
 * DM's Secret Screen - Fantasy RPG style system log panel
 * Deep ink blue/purple background with ink-fade-in animation
 * Chinese fantasy themed agent names and logs
 */
export function DMSecretScreen() {
  const agentLogs = useGameStore((state) => state.agentLogs);

  return (
    <section 
      className="flex h-full min-h-[30vh] flex-col px-4 py-4 lg:px-5 lg:py-5"
      style={{
        background: 'linear-gradient(180deg, #0a0a12 0%, #050508 100%)',
      }}
    >
      {/* Header */}
      <div className="mb-4 flex items-center justify-between border-b border-indigo-800/30 pb-3">
        <div>
          <p className="text-[10px] tracking-[0.2em] text-indigo-500/70 font-serif">幕后运算</p>
          <h2 className="mt-1 flex items-center gap-2 text-base font-bold tracking-wide text-indigo-200 font-serif">
            <Scroll className="h-4 w-4 text-amber-600" />
            命运法则运转日志
          </h2>
        </div>
        <div 
          className="rounded border border-amber-700/30 bg-amber-900/20 px-2 py-1 text-[10px] tracking-[0.15em] text-amber-400/80 font-serif"
        >
          进行中
        </div>
      </div>

      {/* Log container */}
      <div 
        className="relative flex-1 overflow-hidden rounded-lg border border-indigo-800/20 p-3"
        style={{
          background: 'linear-gradient(180deg, rgba(20, 20, 35, 0.8) 0%, rgba(10, 10, 18, 0.9) 100%)',
          boxShadow: 'inset 0 1px 0 rgba(255,255,255,0.03), 0 0 40px rgba(30, 30, 60, 0.3)',
        }}
      >
        {/* Subtle mystical pattern */}
        <div 
          className="pointer-events-none absolute inset-0 opacity-[0.02]"
          style={{
            backgroundImage: `radial-gradient(circle at 20% 50%, rgba(100, 100, 200, 0.3) 0%, transparent 50%),
                              radial-gradient(circle at 80% 80%, rgba(180, 140, 80, 0.2) 0%, transparent 40%)`,
          }}
        />

        <div className="relative flex h-full flex-col justify-end overflow-hidden">
          <div className="space-y-2 overflow-y-auto pr-1">
            <AnimatePresence initial={false}>
              {agentLogs.map((log) => {
                const style = statusStyles[log.status];
                const StatusIcon = style.icon;
                const agentInfo = agentNameMap[log.agentName] || { 
                  name: log.agentName, 
                  color: 'text-stone-400',
                  icon: Scroll 
                };
                const AgentIcon = agentInfo.icon;

                return (
                  <motion.article
                    key={log.id}
                    layout
                    initial={{ opacity: 0, y: 15 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: -8 }}
                    transition={{ duration: 0.35, ease: [0.16, 1, 0.3, 1] }}
                    className={[
                      'rounded-lg border p-3',
                      style.border,
                      style.bg,
                      style.glow,
                    ].join(' ')}
                  >
                    <div className="mb-2 flex items-start justify-between gap-3">
                      <div className="flex items-center gap-2">
                        <AgentIcon className={`h-3.5 w-3.5 ${agentInfo.color}`} />
                        <div>
                          <p className="text-[10px] tracking-wider text-indigo-500/60 font-serif">
                            {formatLogTime(log.time)}
                          </p>
                          <h3 className={`mt-0.5 text-xs font-bold tracking-wide ${agentInfo.color} font-serif`}>
                            {agentInfo.name}
                          </h3>
                        </div>
                      </div>

                      <div className={`flex items-center gap-1.5 text-[10px] tracking-wider ${style.tone} font-serif`}>
                        <StatusIcon className="h-3 w-3" />
                        <span>{style.label}</span>
                      </div>
                    </div>

                    <p className="text-sm leading-relaxed text-indigo-100/80 font-serif pl-5 border-l border-indigo-800/30">
                      {log.action}
                      {log.status === 'pending' && (
                        <motion.span
                          aria-hidden
                          className="ml-1 inline-block text-blue-400"
                          animate={{ opacity: [1, 0.2, 1] }}
                          transition={{ duration: 1, repeat: Infinity, ease: 'easeInOut' }}
                        >
                          ...
                        </motion.span>
                      )}
                    </p>
                  </motion.article>
                );
              })}
            </AnimatePresence>
          </div>
        </div>
      </div>

      {/* Bottom decorative element */}
      <div className="mt-3 flex items-center justify-center gap-2">
        <div className="h-px w-16 bg-gradient-to-r from-transparent to-indigo-800/50" />
        <span className="text-[10px] tracking-[0.3em] text-indigo-700/50 font-serif">守秘者之眼注视着你</span>
        <div className="h-px w-16 bg-gradient-to-l from-transparent to-indigo-800/50" />
      </div>
    </section>
  );
}

export default DMSecretScreen;
