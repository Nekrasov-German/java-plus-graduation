package ru.yandex.practicum.mappers;

import lombok.Getter;

@Getter
public enum ActionUser {
    VIEW(0.4F),
    REGISTER(0.8F),
    LIKE(1.0F);

    private final float weight;

    ActionUser(float weight) {
        this.weight = weight;
    }

}
