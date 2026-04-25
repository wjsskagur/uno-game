package com.example.uno.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Getter
public class GameState {

    public enum Phase { WAITING, PLAYING, FINISHED }

    private final String roomId;
    private final List<Player> players;
    private final Deque<Card> deck;
    private final Deque<Card> discardPile;

    private int currentPlayerIndex;
    private GameDirection direction;

    @Setter
    private CardColor currentColor;  // WILD 후 선언된 색상 추적

    @Setter
    private Phase phase;

    @Setter
    private String winnerId;

    public GameState(String roomId) {
        this.roomId = roomId;
        this.players = new ArrayList<>();
        this.deck = new ArrayDeque<>();
        this.discardPile = new ArrayDeque<>();
        this.direction = GameDirection.CLOCKWISE;
        this.phase = Phase.WAITING;
    }

    public Player currentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public Card topCard() {
        return discardPile.peek();
    }

    public int nextPlayerIndex() {
        int size = players.size();
        return direction == GameDirection.CLOCKWISE
                ? (currentPlayerIndex + 1) % size
                : (currentPlayerIndex - 1 + size) % size;
    }

    public void advanceTurn() {
        currentPlayerIndex = nextPlayerIndex();
    }

    public void reverseDirection() {
        direction = direction.reverse();
    }

    public Player getPlayerById(String playerId) {
        return players.stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
    }

    public boolean isCurrentPlayer(String playerId) {
        return currentPlayer().getId().equals(playerId);
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(String playerId) {
        players.removeIf(p -> p.getId().equals(playerId));
    }
}
