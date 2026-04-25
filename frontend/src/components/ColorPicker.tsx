// components/ColorPicker.tsx
import React from 'react';
import { CardColor } from '../types/game';

interface Props {
  onSelect: (color: CardColor) => void;
}

const COLORS: { color: CardColor; bg: string; label: string }[] = [
  { color: 'RED', bg: '#e53935', label: '빨강' },
  { color: 'BLUE', bg: '#1e88e5', label: '파랑' },
  { color: 'GREEN', bg: '#43a047', label: '초록' },
  { color: 'YELLOW', bg: '#fdd835', label: '노랑' },
];

export const ColorPicker: React.FC<Props> = ({ onSelect }) => (
  <div style={{
    position: 'fixed', inset: 0,
    backgroundColor: 'rgba(0,0,0,0.7)',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    zIndex: 100,
  }}>
    <div style={{
      backgroundColor: '#1a1a2e', borderRadius: 16, padding: 32,
      display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 16,
    }}>
      <h3 style={{ color: '#fff', margin: 0, fontSize: 20 }}>색상을 선택하세요</h3>
      <div style={{ display: 'flex', gap: 16 }}>
        {COLORS.map(({ color, bg, label }) => (
          <button
            key={color}
            onClick={() => onSelect(color)}
            style={{
              width: 72, height: 72, borderRadius: '50%',
              backgroundColor: bg, border: '4px solid rgba(255,255,255,0.3)',
              cursor: 'pointer', color: color === 'YELLOW' ? '#333' : '#fff',
              fontWeight: 'bold', fontSize: 12,
              transition: 'transform 0.1s, border-color 0.1s',
            }}
            onMouseEnter={e => (e.currentTarget.style.transform = 'scale(1.1)')}
            onMouseLeave={e => (e.currentTarget.style.transform = 'scale(1)')}
          >
            {label}
          </button>
        ))}
      </div>
    </div>
  </div>
);
