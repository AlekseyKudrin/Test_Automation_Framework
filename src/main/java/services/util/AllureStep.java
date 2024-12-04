package services.util;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Parameter;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AllureSteps<br/> Утилитарный класс для формирования отчета в Allure и параметризации теста
 */
@UtilityClass
public class AllureStep {
    /**
     * URL для прикрепления адреса тест-кейса
     */
    private static final String ALLURE_LINK_TMS_PATTERN = "https://...";

    /**
     * Метод инициализирует:<br/>
     * - Имя теста<br/>
     * - Ссылку на тест-кейс<br/>
     */
    public static void initializrTest() {
        setTestName();
        setTmsLink();
    }

    /**
     * Метод устанавливает название тест-кейса соответствующие названию переданному
     * через @ParameterizedTest(name = "FIND-...")<br/>
     * <pre>
     *     Пример:
     *     - @DisplayName (FIND-T01, FIND-T02, FIND-T03, ... N)
     *     - @ParametrizedTest (name = "FIND-T01: Проверка")<br/>
     *     - Изначальное имя тест-кейса
     *     ==> FIND-T01, FIND-T02, FIND-T03 FIND-T01: Проверка
     *     - Установленное имя тест-кейса
     *     ==> FIND-T01: Проверка
     * </pre>
     */
    public static void setTestName() {
        Allure.getLifecycle().updateTestCase(testResult -> {
            String nameTestCase = testResult.getName();
            String changeNameTestCase = nameTestCase.substring(nameTestCase.lastIndexOf("FIND-"));
            setTestName(changeNameTestCase);
        });
    }

    /**
     * Метод устанавливает название тест-кейса
     *
     * @param testName название тест-кейса
     */
    public static void setTestName(String testName) {
        Allure.getLifecycle().updateTestCase(testResult -> testResult.setName(testName));
    }

    /**
     * Метод устанавливает ссылке на тест-кейс<br/>
     *
     * <pre>
     *     Пример:
     *     - @ParameterizedTest (name = "FIND-T01: Проверка")
     *     - PATTERN = https://jira.../testCase/<br/>
     *     - Прикрепленная ссылка на тест-кейс
     *     ==> https://jira.../testCase/FIND-T01
     * </pre>
     */
    public static void setTmsLink() {
        Allure.getLifecycle().updateTestCase(testResult -> {
            String nameTestCase = testResult.getName();
            String suffix = nameTestCase.substring(
                    nameTestCase.lastIndexOf("FIND-"),
                    nameTestCase.lastIndexOf(":"));
            if (!testResult.getLabels().toString().contains(ALLURE_LINK_TMS_PATTERN.concat(suffix)))
                Allure.tms(suffix, ALLURE_LINK_TMS_PATTERN.concat(suffix));
        });
    }

    /**
     * Метод добавляет параметры к текущему шагу<br/>
     *
     * @param params добавляемые параметры
     */
    public static void addParam(Param... params) {
        Allure.getLifecycle().updateStep(stepResult -> {
            List<Parameter> parameters = new ArrayList<>(stepResult.getParameters());
            parameters.addAll(createParam(Arrays
                    .stream(params)
                    .collect(Collectors.toMap(
                            param -> param.key,
                            param -> param.value,
                            (a, b) -> b,
                            LinkedHashMap::new))));
            stepResult.setParameters(parameters);
        });
    }

    /**
     * Метод создает шаг тест-кейса
     *
     * @param name название шага
     */
    public static void step(String name) {
        step(name, new Param[]{});
    }

    /**
     * Метод преобразует переданную коллекцию параметров "Map" в коллекцию параметров "List"
     *
     * @param param набор параметров в виде "Map"
     * @return набор параметров в виде "List"
     */
    private static List<Parameter> createParam(Map<String, Object> param) {
        return param != null
                ? param.entrySet()
                .stream()
                .map(i -> new Parameter()
                        .setName(i.getKey())
                        .setValue(i.getValue().toString()))
                .toList()
                : null;
    }

    /**
     * Класс описывает параметр для шага
     *
     * @param key   название параметра
     * @param value значение параметра
     */
    public record Param(String key, Object value) {
        public static Param of(String key, Object value) {
            return new Param(key, value);
        }
    }

    /**
     * Класс описывает прикрепление в шаге
     *
     * @param name  название прикрепления
     * @param value прикрепляемый файл
     * @param type  тип прикрепляемого файла
     */
    public record Attachment(String name, Object value, Type type) {
        public static Attachment of(String name, Object value) {
            return new Attachment(name, value, null);
        }

        public static Attachment of(String name, Object value, Type type) {
            return new Attachment(name, value, type);
        }
    }

    /**
     * Типы и расширения файла для прикрепления
     */
    public enum Type {
        TEXT("text/plain,.json"),
        HTML("text/html,.html"),
        JSON("application/json,.json");

        private final String type;

        Type(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
