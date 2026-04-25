// store/gameStore.ts
import { create } from 'zustand';
import { Card, GameState, PlayerHandUpdate } from '../types/game';

interface GameStore {
  playerId: string | null;
  roomId: string | null;
  nickname: string | null;
  gameState: GameState | null;
  myHand: Card[];
  error: string | null;
  isConnected: boolean;
  selectedCard: Card | null;
  showColorPicker: boolean;

  setPlayerInfo: (playerId: string, roomId: string, nickname: string) => void;
  updateGameState: (state: GameState) => void;
  updateHand: (update: PlayerHandUpdate) => void;
  setConnected: (connected: boolean) => void;
  setError: (error: string | null) => void;
  selectCard: (card: Card | null) => void;
  setShowColorPicker: (show: boolean) => void;
  reset: () => void;
}

export const useGameStore = create<GameStore>((set) => ({
  playerId: null,
  roomId: null,
  nickname: null,
  gameState: null,
  myHand: [],
  error: null,
  isConnected: false,
  selectedCard: null,
  showColorPicker: false,

  setPlayerInfo: (playerId, roomId, nickname) =>
    set({ playerId, roomId, nickname }),

  updateGameState: (gameState) =>
    // Bug fix 3: 게임 상태 업데이트 시 selectedCard 초기화
    // 서버에서 새 상태가 오면 이전에 선택한 카드 참조가 stale해짐
    set({ gameState, selectedCard: null }),

  updateHand: (update) =>
    set({ myHand: update.hand }),

  setConnected: (isConnected) =>
    set({ isConnected }),

  setError: (error) =>
    set({ error }),

  selectCard: (selectedCard) =>
    set({ selectedCard }),

  setShowColorPicker: (showColorPicker) =>
    set({ showColorPicker }),

  reset: () =>
    set({
      playerId: null, roomId: null, nickname: null,
      gameState: null, myHand: [], error: null,
      isConnected: false, selectedCard: null, showColorPicker: false,
    }),
}));
