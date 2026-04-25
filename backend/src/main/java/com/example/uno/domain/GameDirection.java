package com.example.uno.domain;

public enum GameDirection {
    CLOCKWISE, COUNTER_CLOCKWISE;

    public GameDirection reverse() {
        return this == CLOCKWISE ? COUNTER_CLOCKWISE : CLOCKWISE;
    }
}
