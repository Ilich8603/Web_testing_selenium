package ru.netology.web;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class OrderCardValidationTest {

    private WebDriver driver;

    @BeforeAll
    static void setupAll() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-dev-shm-usage", "--no-sandbox", "--headless");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.get("http://localhost:9999");
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }



    void fillName(String value) {
        driver.findElement(By.cssSelector("[data-test-id=name] input")).sendKeys(value);
    }
    void fillPhone(String value) {
        driver.findElement(By.cssSelector("[data-test-id=phone] input")).sendKeys(value);
    }
    void checkAgreement() {
        driver.findElement(By.cssSelector("[data-test-id=agreement]")).click();
    }
    void submit() {
        driver.findElement(By.cssSelector("form button.button")).click();
    }

    String errorMessage(String testId) {
        var list = driver.findElements(By.cssSelector(
                "[data-test-id='" + testId + "'].input_invalid .input__sub"));
        return list.isEmpty() ? "" : list.get(0).getText().trim();
    }


    @Test // всё пусто → подсвечено только имя
    void shouldHighlightOnlyNameWhenAllEmpty() {
        submit();

        String actual   = errorMessage("name");
        String expected = "Поле обязательно для заполнения";

        assertEquals(expected, actual);
        assertEquals("", errorMessage("phone"));
        assertEquals("", errorMessage("agreement"));
    }

    @Test // имя валидно, телефон пустой → подсвечен только телефон
    void shouldHighlightPhoneWhenNameOk() {
        fillName("Иванов Иван");
        checkAgreement();
        submit();

        String actual   = errorMessage("phone");
        String expected = "Поле обязательно для заполнения";

        assertEquals(expected, actual);
        assertEquals("", errorMessage("name"));
        assertEquals("", errorMessage("agreement"));
    }

    @Test // имя и телефон валидны, чекбокс не отмечен → подсвечен только чекбокс
    void shouldHighlightAgreementWhenFieldsOk() {
        fillName("Иванов Иван");
        fillPhone("+79999999999");
        submit();

        String actual = driver.findElement(By.cssSelector("[data-test-id=agreement]"))
                .getAttribute("class")
                .contains("input_invalid") ? "true" : "false";
        String expected = "true";

        assertEquals(expected, actual);
        assertEquals("", errorMessage("name"));
        assertEquals("", errorMessage("phone"));
    }

    @Test // латиница в имени → имя подсвечено
    void shouldHighlightNameWhenLatinLetters() {
        fillName("Ivan");
        fillPhone("+79999999999");
        checkAgreement();
        submit();

        String actual   = errorMessage("name");
        String expected = "Имя и Фамилия указаные неверно. Допустимы только русские буквы, пробелы и дефисы.";

        assertEquals(expected, actual);
        assertEquals("", errorMessage("phone"));
        assertEquals("", errorMessage("agreement"));
    }

    @Test // не буквы в имени → имя подсвечено
    void shouldHighlightNameWhenNotLetters() {
        fillName("1van");
        fillPhone("+79999999999");
        checkAgreement();
        submit();

        String actual   = errorMessage("name");
        String expected = "Имя и Фамилия указаные неверно. Допустимы только русские буквы, пробелы и дефисы.";

        assertEquals(expected, actual);
        assertEquals("", errorMessage("phone"));
        assertEquals("", errorMessage("agreement"));
    }

    @Test // телефон без "+" → телефон подсвечен
    void shouldHighlightPhoneWhenNoPlus() {
        fillName("Иванов Иван");
        fillPhone("89999999999");
        checkAgreement();
        submit();

        String actual   = errorMessage("phone");
        String expected = "Телефон указан неверно. Должно быть 11 цифр, например, +79012345678.";

        assertEquals(expected, actual);
        assertEquals("", errorMessage("name"));
        assertEquals("", errorMessage("agreement"));

    }

    @Test // телефон меньше 11 цифр → телефон подсвечен
    void shouldHighlightPhoneWhenLessThanEleven() {
        fillName("Иванов Иван");
        fillPhone("+8997996959");
        checkAgreement();
        submit();

        String actual   = errorMessage("phone");
        String expected = "Телефон указан неверно. Должно быть 11 цифр, например, +79012345678.";

        assertEquals(expected, actual);
        assertEquals("", errorMessage("name"));
        assertEquals("", errorMessage("agreement"));

    }

    @Test // телефон больше 11 цифр → телефон подсвечен
    void shouldHighlightPhoneWhenMoreThanEleven() {
        fillName("Иванов Иван");
        fillPhone("+899799695949");
        checkAgreement();
        submit();

        String actual   = errorMessage("phone");
        String expected = "Телефон указан неверно. Должно быть 11 цифр, например, +79012345678.";

        assertEquals(expected, actual);
        assertEquals("", errorMessage("name"));
        assertEquals("", errorMessage("agreement"));

    }

    @Test // телефон с буквами → телефон подсвечен
    void shouldHighlightPhoneWhenLetters() {
        fillName("Иванов Иван");
        fillPhone("+79999999a99");
        checkAgreement();
        submit();

        String actual   = errorMessage("phone");
        String expected = "Телефон указан неверно. Должно быть 11 цифр, например, +79012345678.";

        assertEquals(expected, actual);
        assertEquals("", errorMessage("name"));
        assertEquals("", errorMessage("agreement"));
    }


    @Test // телефон только с "+" → телефон подсвечен
    void shouldHighlightPhoneWhenOnlyPlus() {
        fillName("Иванов Иван");
        fillPhone("+");
        checkAgreement();
        submit();

        String actual   = errorMessage("phone");
        String expected = "Телефон указан неверно. Должно быть 11 цифр, например, +79012345678.";

        assertEquals(expected, actual);
        assertEquals("", errorMessage("name"));
        assertEquals("", errorMessage("agreement"));
    }

}
