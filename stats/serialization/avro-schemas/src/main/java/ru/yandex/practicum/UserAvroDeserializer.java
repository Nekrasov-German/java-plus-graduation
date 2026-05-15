package ru.yandex.practicum;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public class UserAvroDeserializer extends BaseAvroDeserializer<UserActionAvro> {
    public UserAvroDeserializer() {
        super(UserActionAvro.getClassSchema());
    }
}
