// pages/WaitingRoom.tsx
import React from 'react';
import axios from 'axios';
import { useGameStore } from '../store/gameStore';

const API = 'http://localhost:8080/api';

export const WaitingRoom: React.FC = () => {
  const { roomId, playerId, gameState } = useGameStore();
  const isHost = gameState?.players[0]?.id === playerId;

  const handleStart = async () => {
    await axios.post(`${API}/rooms/${roomId}/start`);
  };

  const copyRoomCode = () => {
    navigator.clipboard.writeText(roomId ?? '');
  };

  return (
    <div style={{
      minHeight: '100vh',
      background: 'linear-gradient(135deg, #0f0c29, #302b63, #24243e)',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
    }}>
      <div style={{
        backgroundColor: 'rgba(255,255,255,0.05)',
        backdropFilter: 'blur(20px)',
        borderRadius: 24, padding: 48, width: 440,
        border: '1px solid rgba(255,255,255,0.1)',
      }}>
        <h2 style={{ color: '#fff', textAlign: 'center', margin: '0 0 8px' }}>
          대기실
        </h2>

        {/* 방 코드 */}
        <div style={{
          backgroundColor: 'rgba(255,255,255,0.1)',
          borderRadius: 12, padding: '16px 24px',
          textAlign: 'center', marginBottom: 28, cursor: 'pointer',
        }} onClick={copyRoomCode}>
          <p style={{ color: 'rgba(255,255,255,0.5)', margin: '0 0 4px', fontSize: 12 }}>
            방 코드 (클릭해서 복사)
          </p>
          <p style={{ color: '#fdd835', margin: 0, fontSize: 32, fontWeight: 900, letterSpacing: 6 }}>
            {roomId}
          </p>
        </div>

        {/* 플레이어 목록 */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10, marginBottom: 28 }}>
          {gameState?.players.map((p, i) => (
            <div key={p.id} style={{
              display: 'flex', alignItems: 'center', gap: 12,
              backgroundColor: 'rgba(255,255,255,0.08)',
              borderRadius: 10, padding: '12px 16px',
              border: p.id === playerId ? '1px solid rgba(253,216,53,0.4)' : '1px solid transparent',
            }}>
              <span style={{ fontSize: 24 }}>{i === 0 ? '👑' : '🧑'}</span>
              <span style={{ color: '#fff', fontWeight: 600 }}>{p.nickname}</span>
              {i === 0 && (
                <span style={{ marginLeft: 'auto', color: '#fdd835', fontSize: 11, fontWeight: 600 }}>
                  HOST
                </span>
              )}
              {p.id === playerId && (
                <span style={{ marginLeft: i !== 0 ? 'auto' : 8, color: 'rgba(255,255,255,0.4)', fontSize: 11 }}>
                  (나)
                </span>
              )}
            </div>
          ))}
          {/* 빈 슬롯 */}
          {Array.from({ length: 4 - (gameState?.players.length ?? 0) }).map((_, i) => (
            <div key={i} style={{
              borderRadius: 10, padding: '12px 16px',
              border: '1px dashed rgba(255,255,255,0.15)',
              color: 'rgba(255,255,255,0.3)', textAlign: 'center', fontSize: 13,
            }}>
              플레이어 대기 중...
            </div>
          ))}
        </div>

        <p style={{ color: 'rgba(255,255,255,0.4)', textAlign: 'center', fontSize: 13, margin: '0 0 20px' }}>
          {gameState?.players.length ?? 0}/4명 참가 (최소 2명)
        </p>

        {isHost ? (
          <button
            onClick={handleStart}
            disabled={(gameState?.players.length ?? 0) < 2}
            style={{
              width: '100%', padding: '14px 0', borderRadius: 12,
              background: (gameState?.players.length ?? 0) >= 2
                ? 'linear-gradient(135deg, #43a047, #1b5e20)'
                : 'rgba(255,255,255,0.1)',
              color: '#fff', border: 'none', fontSize: 16,
              fontWeight: 700, cursor: (gameState?.players.length ?? 0) >= 2 ? 'pointer' : 'not-allowed',
            }}
          >
            게임 시작
          </button>
        ) : (
          <p style={{ color: 'rgba(255,255,255,0.5)', textAlign: 'center', margin: 0 }}>
            방장이 게임을 시작하기를 기다리는 중...
          </p>
        )}
      </div>
    </div>
  );
};
