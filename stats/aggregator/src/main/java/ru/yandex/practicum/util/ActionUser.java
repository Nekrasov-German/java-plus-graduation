package ru.yandex.practicum.util;

import lombok.Getter;

@Getter
public enum ActionUser {
    VIEW(0.4),
    REGISTER(0.8),
    LIKE(1.0);

    private final double weight;

    ActionUser(double weight) {
        this.weight = weight;
    }

}
