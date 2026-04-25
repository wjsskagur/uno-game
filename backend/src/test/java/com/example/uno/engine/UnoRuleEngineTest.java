package com.example.uno.engine;

import com.example.uno.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Deque;

import static org.assertj.core.api.Assertions.*;

class UnoRuleEngineTest {

    private UnoRuleEngine engine;

    @BeforeEach
    void setUp() {
        engine = new UnoRuleEngine();
    }

    @Test
    @DisplayName("덱 생성 시 108장이어야 한다")
    void createDeck_shouldHave108Cards() {
        Deque<Card> deck = engine.createShuffledDeck();
        assertThat(deck).hasSize(108);
    }

    @Test
    @DisplayName("같은 색상 카드는 낼 수 있다")
    void canPlay_sameColor() {
        Card top = Card.number(CardColor.RED, 5);
        Card toPlay = Card.number(CardColor.RED, 3);
        assertThat(engine.canPlay(toPlay, top, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("같은 숫자 카드는 색상이 달라도 낼 수 있다")
    void canPlay_sameNumber() {
        Card top = Card.number(CardColor.RED, 5);
        Card toPlay = Card.number(CardColor.BLUE, 5);
        assertThat(engine.canPlay(toPlay, top, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("색상도 숫자도 다른 카드는 낼 수 없다")
    void canPlay_differentColorAndNumber() {
        Card top = Card.number(CardColor.RED, 5);
        Card toPlay = Card.number(CardColor.BLUE, 3);
        assertThat(engine.canPlay(toPlay, top, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("WILD 카드는 항상 낼 수 있다")
    void canPlay_wildAlwaysPlayable() {
        Card top = Card.number(CardColor.RED, 5);
        Card wild = Card.wild(CardType.WILD);
        assertThat(engine.canPlay(wild, top, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("WILD 이후 선언된 색상으로 판단한다")
    void canPlay_afterWild_usesDeclaredColor() {
        Card top = Card.wild(CardType.WILD); // 방금 낸 WILD
        CardColor declaredColor = CardColor.BLUE; // 파란색 선언

        Card blue = Card.number(CardColor.BLUE, 3);
        Card red = Card.number(CardColor.RED, 3);

        assertThat(engine.canPlay(blue, top, declaredColor)).isTrue();
        assertThat(engine.canPlay(red, top, declaredColor)).isFalse();
    }

    @Test
    @DisplayName("REVERSE 카드는 방향을 바꾼다")
    void applyEffect_reverse() {
        GameState state = createGameState(3);
        state.getDeck().addAll(engine.createShuffledDeck());

        Card reverse = Card.action(CardColor.RED, CardType.REVERSE);
        GameDirection before = state.getDirection();

        engine.applyEffect(state, reverse, CardColor.RED);

        assertThat(state.getDirection()).isNotEqualTo(before);
    }

    @Test
    @DisplayName("2인 게임에서 REVERSE는 SKIP처럼 동작한다")
    void applyEffect_reverseWith2Players_actLikeSkip() {
        GameState state = createGameState(2);
        int before = state.getCurrentPlayerIndex();

        engine.applyEffect(state, Card.action(CardColor.RED, CardType.REVERSE), CardColor.RED);

        // 방향 바뀌고 advanceTurn 한 번 → 결과적으로 자기 턴 다시
        assertThat(state.getCurrentPlayerIndex()).isEqualTo(before);
    }

    @Test
    @DisplayName("DRAW_TWO 카드는 다음 플레이어에게 2장을 준다")
    void applyEffect_drawTwo() {
        GameState state = createGameState(2);
        state.getDeck().addAll(engine.createShuffledDeck());

        Player nextPlayer = state.getPlayers().get(1);
        int before = nextPlayer.getHandSize();

        engine.applyEffect(state, Card.action(CardColor.RED, CardType.DRAW_TWO), CardColor.RED);

        assertThat(nextPlayer.getHandSize()).isEqualTo(before + 2);
    }

    // ─── 헬퍼 ────────────────────────────────────────────────────────

    private GameState createGameState(int playerCount) {
        GameState state = new GameState("test-room");
        for (int i = 0; i < playerCount; i++) {
            state.addPlayer(new Player("player-" + i, "Player" + i));
        }
        state.getDiscardPile().push(Card.number(CardColor.RED, 1));
        state.setCurrentColor(CardColor.RED);
        return state;
    }
}
