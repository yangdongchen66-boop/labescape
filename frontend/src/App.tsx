import { GameLayout } from './components/layout/GameLayout';
import { StudentProfileHUD } from './components/game/StudentProfileHUD';
import { NarrativeStream } from './components/game/NarrativeStream';
import { ActionInput } from './components/game/ActionInput';
import { AgentTerminal } from './components/game/AgentTerminal';

/**
 * Chrono-Agents: 研二求生指南
 * 
 * 主应用组件
 * 
 * @author Chrono Engine Team
 */

function App() {
  return (
    <GameLayout
      header={<StudentProfileHUD />}
      scene={<NarrativeStream />}
      hand={<ActionInput />}
      sidebar={<AgentTerminal />}
    />
  );
}

export default App;
