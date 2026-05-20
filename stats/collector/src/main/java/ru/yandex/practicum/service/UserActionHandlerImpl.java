package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.yandex.practicum.util.CollectorProducer;
import ru.yandex.practicum.util.mapper.MapperUserAction;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionHandlerImpl implements UserActionHandler {
    private final static String TOPIC_ACTION = "stats.user-actions.v1";


    private final CollectorProducer producer;

    @Override
    public void handle(UserActionProto request) {
        if (request == null) {
            log.warn("Получен пустой запрос UserActionProto");
            return;
        }

        try {
            UserActionAvro avroMessage = MapperUserAction.userActionProtoToUserActionAvro(request);
            producer.sendMessage(TOPIC_ACTION, avroMessage);
            log.info("Сообщение отправлено в топик: {}", TOPIC_ACTION);
        } catch (Exception e) {
            log.error("Ошибка отправки в Kafka для запроса: {}", request, e);
        }
    }
}
