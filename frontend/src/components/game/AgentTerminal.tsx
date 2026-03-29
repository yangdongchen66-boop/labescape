import { motion, AnimatePresence } from 'framer-motion';
import { useGameStore } from '../../store/useGameStore';
import { Terminal, CheckCircle, XCircle, Clock } from 'lucide-react';

/**
 * Agent 监控终端 - 简化版
 * 
 * @author Chrono Engine Team
 */

export function AgentTerminal() {
  const { agentLogs } = useGameStore();

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'success': return <CheckCircle className="w-3 h-3 text-green-400" />;
      case 'error': return <XCircle className="w-3 h-3 text-red-400" />;
      default: return <Clock className="w-3 h-3 text-yellow-400 animate-pulse" />;
    }
  };

  return (
    <div className="h-full flex flex-col bg-[#0d0d0d] relative overflow-hidden">
      {/* CRT 效果 */}
      <div className="absolute inset-0 pointer-events-none z-10">
        <div className="w-full h-full bg-[linear-gradient(rgba(18,16,16,0)_50%,rgba(0,0,0,0.25)_50%),linear-gradient(90deg,rgba(255,0,0,0.06),rgba(0,255,0,0.02),rgba(0,0,255,0.06))] bg-[length:100%_2px,3px_100%]" />
      </div>

      {/* 标题栏 */}
      <div className="h-8 bg-[#1a1a1a] border-b border-[#333] flex items-center px-3 shrink-0">
        <Terminal className="w-4 h-4 text-green-400 mr-2" />
        <span className="text-xs text-green-400 font-mono">Agent_Monitor.exe</span>
      </div>

      {/* 日志 */}
      <div className="flex-1 overflow-y-auto p-3 space-y-2 font-mono text-[11px]">
        <AnimatePresence initial={false}>
          {agentLogs.map((log) => (
            <motion.div
              key={log.id}
              className="flex items-start gap-2 p-2 rounded bg-[#1a1a1a] border border-[#2a2a2a]"
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
            >
              {getStatusIcon(log.status)}
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2">
                  <span className="font-bold text-blue-400">[{log.agentName}]</span>
                  <span className="text-gray-600">{new Date(log.time).toLocaleTimeString('zh-CN')}</span>
                </div>
                <div className="text-gray-400 mt-0.5">{log.action}</div>
              </div>
            </motion.div>
          ))}
        </AnimatePresence>
        
        {agentLogs.length === 0 && (
          <div className="text-center text-gray-700 py-8">
            <Terminal className="w-8 h-8 mx-auto mb-2 opacity-30" />
            <p className="text-[10px]">等待 Agent 活动...</p>
          </div>
        )}
      </div>
    </div>
  );
}

export default AgentTerminal;
