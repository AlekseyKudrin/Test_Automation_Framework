package services.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

@UtilityClass
public class GeneratorValue {
    /**
     * Метод генерирует UUID без знака "-"
     *
     * @return сгенерированный UUID
     */
    public static String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Метод генерирует случайное значение true или false
     *
     * @return сгенерированное значение
     */
    public static Boolean generateBoolean() {
        return new RandomDataGenerator().nextInt(0, 1) != 0;
    }

    /**
     * Метод выбирает случайный объект из переданного множества
     *
     * @param array множество объектов
     * @return выбранный случайный объект
     */
    public static Object generateValueFromArray(Object... array) {
        return array[new RandomDataGenerator().nextInt(0, array.length - 1)];
    }

    /**
     * Метод генерирует строку
     *
     * @param length     длинна строки
     * @param useLetters включить содержание букв в строке
     * @param useNumbers включить содержание цифр в строке
     * @return сгенерированная строка
     */
    public static String generateString(Integer length, Boolean useLetters, Boolean useNumbers) {
        return RandomStringUtils.random(length, useLetters, useNumbers);
    }

    /**
     * Метод генерирует случайное значение Integer из переданного диапазона
     *
     * @param lower нижняя граница диапазона(включительно)
     * @param upper верхняя граница диапазона(включительно)
     * @return сгенерированное значение
     */
    public static Integer generateInteger(int lower, int upper) {
        return new RandomDataGenerator().nextInt(lower, upper);
    }

    /**
     * Метод генерирует случайное значение Long из переданного диапазона
     *
     * @param lower нижняя граница диапазона(включительно)
     * @param upper верхняя граница диапазона(включительно)
     * @return сгенерированное значение
     */
    public static Long generateLong(long lower, long upper) {
        return new RandomDataGenerator().nextLong(lower, upper);
    }

    /**
     * Метод генерирует случайное значение Timestamp из переданного диапазона в формате uuuu-MM-dd'T'HH:mm
     *
     * @param of нижняя граница диапазона(включительно)
     * @param to верхняя граница диапазона(включительно)
     * @return сгенерированное значение
     */
    public static Timestamp generateTimestamp(String of, String to) {
        LocalDateTime leftLimit = of == null
                ? LocalDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault())
                : LocalDateTime.parse(of);
        LocalDateTime rightLimit = to == null
                ? LocalDateTime.now()
                : LocalDateTime.parse(to);
        Instant instant = Instant.ofEpochMilli(generateLong(
                leftLimit.toInstant(ZoneOffset.UTC).toEpochMilli(),
                rightLimit.toInstant(ZoneOffset.UTC).toEpochMilli()
        ));
        return Timestamp.from(instant);
    }
}
