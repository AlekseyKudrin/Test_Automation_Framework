package services.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class HelperJson {
    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
    }

    /**
     * Метод преобразует json в наглядно читаемый вид<br/>
     * <pre>
     *     Пример:
     *     json до преобразования:
     *     ==> {"id": "1", "name": "Alex"}<br/>
     *     json после преобразования:
     *     ==> {
     *             "id": "1",
     *             "name": "Alex"
     *         }
     * </pre>
     *
     * @param json json для преобразования
     * @return преобразованный json
     */
    public static String convertViewJson(String json) {
        try {
            Object o = mapper.readValue(json, Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Некорректная структура json", ex);
        }
    }

    /**
     * Метод десериализует json в объект
     *
     * @param jsonNode jsonNode для десериализации
     * @param clazz    тип объекта десериализации
     * @return десериализованный объект
     */
    public static <T> T convertJsonNodeToObject(JsonNode jsonNode, Class<T> clazz) {
        try {
            return mapper.treeToValue(jsonNode, clazz);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Некорректная структура json", ex);
        }
    }

    public static String convertObjectToJson(Object object) {
        return convertObjectToJson(object, true);
    }

    public static String convertObjectToJson(Object object, Boolean trim) {
        try {
            return mapper.registerModule(
                            new SimpleModule() {{
                                addSerializer(Timestamp.class, new CustomTimestampSerializer(trim));
                                addSerializer(Date.class, new CustomDateSerializer());
                                addSerializer(BigDecimal.class, new CustomBigDecimalSerializer());
                            }}
                    )
                    .writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Ошибка при чтении переданного объекта", ex);
        }
    }

    public static JsonNode convertStringToJsonNode(String json) {
        try {
            return mapper.readTree(json);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Ошибка при чтении JSON в виде строки", ex);
        }
    }

    public static Timestamp convertDataTimeOfArrayToTimestamp(ArrayNode arrayNode) {
        StringBuilder stringBuilder = new StringBuilder();
        arrayNode.forEach(i -> stringBuilder.append(i).append(","));
        LocalDateTime localDateTime = LocalDateTime.parse(
                stringBuilder,
                DateTimeFormatter.ofPattern("yyyy,M,d,H,m[,s],")
        );
        return Timestamp.valueOf(localDateTime);
    }

    public static JsonNode readJsonFromFile(String directory) {
        try {
            return mapper.readTree(new File(directory));
        } catch (IOException ex) {
            throw new RuntimeException("Ошибка при чтении файла", ex);
        }
    }

    public static <T> List<T> convertArrayNodeToList(ArrayNode arrayNode, Class<T> clazz) {
        return new ArrayList<>() {{
            arrayNode.forEach(i -> {
                try {
                    add(mapper.readValue(String.valueOf(i), clazz));

                } catch (IOException ex) {
                    throw new RuntimeException("Ошибка при чтении значения = " + i, ex);
                }
            });
        }};
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class CustomTimestampSerializer extends StdSerializer<Timestamp> {
        boolean trim;

        public CustomTimestampSerializer(boolean trim) {
            this(null, trim);
        }

        public CustomTimestampSerializer(Class<Timestamp> t, boolean trim) {
            super(t);
            this.trim = trim;
        }

        @Override
        public void serialize(Timestamp timestamp, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            serialize(timestamp, jsonGenerator, trim);
        }

        public void serialize(Timestamp timestamp, JsonGenerator jsonGenerator, boolean trim) throws IOException {
            if (trim) {
                String time = LocalDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault()).toString();
                while (time.charAt(time.length() - 1) == '0' || time.charAt(time.length() - 1) == '.') {
                    time = time.substring(0, time.length() - 1);
                }
                jsonGenerator.writeString(time);
            } else {
                jsonGenerator.writeString(LocalDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault()).toString());
            }
        }
    }

    public static class CustomDateSerializer extends StdSerializer<Date> {

        public CustomDateSerializer() {
            this(null);
        }

        public CustomDateSerializer(Class<Date> t) {
            super(t);
        }

        @Override
        public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(date.toString());
        }
    }

    public static class CustomBigDecimalSerializer extends StdSerializer<BigDecimal> {

        public CustomBigDecimalSerializer() {
            this(null);
        }

        public CustomBigDecimalSerializer(Class<BigDecimal> t) {
            super(t);
        }

        @Override
        public void serialize(BigDecimal bigDecimal, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(bigDecimal.toString());
        }
    }
}
