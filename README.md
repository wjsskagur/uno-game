# 🃏 UNO Multiplayer Game

> Spring Boot + React 기반 실시간 멀티플레이어 UNO 카드 게임

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=flat-square)
![React](https://img.shields.io/badge/React-18-61DAFB?style=flat-square)
![TypeScript](https://img.shields.io/badge/TypeScript-5.2-3178C6?style=flat-square)
![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-brightgreen?style=flat-square)

---

## 📌 프로젝트 개요

2~4인이 실시간으로 플레이 가능한 UNO 카드 게임입니다.
WebSocket(STOMP)을 활용해 게임 상태를 실시간으로 동기화하며,
서버가 모든 게임 로직을 관리해 치트를 방지합니다.

---

## 🏗️ 아키텍처

```
┌─────────────────────────────────────────────────────┐
│                    Frontend (React)                  │
│  LobbyPage → WaitingRoom → GameBoard                 │
│  Zustand (상태관리) + STOMP WebSocket                 │
└──────────────────┬──────────────────────────────────┘
                   │ REST (방 생성/입장)
                   │ WebSocket/STOMP (게임 액션)
┌──────────────────▼──────────────────────────────────┐
│                   Backend (Spring Boot)              │
│                                                      │
│  GameController  ──▶  GameService                    │
│  (REST + STOMP)        │                             │
│                        ├── UnoRuleEngine (룰 엔진)   │
│                        └── GameStore (InMemory)      │
└─────────────────────────────────────────────────────┘
```

### 기술적 의사결정

| 결정 | 이유 |
|------|------|
| **STOMP over WebSocket** | pub/sub 모델로 /topic 구독이 직관적, Spring과 자연스럽게 통합 |
| **InMemory ConcurrentHashMap** | 게임 데이터는 휘발성, Redis 없이도 멀티스레드 안전 보장 |
| **Card → record** | 불변 값 객체, equals/hashCode 자동 생성, 실수 방지 |
| **손패 서버 보관** | 클라이언트에 손패 전체를 보내지 않아 치트 방지 |
| **RuleEngine 분리** | 순수 도메인 로직만 담당 → 단위 테스트 용이 |

---

## 🎮 게임 기능

- ✅ 방 생성 / 6자리 코드로 초대
- ✅ 2~4인 실시간 멀티플레이
- ✅ 숫자 카드, SKIP, REVERSE, +2, WILD, +4 전체 구현
- ✅ UNO 선언
- ✅ 덱 소진 시 버린 카드 더미 자동 재셔플
- ✅ 2인 게임에서 REVERSE = SKIP 룰 적용
- ✅ 플레이어 퇴장 시 게임 종료 처리

---

## 🚀 실행 방법

### 사전 요구사항
- Java 21+
- Node.js 18+

### Backend
```bash
cd backend
./gradlew bootRun
# http://localhost:8080
```

### Frontend
```bash
cd frontend
npm install
npm run dev
# http://localhost:3000
```

---

## 📁 프로젝트 구조

```
uno-game/
├── backend/
│   └── src/main/java/com/example/uno/
│       ├── domain/          # Card, Player, GameState 등 핵심 도메인
│       ├── engine/          # UnoRuleEngine - 순수 룰 로직
│       ├── service/         # GameService - 게임 흐름 제어
│       ├── controller/      # REST + WebSocket 컨트롤러
│       ├── store/           # InMemory 게임 저장소
│       ├── dto/             # 요청/응답 DTO
│       └── config/          # WebSocket, CORS 설정
└── frontend/
    └── src/
        ├── components/      # UnoCard, ColorPicker
        ├── pages/           # LobbyPage, WaitingRoom, GameBoard
        ├── hooks/           # useWebSocket
        ├── store/           # Zustand 상태관리
        └── types/           # TypeScript 타입 정의
```

---

## 🔧 개선 가능한 포인트

- [ ] Redis 연동으로 서버 재시작 시 게임 상태 복구
- [ ] JWT 인증 추가
- [ ] UNO 선언 안 했을 때 패널티 (+2장) 구현
- [ ] 카드 내기 애니메이션
- [ ] 관전 모드
