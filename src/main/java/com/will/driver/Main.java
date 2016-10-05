package com.will.driver;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

import okhttp3.HttpUrl;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

/**
 * @author Will Sun
 */
public class Main {
    public static final String COACH_ID = "9112022172";

    private static final String IMAGE_FILE_PATH = Main.class.getResource("/image").getFile() + "/captcha.jpg";

    private static final String INPUT_USER_NAME = "ctl00_ContentPlaceHolder1_txtPupilNo";
    private static final String INPUT_PASSWORD = "ctl00_ContentPlaceHolder1_txtWebPwd";
    private static final String INPUT_CAPTCHA = "ctl00_ContentPlaceHolder1_txtCode";
    public static final String BTN_LOGIN = "ctl00_ContentPlaceHolder1_ibtnLogin";

    public static final String SELECT_TRAIN_TYPE = "ctl00_ContentPlaceHolder2_ddlTrainType";
    public static final String INPUT_TRAIN_DATE = "ctl00_ContentPlaceHolder2_txtBookingClassDate";

    public static final String TABLE_COACH_INFO = "ctl00_ContentPlaceHolder2_gvCoachInfo";

    public static final String RADIO_BUS = "ctl00_ContentPlaceHolder2_radio3";
    public static final String BTN_BOOKING = "ctl00_ContentPlaceHolder2_btnSubmit";

    private static OkHttpClient client = new OkHttpClient();

    private static WebDriver driver;

    private Main() throws IOException {
        String exePath = "/chromedriver.exe";
        URL url = Main.class.getResource(exePath);
        System.setProperty("webdriver.chrome.driver", url.getFile());
        driver = new ChromeDriver();
        driver.get("http://t1.ronganjx.com/Web11/logging/BookingCarStudy.aspx");
        if(!driver.getCurrentUrl().equals("http://t1.ronganjx.com/Web11/logging/BookingCarStudy.aspx")) {
            login();
        }
    }

    private static String getBookingUrl(String date, String time){
        HttpUrl url = new HttpUrl.Builder().scheme("http")
            .host("t1.ronganjx.com")
            .addPathSegments("Web11/logging/BookingCNStudy.aspx")
            .addQueryParameter("coachName", COACH_ID)
            .addQueryParameter("date", date)
            .addQueryParameter("beginTime", time).build();
        return url.toString();
    }


    private static void saveImage(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        byte[] bytes = response.body().bytes();
        Sink sink = Okio.sink(new File(IMAGE_FILE_PATH));
        BufferedSink bsink = Okio.buffer(sink);
        bsink.write(bytes);
        response.close();
        bsink.close();
    }

    private static String inputCaptcha(){
        Scanner reader = new Scanner(System.in);  // Reading from System.in
        System.out.println("Enter captcha: ");
        String result = reader.nextLine();
        reader.close();
        return result;
        //return "env0rkd";
    }

    private static void acceptAlert(){
        Wait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.alertIsPresent());
        Alert alert = driver.switchTo().alert();
        alert.accept();
    }

    private static void login() throws IOException {
//        List<WebElement> elements = driver.findElements(By.tagName("img"));
//        WebElement imageEle = null;
//        for(WebElement element : elements){
//            String src = element.getAttribute("src");
//            if(src.toLowerCase().contains("/Web11/logging/LoginUser.aspx".toLowerCase())){
//                imageEle = element;
//                break;
//            }
//        }
//        String imageUrl = imageEle.getAttribute("src");
//        saveImage(imageUrl);
        WebElement userEle = driver.findElement(By.id(INPUT_USER_NAME));
        WebElement pwdEle = driver.findElement(By.id(INPUT_PASSWORD));
        WebElement captchaEle = driver.findElement(By.id(INPUT_CAPTCHA));
        WebElement loginBtn = driver.findElement(By.id(BTN_LOGIN));

        userEle.sendKeys("06085352");
        pwdEle.sendKeys("590577");
        captchaEle.sendKeys(inputCaptcha());

        loginBtn.click();
    }

    private static void selectDate(String date){
        WebElement trainType = driver.findElement(By.id(SELECT_TRAIN_TYPE));
        trainType.click();
        trainType.findElement(By.cssSelector("option:nth-child(2)")).click();
        acceptAlert();

        WebElement trainDate = driver.findElement(By.id(INPUT_TRAIN_DATE));
        trainDate.sendKeys(date);

        trainDate.submit();
    }

    private static void pickTime(String date, String time){
        String[] keywords = new String[3];
        keywords[0] = "coachName=9112022172";
        keywords[1] = "date=" + date;
        keywords[2] = "beginTime=" + time;

        WebElement coachInfo = driver.findElement(By.id(TABLE_COACH_INFO));
        WebElement bookLink = null;
        for(WebElement element : coachInfo.findElements(By.tagName("a"))){
            String href = element.getAttribute("href");
            boolean match = true;
            for(String keyword : keywords){
                if(!href.contains(keyword)){
                    match = false;
                }
            }
            if(match){
                bookLink = element;
                break;
            }
        }
        if(bookLink == null){
            System.out.println("No matched booking time found.");
        }else{
            bookLink.click();
        }
    }

    private static void bookCoach() throws IOException {
        driver.get("hhttp://t1.ronganjx.com/Web11/logging/BookingCarStudy.aspx");
        if(!driver.getCurrentUrl().equals("http://t1.ronganjx.com/Web11/logging/BookingCarStudy.aspx")) {
            login();
        }
        selectDate("2016-10-15");
        pickTime("2016-10-15", "0700");

        driver.findElement(By.id(RADIO_BUS)).click();
        driver.findElement(By.id(BTN_BOOKING)).click();
    }


    public static void main(String[] args) throws IOException {





        //driver.close();
    }
}
