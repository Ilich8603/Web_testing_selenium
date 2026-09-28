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

    boolean isInvalid(String testId) {
        return driver.findElement(By.cssSelector("[data-test-id=" + testId + "]"))
                .getAttribute("class").contains("input_invalid");
    }


    @Test // всё пусто → подсвечено только имя
    void shouldHighlightNameWhenAllEmpty() {
        submit();

        assertTrue(isInvalid("name"));
        assertFalse(isInvalid("phone"));
        assertFalse(isInvalid("agreement"));
    }

    @Test // имя валидно, телефон пустой → подсвечен только телефон
    void shouldHighlightPhoneWhenNameOk() {
        fillName("Иванов Иван");
        checkAgreement();
        submit();

        assertFalse(isInvalid("name"));
        assertTrue(isInvalid("phone"));
        assertFalse(isInvalid("agreement"));
    }

    @Test // имя и телефон валидны, чекбокс не отмечен → подсвечен только чекбокс
    void shouldHighlightAgreementWhenFieldsOk() {
        fillName("Иванов Иван");
        fillPhone("+79999999999");
        submit();

        assertFalse(isInvalid("name"));
        assertFalse(isInvalid("phone"));
        assertTrue(isInvalid("agreement"));
    }

    @Test // латиница в имени → имя подсвечено
    void shouldHighlightNameWhenLatinLetters() {
        fillName("Ivan");
        fillPhone("+79999999999");
        checkAgreement();
        submit();

        assertTrue(isInvalid("name"));
    }

    @Test // телефон без "+" → телефон подсвечен
    void shouldHighlightPhoneWhenNoPlus() {
        fillName("Иванов Иван");
        fillPhone("89999999999");
        checkAgreement();
        submit();

        assertTrue(isInvalid("phone"));
    }

    @Test // телефон с буквами → телефон подсвечен
    void shouldHighlightPhoneWhenLetters() {
        fillName("Иванов Иван");
        fillPhone("+7999999999a");
        checkAgreement();
        submit();

        assertTrue(isInvalid("phone"));
    }


}
