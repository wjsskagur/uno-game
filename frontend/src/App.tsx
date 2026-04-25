// App.tsx
import React from 'react';
import { useGameStore } from './store/gameStore';
import { useWebSocket } from './hooks/useWebSocket';
import { LobbyPage } from './pages/LobbyPage';
import { WaitingRoom } from './pages/WaitingRoom';
import { GameBoard } from './pages/GameBoard';

function App() {
  const { playerId, roomId, gameState } = useGameStore();

  // Bug fix 2: useWebSocket을 App에서 한 번만 호출
  // GameBoard에서도 호출하면 WebSocket 연결이 2개 생겨 메시지 중복 수신
  const { playCard, drawCard, callUno } = useWebSocket();

  if (!playerId || !roomId) {
    return <LobbyPage onEnter={() => {}} />;
  }

  if (!gameState || gameState.phase === 'WAITING') {
    return <WaitingRoom />;
  }

  return <GameBoard playCard={playCard} drawCard={drawCard} callUno={callUno} />;
}

export default App;
