package services.util;

import io.qameta.allure.Allure;
import lombok.experimental.UtilityClass;

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
}
