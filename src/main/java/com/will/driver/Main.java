package com.will.driver;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;
import okio.Source;

/**
 * @author Will Sun
 */
public class Main {
    public static final Logger logger = Logger.getLogger(Main.class.getName());

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

    public static final String SPAN_ERROR = "ctl00_ContentPlaceHolder2_lblInfo";

    private static OkHttpClient client = new OkHttpClient();

    private WebDriver driver;

    public Main() throws IOException {
        String exePath = "/chromedriver.exe";
        URL url = this.getClass().getResource(exePath);
        System.setProperty("webdriver.chrome.driver", url.getFile());
        driver = new ChromeDriver();
        driver.get("http://t1.ronganjx.com/Web11/logging/BookingCarStudy.aspx");
        if(!driver.getCurrentUrl().equals("http://t1.ronganjx.com/Web11/logging/BookingCarStudy.aspx")) {
            login();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new Main().bookCoach();
        //System.out.println(getBookingUrl(LocalDateTime.now()));
    }

    private void bookCoach() throws IOException, InterruptedException {
        Queue<LocalDateTime> dateTimes = readBookingDates();
        int size = 0, count = 0;
        while(true){
            if(dateTimes.isEmpty()){
                break;
            }
            size = dateTimes.size();
            LocalDateTime dateTime = dateTimes.poll();
            logger.info("Try to book: " + dateTime.toString());
            boolean booked = false;
            String url = getBookingUrl(dateTime);
            driver.navigate().to(url);
            WebElement bookingBtn = driver.findElement(By.id(BTN_BOOKING));
            WebElement errorSpan = driver.findElement(By.id(SPAN_ERROR));
            String errorMsg = errorSpan.getText();
            if(bookingBtn.getAttribute("disabled") != null
                && bookingBtn.getAttribute("disabled").equals("true")){
                logger.info("Can't book in " + dateTime.toString() + ": " + errorMsg);
            }else{
                try{
                    WebElement radio = driver.findElement(By.id(RADIO_BUS));
                    if(radio != null){
                        radio.click();
                    }
                }catch (NoSuchElementException elementException){
                    logger.info("Already selected bus");
                }
                bookingBtn = driver.findElement(By.id(BTN_BOOKING));
                bookingBtn.click();
                if(driver.getCurrentUrl().equals("http://t1.ronganjx.com/Web11/logging/BookingCarStudy.aspx")) {
                    booked = true;
                    logger.info("Successfully booked: " + dateTime);
                }
            }
            if(!booked){
                dateTimes.offer(dateTime);
            }

            count++;
            if(count >= size){
                count = 0;
                Thread.sleep(getSleepTime());
            }
        }
    }

    private long getSleepTime(){
        LocalDateTime now = LocalDateTime.now();
        if(now.getHour() == 12 && (now.getMinute() > 25 && now.getMinute() < 40)){
            return 1000L;
        }
        return 10000L;
    }

    private Queue<LocalDateTime> readBookingDates() throws IOException {
        Queue<LocalDateTime> result = new LinkedList<>();
        Source source = Okio.source(Main.class.getResourceAsStream("/booking"));
        BufferedSource bsource = Okio.buffer(source);
        String dateTime = null;
        while((dateTime = bsource.readUtf8Line()) != null){
            result.offer(
                LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm")));
        }
        return result;
    }

    private static String getBookingUrl(LocalDateTime dateTime){
        String date = dateTime.toLocalDate().format(DateTimeFormatter.ofPattern("uuuu-MM-dd"));
        String time = dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HHmm"));
        HttpUrl url = new HttpUrl.Builder().scheme("http")
            .host("t1.ronganjx.com")
            .addPathSegments("Web11/logging/BookingCNStudy.aspx")
            .addQueryParameter("coachName", COACH_ID)
            .addQueryParameter("date", date)
            .addQueryParameter("beginTime", time)
            .addQueryParameter("trainType", "%E5%9C%BA%E5%86%85")
            .addQueryParameter("timeLine", String.valueOf(dateTime.toLocalTime().getHour() + 1))
            .build();
        return url.toString();
    }

    private void login() throws IOException {
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

    private static String inputCaptcha(){
        Scanner reader = new Scanner(System.in);  // Reading from System.in
        System.out.println("Enter captcha: ");
        String result = reader.nextLine();
        reader.close();
        return result;
        //return "env0rkd";
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
//    private void acceptAlert(){
//        Wait wait = new WebDriverWait(driver, 10);
//        wait.until(ExpectedConditions.alertIsPresent());
//        Alert alert = driver.switchTo().alert();
//        alert.accept();
//    }
//    private void selectDate(String date){
//        WebElement trainType = driver.findElement(By.id(SELECT_TRAIN_TYPE));
//        trainType.click();
//        trainType.findElement(By.cssSelector("option:nth-child(2)")).click();
//        acceptAlert();
//
//        WebElement trainDate = driver.findElement(By.id(INPUT_TRAIN_DATE));
//        trainDate.sendKeys(date);
//
//        trainDate.submit();
//    }
//
//    private void pickTime(String date, String time){
//        String[] keywords = new String[3];
//        keywords[0] = "coachName=9112022172";
//        keywords[1] = "date=" + date;
//        keywords[2] = "beginTime=" + time;
//
//        WebElement coachInfo = driver.findElement(By.id(TABLE_COACH_INFO));
//        WebElement bookLink = null;
//        for(WebElement element : coachInfo.findElements(By.tagName("a"))){
//            String href = element.getAttribute("href");
//            boolean match = true;
//            for(String keyword : keywords){
//                if(!href.contains(keyword)){
//                    match = false;
//                }
//            }
//            if(match){
//                bookLink = element;
//                break;
//            }
//        }
//        if(bookLink == null){
//            System.out.println("No matched booking time found.");
//        }else{
//            bookLink.click();
//        }
//    }
//
//    private void bookCoach() throws IOException {
//        driver.get("hhttp://t1.ronganjx.com/Web11/logging/BookingCarStudy.aspx");
//        if(!driver.getCurrentUrl().equals("http://t1.ronganjx.com/Web11/logging/BookingCarStudy.aspx")) {
//            login();
//        }
//        selectDate("2016-10-15");
//        pickTime("2016-10-15", "0700");
//
//        driver.findElement(By.id(RADIO_BUS)).click();
//        driver.findElement(By.id(BTN_BOOKING)).click();
//    }

}
