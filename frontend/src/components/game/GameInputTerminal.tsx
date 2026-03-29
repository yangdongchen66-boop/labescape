import { useState, useRef, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Sword, Dices } from 'lucide-react';
import { useGameStore } from '../../store/useGameStore';

/**
 * Fantasy RPG Action Input Box - Parchment-styled input for tabletop gaming
 * Features: sword icon prefix, auto-expanding textarea, magical glow on ROLL button
 */
export function GameInputTerminal() {
  const [input, setInput] = useState('');
  const [isFocused, setIsFocused] = useState(false);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const submitPlayerAction = useGameStore((state) => state.submitPlayerAction);

  // Auto-resize textarea based on content
  const handleInputChange = useCallback((e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const value = e.target.value;
    setInput(value);

    // Auto-resize logic
    const textarea = textareaRef.current;
    if (textarea) {
      textarea.style.height = 'auto';
      const newHeight = Math.min(textarea.scrollHeight, 72); // Max 3 lines (~72px)
      textarea.style.height = `${newHeight}px`;
    }
  }, []);

  // Handle submit
  const handleSubmit = useCallback(() => {
    const trimmed = input.trim();
    if (!trimmed) return;

    submitPlayerAction(trimmed);
    setInput('');

    // Reset textarea height
    const textarea = textareaRef.current;
    if (textarea) {
      textarea.style.height = 'auto';
    }
  }, [input, submitPlayerAction]);

  // Handle keydown - Enter to submit, Shift+Enter for new line
  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        handleSubmit();
      }
    },
    [handleSubmit]
  );

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, ease: [0.16, 1, 0.3, 1], delay: 0.2 }}
      className="h-full flex flex-col"
    >
      {/* Main input container - Card game hand style */}
      <div
        className="flex-1 relative px-4 py-3 lg:px-6 lg:py-4 rounded-lg"
        style={{
          background: 'linear-gradient(180deg, rgba(28, 25, 23, 0.8) 0%, rgba(15, 13, 10, 0.9) 100%)',
          border: '1px solid rgba(139, 119, 89, 0.25)',
          boxShadow: 'inset 0 2px 10px rgba(0, 0, 0, 0.5), 0 2px 10px rgba(0, 0, 0, 0.3)',
        }}
      >
        {/* Parchment texture overlay */}
        <div
          className="pointer-events-none absolute inset-0 opacity-[0.02]"
          style={{
            backgroundImage: `repeating-linear-gradient(0deg, transparent, transparent 2px, rgba(139, 119, 89, 0.5) 2px, rgba(139, 119, 89, 0.5) 4px)`,
          }}
        />

        <div className="relative flex items-end gap-3">
          {/* Sword prefix icon */}
          <div className="flex shrink-0 items-center gap-2 pb-2">
            <div className="flex h-8 w-8 items-center justify-center rounded bg-amber-900/30 border border-amber-700/40">
              <Sword className="h-4 w-4 text-amber-500" />
            </div>
            <span className="font-serif text-sm tracking-wider text-amber-600/80">
              &gt;&gt;&gt;
            </span>
          </div>

          {/* Auto-expanding textarea */}
          <div className="relative flex-1">
            <textarea
              ref={textareaRef}
              value={input}
              onChange={handleInputChange}
              onKeyDown={handleKeyDown}
              onFocus={() => setIsFocused(true)}
              onBlur={() => setIsFocused(false)}
              placeholder="描述你的下一步行动，或说出的台词..."
              rows={1}
              className="w-full resize-none bg-transparent font-serif text-[15px] text-amber-100 placeholder:text-amber-800/40 border-none outline-none min-h-[28px] max-h-[72px] py-1"
              style={{
                textShadow: isFocused ? '0 0 10px rgba(180, 140, 80, 0.2)' : 'none',
              }}
            />

            {/* Focus glow effect */}
            <AnimatePresence>
              {isFocused && (
                <motion.div
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  exit={{ opacity: 0 }}
                  className="pointer-events-none absolute -inset-2 rounded-lg bg-amber-400/5"
                />
              )}
            </AnimatePresence>
          </div>

          {/* ROLL button with magical glow */}
          <motion.button
            onClick={handleSubmit}
            disabled={!input.trim()}
            whileHover={input.trim() ? { scale: 1.02 } : {}}
            whileTap={input.trim() ? { scale: 0.98 } : {}}
            className={[
              'relative shrink-0 overflow-hidden rounded-lg px-5 py-2.5',
              'font-serif text-sm font-bold tracking-wider',
              'transition-all duration-300',
              input.trim()
                ? [
                    'bg-gradient-to-r from-red-900/80 to-amber-900/80',
                    'text-amber-100',
                    'border border-amber-600/50',
                    'hover:shadow-[0_0_20px_rgba(180,140,80,0.4)]',
                    'hover:border-amber-500/70',
                    'hover:from-red-800/80 hover:to-amber-800/80',
                  ].join(' ')
                : [
                    'bg-stone-800/50 text-stone-600',
                    'border border-stone-700/30',
                    'cursor-not-allowed',
                  ].join(' '),
            ].join(' ')}
          >
            {/* Magical shimmer animation */}
            {input.trim() && (
              <motion.div
                className="absolute inset-0 bg-gradient-to-r from-transparent via-amber-400/20 to-transparent"
                animate={{ x: ['-100%', '100%'] }}
                transition={{ duration: 2, repeat: Infinity, ease: 'linear' }}
              />
            )}
            <span className="relative z-10 flex items-center gap-2">
              <Dices className="h-4 w-4" />
              掷骰行动
            </span>
          </motion.button>
        </div>

        {/* Bottom hint line */}
        <div className="mt-2 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <span className="text-[10px] tracking-wider text-stone-600 font-serif">
              <span className="text-amber-700/60">◈</span> 命运之轮转动中
            </span>
            <motion.span
              className="text-[10px] tracking-wider text-emerald-700/70 font-serif"
              animate={{ opacity: [0.5, 1, 0.5] }}
              transition={{ duration: 2, repeat: Infinity }}
            >
              ● 与守秘者连接稳定
            </motion.span>
          </div>
          <span className="font-serif text-[10px] text-stone-600">
            {input.length}/500
          </span>
        </div>
      </div>
    </motion.div>
  );
}

export default GameInputTerminal;
