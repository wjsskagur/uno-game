// components/UnoCard.tsx
import React from 'react';
import { Card, CardColor } from '../types/game';

interface Props {
  card: Card;
  onClick?: () => void;
  disabled?: boolean;
  selected?: boolean;
  size?: 'sm' | 'md' | 'lg';
}

const COLOR_MAP: Record<CardColor, string> = {
  RED: '#e53935',
  BLUE: '#1e88e5',
  GREEN: '#43a047',
  YELLOW: '#fdd835',
  WILD: '#212121',
};

const LABEL_MAP: Record<string, string> = {
  SKIP: '⊘',
  REVERSE: '↺',
  DRAW_TWO: '+2',
  WILD: '🌈',
  WILD_DRAW_FOUR: '+4',
};

const SIZE_MAP = {
  sm: { width: 52, height: 78, fontSize: 14 },
  md: { width: 70, height: 105, fontSize: 18 },
  lg: { width: 90, height: 135, fontSize: 24 },
};

export const UnoCard: React.FC<Props> = ({
  card, onClick, disabled = false, selected = false, size = 'md'
}) => {
  const { width, height, fontSize } = SIZE_MAP[size];
  const bgColor = COLOR_MAP[card.color];
  const label = card.type === 'NUMBER' ? String(card.number) : (LABEL_MAP[card.type] ?? card.type);

  return (
    <div
      onClick={disabled ? undefined : onClick}
      style={{
        width,
        height,
        backgroundColor: bgColor,
        borderRadius: 10,
        border: selected ? '3px solid #fff' : '3px solid rgba(255,255,255,0.3)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        cursor: disabled ? 'not-allowed' : 'pointer',
        transform: selected ? 'translateY(-12px)' : 'none',
        transition: 'transform 0.15s ease, box-shadow 0.15s ease',
        boxShadow: selected
          ? '0 8px 24px rgba(0,0,0,0.5)'
          : '0 2px 8px rgba(0,0,0,0.3)',
        opacity: disabled ? 0.5 : 1,
        userSelect: 'none',
        flexShrink: 0,
        position: 'relative',
      }}
    >
      {/* 타원형 안쪽 */}
      <div style={{
        width: '75%', height: '80%',
        backgroundColor: 'rgba(255,255,255,0.15)',
        borderRadius: '50%',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        position: 'absolute',
        transform: 'rotate(-30deg)',
      }} />
      <span style={{
        color: card.color === 'YELLOW' ? '#333' : '#fff',
        fontSize,
        fontWeight: 'bold',
        zIndex: 1,
        textShadow: '0 1px 3px rgba(0,0,0,0.4)',
      }}>
        {label}
      </span>
      {/* 모서리 라벨 */}
      <span style={{
        position: 'absolute', top: 4, left: 6,
        fontSize: fontSize * 0.5,
        color: card.color === 'YELLOW' ? '#333' : '#fff',
        fontWeight: 'bold', lineHeight: 1,
      }}>{label}</span>
    </div>
  );
};
