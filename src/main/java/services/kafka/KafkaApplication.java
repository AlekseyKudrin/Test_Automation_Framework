package services.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class KafkaApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(KafkaApplication.class, args);
        KafkaService kafkaTest = context.getBean(KafkaService.class);
        String a = "asdf" + System.lineSeparator();
        StringBuilder st = new StringBuilder();
        st.replace(
                st.lastIndexOf("\r\n") == -1
                        ? st.lastIndexOf("\n")
                        : st.lastIndexOf("\r\n"),
                st.length(),
                "asdf");
//        List<String> kafkaConfig = (List<String>) context.getBean("producers");
//        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!");
//        System.out.println(kafkaConfig.get(0));
//        CompletableFuture<SendResult<Integer, String>> send = kafkaTest.getKafkaTemplate().send(
//                new ProducerRecord<>(
//                        "READ_TEST",
//                        0,
//                        "asdfasdf",
//                        "Test headers",
//                        new HashSet<>() {{
//                            add(new Header() {
//                                @Override
//                                public String key() {
//                                    return "TYPE";
//                                }
//
//                                @Override
//                                public byte[] value() {
//                                    return "application".getBytes(StandardCharsets.UTF_8);
//                                }
//                            });
//                        }}
//                ));
//
//        send.whenComplete((result, ex) -> result.getProducerRecord()
//                .headers().forEach(i -> System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!" + i.key() + " " + new String(i.value(), StandardCharsets.UTF_8))));
//
    }
}
