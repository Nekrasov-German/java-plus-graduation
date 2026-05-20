package ru.yandex.practicum.service;

import ru.practicum.ewm.stats.proto.UserActionProto;

public interface UserActionHandler {
    void handle(UserActionProto request);
}
