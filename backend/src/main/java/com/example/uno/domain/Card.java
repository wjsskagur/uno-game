package com.example.uno.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Card는 불변 값 객체 → record 사용
 * equals/hashCode/toString 자동 생성, 실수로 상태 변경 불가
 */
public record Card(CardColor color, CardType type, int number) {

    public static Card number(CardColor color, int number) {
        return new Card(color, CardType.NUMBER, number);
    }

    public static Card action(CardColor color, CardType type) {
        return new Card(color, type, -1);
    }

    public static Card wild(CardType type) {
        return new Card(CardColor.WILD, type, -1);
    }

    @JsonIgnore
    public boolean isWild() {
        return color == CardColor.WILD;
    }

    @JsonIgnore
    public boolean isNumberCard() {
        return type == CardType.NUMBER;
    }

    @Override
    public String toString() {
        return isWild()
                ? type.name()
                : color.name() + "_" + (isNumberCard() ? number : type.name());
    }
}
