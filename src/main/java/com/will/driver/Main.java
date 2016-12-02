package com.will.driver;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 * @author Will Sun
 */
public class Main {
    public static final Logger logger = LoggerFactory.getLogger(Main.class);

    //private static final String COACH_ID = "9114045123"; //9113026133

    private static final String[] coaches = ConfigUtil.getCoaches();

    private static final Queue<LocalDateTime> dateTimes = ConfigUtil.readBookingDates();

    private static final Map<String, String> allCoaches = ConfigUtil.getAllCoaches();

    private static final String host = ConfigUtil.getHost();
    private static final String baseUrl = "http://" + host + "/Web11/logging/";

    private static final int bookType = ConfigUtil.getBookType();

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String LINK_REGEX = ".+\"(BookingCWStudy.aspx.+date=(201\\d\\-\\d{2}\\-\\d{2}).*timeLine=(\\d+).*)\".*";

    // private static final String IMAGE_FILE_PATH = Main.class.getResource("/image").getFile() +
    // "/captcha.jpg";

    private static final String INPUT_USER_NAME = "ctl00_ContentPlaceHolder1_txtPupilNo";
    private static final String INPUT_PASSWORD = "ctl00_ContentPlaceHolder1_txtWebPwd";
    private static final String INPUT_CAPTCHA = "ctl00_ContentPlaceHolder1_txtCode";
    public static final String BTN_LOGIN = "ctl00_ContentPlaceHolder1_ibtnLogin";

    public static final String SELECT_TRAIN_TYPE = "ctl00_ContentPlaceHolder2_ddlTrainType";
    public static final String INPUT_TRAIN_DATE = "ctl00_ContentPlaceHolder2_txtBookingClassDate";

    public static final String TABLE_COACH_INFO = "ctl00_ContentPlaceHolder2_gvCoachInfo";
    //public static final String TABLE_COACH_INFO = "ctl00_ContentPlaceHolder2_GridView1";

    public static final String RADIO_BUS = "ctl00_ContentPlaceHolder2_radio3";
    public static final String BTN_BOOKING = "ctl00_ContentPlaceHolder2_btnSubmit";

    public static final String SPAN_ERROR = "ctl00_ContentPlaceHolder2_lblInfo";

    private static OkHttpClient client = new OkHttpClient();

    private static WebDriver driver;

    static {
        String exePath = "/chromedriver.exe";
        URL exeUrl = Main.class.getResource(exePath);
        exePath = exeUrl.getFile();
        if (!new File(exePath).exists()) {
            exePath = "chromedriver.exe";
        }

        System.setProperty("webdriver.chrome.driver", exePath);
        driver = new ChromeDriver();
    }

    public Main() throws IOException {
        driver.get(baseUrl + "BookingCarStudy.aspx");
        if (!driver.getCurrentUrl()
            .equals(baseUrl + "BookingCarStudy.aspx")) {
            login();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        try{
            Main main = new Main();
            while (true) {
                if(bookType == 0){
                    main.bookByAvailableTime();
                }else{
                    main.bookByDates();
                }
                Thread.sleep(main.getSleepTime());
            }

//            main.findAllAvilableDates(LocalDate.of(2016, 11, 27));
//            main.findAllAvilableDates(LocalDate.of(2016, 11, 28));
//            main.findAllAvilableDates(LocalDate.of(2016, 11, 29));
//            main.findAllAvilableDates(LocalDate.of(2016, 11, 30));
//            main.findAllAvilableDates(LocalDate.of(2016, 12, 1));
//            main.findAllAvilableDates(LocalDate.of(2016, 12, 2));
//            main.findAllAvilableDates(LocalDate.of(2016, 12, 3));
//            main.findAllAvilableDates(LocalDate.of(2016, 12, 4));
        }catch (Exception e){
            driver.close();
        }
        driver.close();
    }

    private void bookByAvailableTime() {
        LocalDate localDate = LocalDate.now().plusDays(7);
        driver.get(baseUrl + "BookingCarStudy.aspx");
        try {
            if(!selectDate(localDate)){
                return;
            }
            WebElement tableEle = driver.findElement(By.id(TABLE_COACH_INFO));
            List<WebElement> linkEles = tableEle.findElements(By.cssSelector("td a"));
            if(linkEles.isEmpty()){
                logger.info("No available booking time");
                return;
            }
            LinkedList<String> links = new LinkedList<>();

            for (WebElement element : linkEles) {
                String link = element.getAttribute("href");
                Pattern pattern = Pattern.compile(LINK_REGEX);
                Matcher m = pattern.matcher(link);
                if (m.find()) {
//                    LocalDate selectDate = LocalDate.parse(m.group(2), DATE_TIME_FORMATTER);
//                    if (selectDate.compareTo(localDate) > 0) {
                        int selectTime = Integer.parseInt(m.group(3));
                        if (selectTime > ConfigUtil.getMinTime() && selectTime < ConfigUtil.getMaxTime()) {
                            links.addFirst(baseUrl + m.group(1));
                        }
//                    }
                }
            }

            if(!links.isEmpty()){
                Iterator<String> it = links.iterator();
                while(it.hasNext()){
                    String link = it.next();
                    logger.info("Try to book: {}", link);
                    try{
                        driver.navigate().to(link);
                        bookCoach();
                        it.remove();
                    }catch (Exception e1){
                        logger.error("Failed to book: {}", link);
                        e1.printStackTrace();
                    }
                }


                for(String link : links){
                    logger.info("Try to book: {}", link);
                    try{
                        driver.navigate().to(link);
                        bookCoach();
                    }catch (Exception e1){
                        logger.error("Failed to book: {}", link);
                        e1.printStackTrace();
                    }
                }
            }else{
                logger.info("No matched booking time");
            }
        } catch (UnhandledAlertException e){
            acceptAlert();
            return;
        }
        catch (Exception e) {
            logger.error("Unexpected exception:");
            e.printStackTrace();
        }
    }

    private void bookByDates() throws IOException, InterruptedException {
        if (dateTimes == null || dateTimes.isEmpty()) {
            return;
        }
        try {
            for(String coach : coaches){
                for (LocalDateTime dateTime : dateTimes) {
                    logger.info("Try to book {}: {}", coach, dateTime.toString());
                    String url = getBookingUrl(dateTime, coach);
                    driver.navigate().to(url);
                    bookCoach();
                }
            }

        } catch (Exception e) {
            logger.error("Failed to book: {}", e.getMessage());
        }
    }

    private void findAllAvilableDates(LocalDate date) throws InterruptedException {
        System.out.println("Finding: " + date.toString());
        for(Map.Entry<String, String> coach : allCoaches.entrySet()){
            System.out.print(coach.getValue() + ": ");
            for(int i = 7; i < 22; i++){
                int minute = 0;
                if(i > 11 && i < 17){
                    minute = 30;
                }
                LocalDateTime localDateTime = LocalDateTime.of(date, LocalTime.of(i, minute));
                String url = getBookingUrl(localDateTime, coach.getKey());
                driver.navigate().to(url);
                WebElement bookingBtn = driver.findElement(By.id(BTN_BOOKING));
                if (bookingBtn.getAttribute("disabled") != null
                    && bookingBtn.getAttribute("disabled").equals("true")) {
                    logger.debug("Coach not available {}: {}", coach.getValue(), localDateTime);
                }else{
                    System.out.print(
                        localDateTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " ");
                }
                Thread.sleep(200L);
            }
            System.out.println();
        }
    }

    private long getSleepTime() {
        LocalDateTime now = LocalDateTime.now();
        if (now.getHour() == 12 && (now.getMinute() > 25 && now.getMinute() < 40)) {
            return ConfigUtil.getFastWaitTime();
        }
        return ConfigUtil.getRegWaitTime();
    }

    private static String getBookingUrl(LocalDateTime dateTime, String coach) {
        String date = dateTime.toLocalDate().format(DateTimeFormatter.ofPattern("uuuu-MM-dd"));
        String time = dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HHmm"));
        HttpUrl url = new HttpUrl.Builder().scheme("http").host(host)
            .addPathSegments("Web11/logging/BookingCWStudy.aspx")
            .addQueryParameter("coachName", coach)
            .addQueryParameter("date", date)
            .addQueryParameter("beginTime", time)
            .addQueryParameter("trainType", "%E5%9C%BA%E5%A4%96")
            .addQueryParameter("timeLine", String.valueOf(dateTime.toLocalTime().getHour() + 1))
            .build();
        return url.toString();
    }

    private void login() throws IOException {
        // List<WebElement> elements = driver.findElements(By.tagName("img"));
        // WebElement imageEle = null;
        // for(WebElement element : elements){
        // String src = element.getAttribute("src");
        // if(src.toLowerCase().contains("/Web11/logging/LoginUser.aspx".toLowerCase())){
        // imageEle = element;
        // break;
        // }
        // }
        // String imageUrl = imageEle.getAttribute("src");
        // saveImage(imageUrl);
        WebElement userEle = driver.findElement(By.id(INPUT_USER_NAME));
        WebElement pwdEle = driver.findElement(By.id(INPUT_PASSWORD));
        WebElement captchaEle = driver.findElement(By.id(INPUT_CAPTCHA));
        WebElement loginBtn = driver.findElement(By.id(BTN_LOGIN));

        userEle.sendKeys("06085352");
        pwdEle.sendKeys("590577");
        captchaEle.sendKeys(inputCaptcha());

        loginBtn.click();
    }

    private static String inputCaptcha() {
        Scanner reader = new Scanner(System.in); // Reading from System.in
        System.out.println("Enter captcha: ");
        String result = reader.nextLine();
        reader.close();
        return result;
    }


    // private static void saveImage(String url) throws IOException {
    // Request request = new Request.Builder().url(url).build();
    // Response response = client.newCall(request).execute();
    // byte[] bytes = response.body().bytes();
    // Sink sink = Okio.sink(new File(IMAGE_FILE_PATH));
    // BufferedSink bsink = Okio.buffer(sink);
    // bsink.write(bytes);
    // response.close();
    // bsink.close();
    // }
    private void acceptAlert() {
        Wait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.alertIsPresent());
        Alert alert = driver.switchTo().alert();
        alert.accept();
    }

    private boolean selectDate(LocalDate localDate) {
        String date = localDate.format(DATE_TIME_FORMATTER);
        //logger.info("Select date: {}", date);
        WebElement trainType = driver.findElement(By.id(SELECT_TRAIN_TYPE));
        trainType.click();
        trainType.findElement(By.cssSelector("option:nth-child(3)")).click();
        acceptAlert();

        WebElement trainDate = driver.findElement(By.id(INPUT_TRAIN_DATE));
        trainDate.sendKeys(date);

        try{
            trainDate.submit();
        } catch (UnhandledAlertException e){
            acceptAlert();
            return false;
        }
        return true;
    }

    // private void pickTime(String date, String time){
    // String[] keywords = new String[3];
    // keywords[0] = "coachName=9112022172";
    // keywords[1] = "date=" + date;
    // keywords[2] = "beginTime=" + time;
    //
    // WebElement coachInfo = driver.findElement(By.id(TABLE_COACH_INFO));
    // WebElement bookLink = null;
    // for(WebElement element : coachInfo.findElements(By.tagName("a"))){
    // String href = element.getAttribute("href");
    // boolean match = true;
    // for(String keyword : keywords){
    // if(!href.contains(keyword)){
    // match = false;
    // }
    // }
    // if(match){
    // bookLink = element;
    // break;
    // }
    // }
    // if(bookLink == null){
    // System.out.println("No matched booking time found.");
    // }else{
    // bookLink.click();
    // }
    // }

    private boolean bookCoach() throws IOException {
        boolean booked = false;
        String currentUrl = driver.getCurrentUrl();
        WebElement bookingBtn = driver.findElement(By.id(BTN_BOOKING));
        WebElement errorSpan = driver.findElement(By.id(SPAN_ERROR));
        String errorMsg = errorSpan.getText();
        if (bookingBtn.getAttribute("disabled") != null
            && bookingBtn.getAttribute("disabled").equals("true")) {
            logger.info("Coach not available {}: {}", currentUrl, errorMsg);
        } else {
            try {
                WebElement radio = driver.findElement(By.id(RADIO_BUS));
                if (radio != null) {
                    radio.click();
                }
            } catch (NoSuchElementException elementException) {
                logger.info("Already selected bus");
            }
            Wait wait = new WebDriverWait(driver, 10);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("ctl00_ContentPlaceHolder2_times")));
            bookingBtn = driver.findElement(By.id(BTN_BOOKING));
            bookingBtn.click();
            if (driver.getCurrentUrl()
                .equals(baseUrl + "BookingCarStudy.aspx")) {
                booked = true;
                logger.info("Success booked: {}", currentUrl);
            }
        }
        return booked;
    }

}
