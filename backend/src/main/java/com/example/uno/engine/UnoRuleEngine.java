package com.example.uno.engine;

import com.example.uno.domain.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * UNO 룰 엔진 - 순수 도메인 로직만 담당
 * Spring 의존성 최소화 → 단위 테스트 용이
 */
@Component
public class UnoRuleEngine {

    private static final int INITIAL_HAND_SIZE = 7;

    /**
     * 108장 덱 생성 후 셔플
     * 0: 각 색 1장 / 1~9: 각 색 2장 / 액션: 각 색 2장 / WILD: 4장씩
     */
    public Deque<Card> createShuffledDeck() {
        List<Card> cards = new ArrayList<>();
        List<CardColor> colors = List.of(CardColor.RED, CardColor.BLUE, CardColor.GREEN, CardColor.YELLOW);

        for (CardColor color : colors) {
            cards.add(Card.number(color, 0));

            for (int i = 1; i <= 9; i++) {
                cards.add(Card.number(color, i));
                cards.add(Card.number(color, i));
            }

            for (CardType type : List.of(CardType.SKIP, CardType.REVERSE, CardType.DRAW_TWO)) {
                cards.add(Card.action(color, type));
                cards.add(Card.action(color, type));
            }
        }

        for (int i = 0; i < 4; i++) {
            cards.add(Card.wild(CardType.WILD));
            cards.add(Card.wild(CardType.WILD_DRAW_FOUR));
        }

        Collections.shuffle(cards);
        return new ArrayDeque<>(cards);
    }

    /**
     * 게임 시작 초기화
     * - 덱 생성 및 배분
     * - 첫 카드는 숫자 카드로 보장 (액션 카드로 시작하면 복잡해짐)
     */
    public void initializeGame(GameState state) {
        Deque<Card> deck = createShuffledDeck();
        state.getDeck().addAll(deck);

        // 각 플레이어에게 7장 배분
        for (Player player : state.getPlayers()) {
            for (int i = 0; i < INITIAL_HAND_SIZE; i++) {
                player.addCard(state.getDeck().poll());
            }
        }

        // 첫 카드: WILD가 나오면 덱 맨 뒤로 보내고 재시도
        Card firstCard;
        do {
            firstCard = state.getDeck().poll();
            if (firstCard.isWild()) {
                state.getDeck().offer(firstCard);
            }
        } while (firstCard.isWild());

        state.getDiscardPile().push(firstCard);
        state.setCurrentColor(firstCard.color());
        state.setPhase(GameState.Phase.PLAYING);
    }

    /**
     * 카드를 낼 수 있는지 검증
     * 우선순위: WILD > 색깔 일치 > 타입 일치 > 숫자 일치
     */
    public boolean canPlay(Card toPlay, Card topCard, CardColor currentColor) {
        if (toPlay.isWild()) return true;

        CardColor effectiveColor = topCard.isWild() ? currentColor : topCard.color();

        return toPlay.color() == effectiveColor
                || toPlay.type() == topCard.type()
                || (toPlay.isNumberCard() && topCard.isNumberCard() && toPlay.number() == topCard.number());
    }

    /**
     * 카드 효과 적용
     * advanceTurn()은 효과 적용 후 GameService에서 호출 → 단일 책임 분리
     */
    public void applyEffect(GameState state, Card played, CardColor declaredColor) {
        switch (played.type()) {
            case SKIP -> state.advanceTurn();  // 다음 플레이어 건너뜀

            case REVERSE -> {
                state.reverseDirection();
                if (state.getPlayers().size() == 2) {
                    state.advanceTurn(); // 2인 게임에서 REVERSE = SKIP
                }
            }

            case DRAW_TWO -> {
                giveCards(state, state.nextPlayerIndex(), 2);
                state.advanceTurn();
            }

            case WILD -> state.setCurrentColor(declaredColor);

            case WILD_DRAW_FOUR -> {
                state.setCurrentColor(declaredColor);
                giveCards(state, state.nextPlayerIndex(), 4);
                state.advanceTurn();
            }

            case NUMBER -> { /* 숫자 카드는 효과 없음 */ }
        }
    }

    /**
     * 특정 플레이어에게 카드 지급
     * 덱이 소진되면 discardPile을 재활용 (맨 위 카드 제외)
     */
    public void giveCards(GameState state, int playerIndex, int count) {
        Player target = state.getPlayers().get(playerIndex);
        for (int i = 0; i < count; i++) {
            if (state.getDeck().isEmpty()) reshuffleDeck(state);
            Card drawn = state.getDeck().poll();
            if (drawn != null) target.addCard(drawn);
        }
    }

    /**
     * 덱 재셔플: 버린 카드 더미(맨 위 제외)를 새 덱으로
     */
    private void reshuffleDeck(GameState state) {
        Card topCard = state.getDiscardPile().poll();
        List<Card> rest = new ArrayList<>(state.getDiscardPile());
        state.getDiscardPile().clear();
        if (topCard != null) state.getDiscardPile().push(topCard);

        Collections.shuffle(rest);
        rest.forEach(state.getDeck()::offer);
    }
}
