// types/game.ts

export type CardColor = 'RED' | 'BLUE' | 'GREEN' | 'YELLOW' | 'WILD';
export type CardType = 'NUMBER' | 'SKIP' | 'REVERSE' | 'DRAW_TWO' | 'WILD' | 'WILD_DRAW_FOUR';
export type GameDirection = 'CLOCKWISE' | 'COUNTER_CLOCKWISE';
export type GamePhase = 'WAITING' | 'PLAYING' | 'FINISHED';

export interface Card {
  color: CardColor;
  type: CardType;
  number: number;
}

export interface PlayerInfo {
  id: string;
  nickname: string;
  handSize: number;
  calledUno: boolean;
}

export interface GameState {
  roomId: string;
  currentPlayerId: string;
  currentPlayerNickname: string;
  currentColor: CardColor;
  direction: GameDirection;
  topCard: Card;
  deckSize: number;
  players: PlayerInfo[];
  phase: GamePhase;
  winnerId: string | null;
  winnerNickname: string | null;
  lastAction: string;
}

export interface PlayerHandUpdate {
  playerId: string;
  hand: Card[];
}

export interface ErrorResponse {
  message: string;
}
