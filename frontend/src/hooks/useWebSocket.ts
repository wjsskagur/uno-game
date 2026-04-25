// hooks/useWebSocket.ts
import { useEffect, useRef, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useGameStore } from '../store/gameStore';
import { Card, CardColor } from '../types/game';

const WS_URL = 'http://localhost:8080/ws';

export function useWebSocket() {
  const clientRef = useRef<Client | null>(null);
  const { playerId, roomId, setConnected, updateGameState, updateHand, setError } = useGameStore();

  useEffect(() => {
    if (!playerId || !roomId) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      reconnectDelay: 3000,

      onConnect: () => {
        setConnected(true);

        // 방 전체 상태 구독
        client.subscribe(`/topic/room/${roomId}`, (msg) => {
          updateGameState(JSON.parse(msg.body));
        });

        // 본인 손패 구독
        client.subscribe(`/topic/hand/${playerId}`, (msg) => {
          updateHand(JSON.parse(msg.body));
        });

        // 에러 구독
        client.subscribe(`/topic/error/${playerId}`, (msg) => {
          const err = JSON.parse(msg.body);
          setError(err.message);
          setTimeout(() => setError(null), 3000);
        });
      },

      onDisconnect: () => setConnected(false),
      onStompError: () => setError('연결 오류가 발생했습니다.'),
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [playerId, roomId]);

  const playCard = useCallback((card: Card, declaredColor?: CardColor) => {
    clientRef.current?.publish({
      destination: '/app/game.play',
      body: JSON.stringify({ roomId, playerId, card, declaredColor }),
    });
  }, [roomId, playerId]);

  const drawCard = useCallback(() => {
    clientRef.current?.publish({
      destination: '/app/game.draw',
      body: JSON.stringify({ roomId, playerId }),
    });
  }, [roomId, playerId]);

  const callUno = useCallback(() => {
    clientRef.current?.publish({
      destination: '/app/game.uno',
      body: JSON.stringify({ roomId, playerId }),
    });
  }, [roomId, playerId]);

  return { playCard, drawCard, callUno };
}
