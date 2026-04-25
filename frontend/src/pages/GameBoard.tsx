// pages/GameBoard.tsx
import React from 'react';
import { useGameStore } from '../store/gameStore';
import { UnoCard } from '../components/UnoCard';
import { ColorPicker } from '../components/ColorPicker';
import { Card, CardColor } from '../types/game';

const COLOR_MAP: Record<CardColor, string> = {
  RED: '#e53935', BLUE: '#1e88e5', GREEN: '#43a047',
  YELLOW: '#fdd835', WILD: '#7c4dff',
};

// playCard, drawCard, callUno를 props로 받음
// useWebSocket은 App.tsx에서 한 번만 호출 → 중복 WebSocket 연결 방지
interface GameBoardProps {
  playCard: (card: Card, declaredColor?: CardColor) => void;
  drawCard: () => void;
  callUno: () => void;
}

export const GameBoard: React.FC<GameBoardProps> = ({ playCard, drawCard, callUno }) => {
  const {
    playerId, gameState, myHand, error,
    selectedCard, showColorPicker,
    selectCard, setShowColorPicker,
  } = useGameStore();

  if (!gameState) return null;

  const isMyTurn = gameState.currentPlayerId === playerId;
  const others = gameState.players.filter(p => p.id !== playerId);

  // Bug fix 1: 객체 참조 비교 대신 카드 내용으로 비교
  const isSameCard = (a: Card | null, b: Card): boolean => {
    if (!a) return false;
    return a.color === b.color && a.type === b.type && a.number === b.number;
  };

  const handleCardClick = (card: Card) => {
    if (!isMyTurn) return;
    if (isSameCard(selectedCard, card)) {
      if (card.type === 'WILD' || card.type === 'WILD_DRAW_FOUR') {
        setShowColorPicker(true);
      } else {
        playCard(card);
        selectCard(null);
      }
    } else {
      selectCard(card);
    }
  };

  const handleColorSelect = (color: CardColor) => {
    if (selectedCard) {
      playCard(selectedCard, color);
      selectCard(null);
    }
    setShowColorPicker(false);
  };

  // 게임 종료
  if (gameState.phase === 'FINISHED') {
    const isWinner = gameState.winnerId === playerId;
    return (
      <div style={{
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #0f0c29, #302b63)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
      }}>
        <div style={{ textAlign: 'center', color: '#fff' }}>
          <div style={{ fontSize: 80, marginBottom: 16 }}>{isWinner ? '🏆' : '😢'}</div>
          <h1 style={{ fontSize: 48, margin: '0 0 8px' }}>
            {isWinner ? '우승!' : '패배'}
          </h1>
          <p style={{ color: 'rgba(255,255,255,0.6)', fontSize: 20 }}>
            {gameState.winnerNickname}님이 우승했습니다
          </p>
          <button
            onClick={() => window.location.reload()}
            style={{
              marginTop: 32, padding: '14px 40px', borderRadius: 12,
              background: 'linear-gradient(135deg, #e53935, #e91e63)',
              color: '#fff', border: 'none', fontSize: 18, fontWeight: 700, cursor: 'pointer',
            }}
          >
            다시 하기
          </button>
        </div>
      </div>
    );
  }

  return (
    <div style={{
      minHeight: '100vh',
      background: 'linear-gradient(135deg, #0a0a1a, #1a1a3e)',
      display: 'flex', flexDirection: 'column',
      userSelect: 'none',
    }}>
      {/* 상단: 다른 플레이어들 */}
      <div style={{
        display: 'flex', justifyContent: 'center', gap: 32,
        padding: '16px 24px',
      }}>
        {others.map((p) => (
          <div key={p.id} style={{
            backgroundColor: 'rgba(255,255,255,0.05)',
            borderRadius: 16, padding: '12px 20px',
            border: gameState.currentPlayerId === p.id
              ? '2px solid #fdd835'
              : '2px solid transparent',
            textAlign: 'center', minWidth: 100,
          }}>
            <div style={{ fontSize: 28, marginBottom: 4 }}>🧑</div>
            <div style={{ color: '#fff', fontWeight: 600, fontSize: 13 }}>{p.nickname}</div>
            <div style={{ color: gameState.currentPlayerId === p.id ? '#fdd835' : 'rgba(255,255,255,0.4)', fontSize: 12 }}>
              {gameState.currentPlayerId === p.id ? '⬆ 이 사람 차례' : `카드 ${p.handSize}장`}
            </div>
            {p.calledUno && (
              <div style={{ color: '#e53935', fontWeight: 900, fontSize: 11, marginTop: 2 }}>UNO!</div>
            )}
          </div>
        ))}
      </div>

      {/* 중앙: 게임판 */}
      <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 48 }}>
        <div style={{
          width: 60, height: 60, borderRadius: '50%',
          backgroundColor: COLOR_MAP[gameState.currentColor],
          border: '4px solid rgba(255,255,255,0.3)',
          boxShadow: `0 0 30px ${COLOR_MAP[gameState.currentColor]}80`,
        }} />

        <div>
          <p style={{ color: 'rgba(255,255,255,0.4)', textAlign: 'center', margin: '0 0 8px', fontSize: 12 }}>
            버린 카드
          </p>
          {gameState.topCard && <UnoCard card={gameState.topCard} size="lg" />}
        </div>

        <div
          onClick={isMyTurn ? drawCard : undefined}
          style={{ cursor: isMyTurn ? 'pointer' : 'not-allowed' }}
        >
          <p style={{ color: 'rgba(255,255,255,0.4)', textAlign: 'center', margin: '0 0 8px', fontSize: 12 }}>
            덱 ({gameState.deckSize}장)
          </p>
          <div style={{
            width: 90, height: 135, borderRadius: 10,
            background: 'linear-gradient(135deg, #1a237e, #283593)',
            border: isMyTurn ? '3px solid #fdd835' : '3px solid rgba(255,255,255,0.2)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            boxShadow: isMyTurn ? '0 0 20px rgba(253,216,53,0.4)' : '0 4px 12px rgba(0,0,0,0.4)',
            transition: 'all 0.2s',
          }}>
            <span style={{ fontSize: 32 }}>🂠</span>
          </div>
        </div>

        <div style={{ color: 'rgba(255,255,255,0.5)', fontSize: 32 }}>
          {gameState.direction === 'CLOCKWISE' ? '↻' : '↺'}
        </div>
      </div>

      {/* 하단 액션 바 */}
      <div style={{ padding: '8px 24px', display: 'flex', justifyContent: 'center', gap: 12 }}>
        <div style={{
          backgroundColor: isMyTurn ? 'rgba(253,216,53,0.15)' : 'rgba(255,255,255,0.05)',
          borderRadius: 12, padding: '8px 20px',
          border: isMyTurn ? '1px solid rgba(253,216,53,0.4)' : '1px solid rgba(255,255,255,0.1)',
        }}>
          <span style={{ color: isMyTurn ? '#fdd835' : 'rgba(255,255,255,0.5)', fontWeight: 600, fontSize: 14 }}>
            {isMyTurn ? '⬆ 내 차례!' : `${gameState.currentPlayerNickname} 차례`}
          </span>
        </div>

        {myHand.length === 1 && (
          <button onClick={callUno} style={{
            padding: '8px 20px', borderRadius: 12,
            background: 'linear-gradient(135deg, #e53935, #c62828)',
            color: '#fff', border: 'none', fontWeight: 900,
            fontSize: 16, cursor: 'pointer', letterSpacing: 2,
          }}>
            UNO!
          </button>
        )}
      </div>

      <div style={{ textAlign: 'center', padding: '4px 24px 8px' }}>
        <span style={{ color: 'rgba(255,255,255,0.35)', fontSize: 12 }}>
          {gameState.lastAction}
        </span>
      </div>

      {/* 내 손패 */}
      <div style={{ padding: '12px 24px 24px', borderTop: '1px solid rgba(255,255,255,0.05)' }}>
        <p style={{ color: 'rgba(255,255,255,0.4)', margin: '0 0 10px', fontSize: 12, textAlign: 'center' }}>
          내 손패 ({myHand.length}장) {selectedCard ? '— 카드를 다시 클릭하면 냅니다' : '— 카드를 클릭해 선택'}
        </p>
        <div style={{
          display: 'flex', justifyContent: 'center',
          flexWrap: 'wrap', gap: 8, maxWidth: '100%',
        }}>
          {myHand.map((card, i) => (
            <UnoCard
              key={i}
              card={card}
              size="md"
              selected={isSameCard(selectedCard, card)}
              disabled={!isMyTurn}
              onClick={() => handleCardClick(card)}
            />
          ))}
        </div>
      </div>

      {error && (
        <div style={{
          position: 'fixed', top: 24, left: '50%', transform: 'translateX(-50%)',
          backgroundColor: '#e53935', color: '#fff',
          padding: '12px 24px', borderRadius: 10, fontWeight: 600,
          boxShadow: '0 4px 20px rgba(0,0,0,0.4)', zIndex: 200,
        }}>
          {error}
        </div>
      )}

      {showColorPicker && <ColorPicker onSelect={handleColorSelect} />}
    </div>
  );
};
