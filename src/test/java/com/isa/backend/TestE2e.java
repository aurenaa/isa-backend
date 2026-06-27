package com.isa.backend;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestE2e {

    private WebDriver driver;
    private final String APPLICATION_URL = "http://localhost:4200";

    @BeforeEach
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void login(String username, String password) {
        driver.get(APPLICATION_URL);
        driver.findElement(By.className("btn-pink")).click();
        driver.findElement(By.name("username")).sendKeys(username);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }

    @Test
    public void test_successful_login() {
        login("username", "password123");
        assertTrue(driver.findElement(By.className("profile-btn")).isDisplayed());
    }

    @Test
    public void test_unsuccessful_login() {
        login("username", "password321");
        assertTrue(driver.findElement(By.className("alert-danger")).isDisplayed());
    }

    @Test
    public void test_successful_video_upload() {
        login("username", "password123");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("profile-btn")));

        driver.findElement(By.className("upload-button")).click();
        driver.findElement(By.name("title")).sendKeys("My Test Video");
        driver.findElement(By.name("description")).sendKeys("Video description");

        List<WebElement> fileInputs = driver.findElements(By.cssSelector("input[type='file']"));

        String videoPath = "C:\\Users\\Nevenica\\Downloads\\YTDown_YouTube_Supergirl-Official-Trailer_Media_s1-pfiVMKAs_004_360p.mp4";
        String thumbnailPath = "C:\\Users\\Nevenica\\Downloads\\s1-pfiVMKAs-SD.jpg";

        fileInputs.get(0).sendKeys(videoPath);
        fileInputs.get(1).sendKeys(thumbnailPath);

        driver.findElement(By.name("title")).submit();

        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        driver.get(APPLICATION_URL);
        assertTrue(driver.findElement(By.xpath("//*[contains(text(), 'My Test Video')]")).isDisplayed());
    }

    @Test
    public void test_adding_comment_to_video() {
        login("username", "password123");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("profile-btn")));

        driver.get(APPLICATION_URL + "/video/1");

        WebElement commentInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("comment-input")));

        String commentText = "Test comment.";
        commentInput.sendKeys(commentText + Keys.ENTER);
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[contains(text(), '" + commentText + "')]"))).isDisplayed());
    }
}