// pages/LobbyPage.tsx
import React, { useState } from 'react';
import axios from 'axios';
import { useGameStore } from '../store/gameStore';

const API = 'http://localhost:8080/api';

export const LobbyPage: React.FC<{ onEnter: () => void }> = ({ onEnter }) => {
  const [nickname, setNickname] = useState('');
  const [roomId, setRoomId] = useState('');
  const [mode, setMode] = useState<'create' | 'join'>('create');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { setPlayerInfo } = useGameStore();

  const handleCreate = async () => {
    if (!nickname.trim()) return setError('닉네임을 입력해주세요');
    setLoading(true);
    try {
      const res = await axios.post(`${API}/rooms`, { nickname });
      setPlayerInfo(res.data.playerId, res.data.roomId, nickname);
      onEnter();
    } catch {
      setError('방 생성에 실패했습니다');
    } finally {
      setLoading(false);
    }
  };

  const handleJoin = async () => {
    if (!nickname.trim()) return setError('닉네임을 입력해주세요');
    if (!roomId.trim()) return setError('방 코드를 입력해주세요');
    setLoading(true);
    try {
      const res = await axios.post(`${API}/rooms/${roomId.toUpperCase()}/join`, { nickname });
      setPlayerInfo(res.data.playerId, res.data.roomId, nickname);
      onEnter();
    } catch {
      setError('방 입장에 실패했습니다. 방 코드를 확인해주세요');
    } finally {
      setLoading(false);
    }
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
        borderRadius: 24, padding: 48, width: 400,
        border: '1px solid rgba(255,255,255,0.1)',
        boxShadow: '0 20px 60px rgba(0,0,0,0.5)',
      }}>
        {/* 로고 */}
        <div style={{ textAlign: 'center', marginBottom: 40 }}>
          <div style={{ fontSize: 64, marginBottom: 8 }}>🃏</div>
          <h1 style={{
            color: '#fff', margin: 0, fontSize: 36,
            fontWeight: 900, letterSpacing: 4,
          }}>UNO</h1>
          <p style={{ color: 'rgba(255,255,255,0.5)', margin: '8px 0 0', fontSize: 14 }}>
            멀티플레이어 카드 게임
          </p>
        </div>

        {/* 탭 */}
        <div style={{ display: 'flex', marginBottom: 28, borderRadius: 12, overflow: 'hidden',
          border: '1px solid rgba(255,255,255,0.1)' }}>
          {(['create', 'join'] as const).map((m) => (
            <button key={m} onClick={() => setMode(m)} style={{
              flex: 1, padding: '12px 0',
              backgroundColor: mode === m ? 'rgba(255,255,255,0.15)' : 'transparent',
              color: mode === m ? '#fff' : 'rgba(255,255,255,0.5)',
              border: 'none', cursor: 'pointer', fontSize: 14, fontWeight: 600,
              transition: 'all 0.2s',
            }}>
              {m === 'create' ? '방 만들기' : '방 참가'}
            </button>
          ))}
        </div>

        {/* 입력 */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <input
            placeholder="닉네임"
            value={nickname}
            onChange={e => setNickname(e.target.value)}
            maxLength={10}
            style={inputStyle}
          />
          {mode === 'join' && (
            <input
              placeholder="방 코드 (6자리)"
              value={roomId}
              onChange={e => setRoomId(e.target.value.toUpperCase())}
              maxLength={6}
              style={{ ...inputStyle, letterSpacing: 4, fontWeight: 700 }}
            />
          )}
          {error && (
            <p style={{ color: '#ff6b6b', margin: 0, fontSize: 13, textAlign: 'center' }}>
              {error}
            </p>
          )}
          <button
            onClick={mode === 'create' ? handleCreate : handleJoin}
            disabled={loading}
            style={{
              padding: '14px 0', borderRadius: 12,
              background: 'linear-gradient(135deg, #e53935, #e91e63)',
              color: '#fff', border: 'none', fontSize: 16,
              fontWeight: 700, cursor: loading ? 'not-allowed' : 'pointer',
              opacity: loading ? 0.7 : 1,
              transition: 'transform 0.1s',
            }}
            onMouseEnter={e => !loading && (e.currentTarget.style.transform = 'scale(1.02)')}
            onMouseLeave={e => (e.currentTarget.style.transform = 'scale(1)')}
          >
            {loading ? '처리 중...' : mode === 'create' ? '방 만들기' : '참가하기'}
          </button>
        </div>
      </div>
    </div>
  );
};

const inputStyle: React.CSSProperties = {
  padding: '14px 16px',
  borderRadius: 12,
  border: '1px solid rgba(255,255,255,0.15)',
  backgroundColor: 'rgba(255,255,255,0.08)',
  color: '#fff',
  fontSize: 16,
  outline: 'none',
};
