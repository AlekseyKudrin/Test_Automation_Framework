package services.kafka;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;

@Component
@RequiredArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaService {
    KafkaTemplate kafkaTemplate;

    /*Должны быть методы отправки сообщений
    1. С заголовками
    2. без заголовков
    3. на определенный раздел ?
    4. сразу же получить ответ
     */
    public void send (){
        kafkaTemplate.send(
                 new ProducerRecord<>(
                        "READ_TEST",
                        0,
                        "asdfasdf",
                        "Test headers",
                        new HashSet<Header>() {{
                            add(new Header() {
                                @Override
                                public String key() {
                                    return "TYPE";
                                }

                                @Override
                                public byte[] value() {
                                    return "application".getBytes(StandardCharsets.UTF_8);
                                }
                            });
                        }}
                ));

//        send.whenComplete((result, ex) -> result.getProducerRecord()
//                .headers().forEach(i -> System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!" + i.key() + " " + new String(i.value(), StandardCharsets.UTF_8))));
    }
}
