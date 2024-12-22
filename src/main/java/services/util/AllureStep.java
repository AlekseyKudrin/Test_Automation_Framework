package services.util;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Parameter;
import io.qameta.allure.model.Stage;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import lombok.experimental.UtilityClass;
import org.junit.jupiter.params.provider.Arguments;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
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
     * Метод устанавливает название текущего шага и параметры для него.<br/>
     * Параметры устанавливаются в той последовательности, в которой было переданы.
     *
     * @param name   название текущего шага
     * @param params параметры текущего шага
     */
    public static void step(String name, Param... params) {
        Map<String, Object> map = Arrays
                .stream(params)
                .collect(Collectors.toMap(
                        Param::key,
                        Param::value,
                        (a, b) -> b,
                        LinkedHashMap::new
                ));
        step(name, map);
    }

    /**
     * Метод устанавливает название текущего шага и параметры для него.<br/>
     * Параметры устанавливаются в случайной последовательности.
     *
     * @param name  название текущего шага
     * @param param параметры текущего шага
     */
    public static void step(String name, Map<String, Object> param) {
        String uuidTestCase = Allure.getLifecycle().getCurrentTestCase().orElseThrow();
        String uuidStep = Allure.getLifecycle().getCurrentTestCaseOrStep().orElseThrow();
        switch (name.substring(0, !name.contains(" ") ? name.length() : name.indexOf(" ")).toLowerCase()) {
            case "last" -> Allure.getLifecycle().updateStep(stepResult -> {
                if (stepResult.getSteps()
                        .stream()
                        .allMatch(i -> i.getStatus().equals(Status.PASSED))) {
                    while ((!uuidTestCase.equals(Allure.getLifecycle().getCurrentTestCaseOrStep().orElseThrow()))) {
                        Allure.getLifecycle().updateStep(currentStep -> {
                            currentStep.setStatus(Status.PASSED);
                            currentStep.setStage(Stage.FINISHED);
                        });
                        Allure.getLifecycle().stopStep();
                    }
                }
            });
            case "шаг" -> {
                if (isStepAlreadyRun()) {
                    while (!uuidTestCase.equals(Allure.getLifecycle().getCurrentTestCaseOrStep().orElseThrow())) {
                        Allure.getLifecycle().updateStep(stepResult -> {
                            stepResult.setStatus(Status.PASSED);
                            stepResult.setStage(Stage.FINISHED);
                        });
                        Allure.getLifecycle().stopStep();
                    }
                    Allure.getLifecycle().startStep(
                            uuidTestCase,
                            GeneratorValue.generateId(),
                            new StepResult().setName(name)
                                    .setParameters(createParam(param))
                                    .setStage(Stage.RUNNING)
                                    .setStatus(Status.FAILED)
                    );
                } else {
                    Allure.getLifecycle().startStep(
                            GeneratorValue.generateId(),
                            new StepResult().setName(name)
                                    .setParameters(createParam(param))
                                    .setStage(Stage.RUNNING)
                                    .setStatus(Status.FAILED)
                    );
                }
            }
            case "проверка", "подготовка" -> Allure.getLifecycle().startStep(
                    uuidStep,
                    GeneratorValue.generateId(),
                    new StepResult()
                            .setName(name)
                            .setParameters(createParam(param))
                            .setStage(Stage.RUNNING)
                            .setStatus(Status.FAILED)
            );
            default -> {
                if (isSubStepAlreadyRun()) {
                    Allure.getLifecycle().updateStep(stepResult -> {
                        stepResult.setStage(Stage.FINISHED);
                        stepResult.setStatus(Status.PASSED);
                    });
                    Allure.getLifecycle().stopStep(uuidStep);
                    uuidStep = Allure.getLifecycle().getCurrentTestCaseOrStep().orElseThrow();
                }
                Allure.getLifecycle().startStep(
                        uuidStep,
                        GeneratorValue.generateId(),
                        new StepResult()
                                .setName(name)
                                .setParameters(createParam(param))
                                .setStage(Stage.RUNNING)
                                .setStatus(Status.FAILED)
                );
            }
        }
    }

    /**
     * Метод проверяет статус "RUNNING" у шага начинающегося со строки:<br/>
     * - "шаг"<br/>
     * - "подготовка"
     *
     * @return true/false
     */
    public static boolean isStepAlreadyRun() {
        AtomicBoolean isStepPresent = new AtomicBoolean(false);
        Allure.getLifecycle().updateTestCase(testResult -> {
            StepResult lastStep = testResult.getSteps().size() == 0
                    ? null
                    : testResult.getSteps().get(testResult.getSteps().size() - 1);
            if (lastStep != null) {
                isStepPresent.set((lastStep.getName().contains("Шаг")
                        || lastStep.getName().contains("Подготовка"))
                        && lastStep.getStage().equals(Stage.RUNNING));
            }
        });
        return isStepPresent.get();
    }

    /**
     * Метод проверяет, является ли данный шаг вложенным шагом.
     *
     * @return true/false
     */
    public static boolean isSubStepAlreadyRun() {
        AtomicBoolean isStepPresent = new AtomicBoolean(false);
        Allure.getLifecycle().updateStep(stepResult -> isStepPresent.set(!stepResult.getName().contains("Шаг")));
        return isStepPresent.get();
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
     * Метод прикрепляет вложения к текущему шагу
     *
     * @param attachments массив вложений
     */
    public static void attachment(Attachment... attachments) {
        for (Attachment attachment : attachments) {
            String[] typeAndExtends = attachment.type() == null
                    ? new String[]{"text/plain", ".json"}
                    : attachment.type().getType().split(",");
            if (attachment.value() instanceof String) {
                Allure.addAttachment(
                        attachment.name(),
                        typeAndExtends[0],
                        attachment.value().toString(),
                        typeAndExtends[1]
                );
            } else {
                Allure.addAttachment(
                        attachment.name(),
                        typeAndExtends[0],
                        convertObjectToJson(attachment.value()),
                        typeAndExtends[1]
                );
            }
        }
    }

    /**
     * Метод прикрепляет вложение, с отличиями фактического значения от ожидаемого, к текущему шагу
     *
     * @param expected ожидаемый объект
     * @param actual   текущий объект
     */
    public static void attachment(Object expected, Object actual) {
        StringBuilder builder = new StringBuilder("""
                <html>
                <head>
                    <mata http-equiv="context-type" content="text/html; charset=utf-8">
                </head>
                <body>
                <pre>
                """);
        String[] arrayExpected = converterViewJson(expected != null
                ? converterStringToJsonNode(convertObjectToJson(actual)).toString()
                : "{null}"
        ).split("\n");
        String[] arrayActual = converterViewJson(actual != null
                ? converterStringToJsonNode(convertObjectToJson(actual)).toString()
                : "{null}"
        ).split("\n");
        for (int i = 0; i < arrayActual.length; i++) {
            int countOpen = (builder.toString().length() - builder.toString().replaceAll("<span", "").length()) / 5;
            int countClose = (builder.toString().length() - builder.toString().replaceAll("</span", "").length()) / 6;
            if (arrayActual[i].equals(i > arrayExpected.length - 1
                    ? ""
                    : arrayExpected[i])
            ) {
                if (countOpen != countClose) {
                    builder.replace(
                            builder.lastIndexOf("\r\n") == -1
                                    ? builder.lastIndexOf("\n")
                                    : builder.lastIndexOf("\r\n"),
                            builder.length(),
                            "</span>\n"
                    );
                }
                builder.append(arrayActual[i]).append("\n");
            } else {
                if (countOpen == countClose) {
                    builder.replace(
                            builder.lastIndexOf("\r\n") == -1
                                    ? builder.lastIndexOf("\n")
                                    : builder.lastIndexOf("\r\n"),
                            builder.length(),
                            "<span style=\"color: red\">\n");
                    builder.append(arrayActual[i]).append("\n");
                } else {
                    builder.append(arrayActual[i]).append("\n");
                }
            }
            if (i == arrayActual.length - 1) {
                if (countOpen != countClose) {
                    builder.replace(
                            builder.lastIndexOf("\n"),
                            builder.length(),
                            "</span>\n"
                    );
                }
                builder.append("""
                        </pre>
                        </body>
                        </html>""");
            }
        }
        attachment(Attachment.of(
                "полученное",
                builder.toString(),
                Type.HTML
        ));
    }

    /**
     * Метод возвращает название текущего шага
     *
     * @return название шага
     */
    public static String getCurrentStepName() {
        AtomicReference<String> name = new AtomicReference<>();
        Allure.getLifecycle().updateTestCase(testResult ->
                name.set(testResult.getSteps().get(testResult.getSteps().size() - 1).getName()));
        return name.get();
    }

    /**
     * Метод возвращает название текущего вложенного шага
     *
     * @return название вложенного шага
     */
    public static String getCurrentSubStepName() {
        AtomicReference<String> name = new AtomicReference<>();
        Allure.getLifecycle().updateStep(stepResult -> name.set(stepResult.getName()));
        return name.get();
    }

    /**
     * Метод формирует набор аргументов для сценария в параметризованном тесте<br/>
     * Тест может содержать 1...N сценариев
     *
     * @param supplier перечисление аргументов для сценария
     * @return набор аргументов для сценария
     */
    public static Arguments scenario(Supplier<Arguments> supplier) {
        return supplier.get();
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
