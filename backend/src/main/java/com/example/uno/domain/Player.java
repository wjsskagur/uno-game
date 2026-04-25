package com.example.uno.domain;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Player {

    private final String id;         // WebSocket sessionId
    private final String nickname;
    private final List<Card> hand;   // 손패 - 서버만 보유 (치트 방지)
    private boolean calledUno;       // UNO 선언 여부

    public Player(String id, String nickname) {
        this.id = id;
        this.nickname = nickname;
        this.hand = new ArrayList<>();
        this.calledUno = false;
    }

    public void addCard(Card card) {
        hand.add(card);
        if (hand.size() > 1) calledUno = false; // 카드 추가되면 UNO 선언 리셋
    }

    public boolean removeCard(Card card) {
        return hand.remove(card);
    }

    public void callUno() {
        this.calledUno = true;
    }

    public int handSize() {
        return hand.size();
    }

    public boolean hasWon() {
        return hand.isEmpty();
    }
}
