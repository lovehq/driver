package com.will.driver;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okio.BufferedSource;
import okio.Okio;
import okio.Source;

/**
 * @author Will Sun
 */
public class ConfigUtil {
    private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);

    private static Properties properties;

    static{
        properties = new Properties();
        try {
            properties.load(new FileInputStream("config.properties"));
        } catch (Exception e) {
            try{
                properties.load(ConfigUtil.class.getResourceAsStream("/config.properties"));
            }catch (Exception e1){
                logger.error("Can't find config.properties file");
                throw new RuntimeException(e1.getMessage());
            }
        }
    }

    private static Source source;
    private static Queue<LocalDateTime> bookingDates;

    static{
        try {
            source = Okio.source(new FileInputStream("booking.txt"));
        } catch (Exception e) {
            try{
                source = Okio.source(ConfigUtil.class.getResourceAsStream("/booking.txt"));
            }catch (Exception e1){
                logger.error("Can't find booking.txt file");
                throw new RuntimeException(e1.getMessage());
            }
        }
    }

    public static synchronized Queue<LocalDateTime> readBookingDates() {
        try{
            if(bookingDates != null){
                return bookingDates;
            }
            bookingDates = new LinkedList<>();
            BufferedSource bsource = Okio.buffer(source);
            String dateTime;
            while((dateTime = bsource.readUtf8Line()) != null){
                if(!dateTime.isEmpty()) {
                    bookingDates.offer(LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm")));
                }
            }
            bsource.close();
            source.close();
            return bookingDates;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private static Source coachSource;
    private static Map<String, String> allCoaches;
    static{
        try {
            coachSource = Okio.source(ConfigUtil.class.getResourceAsStream("/coaches.txt"));
        } catch (Exception e) {
            logger.error("Can't find coaches.txt file");
            throw new RuntimeException(e.getMessage());
        }
    }

    public static synchronized Map<String, String> getAllCoaches() {
        try{
            if(allCoaches != null){
                return allCoaches;
            }
            allCoaches = new LinkedHashMap<>();
            BufferedSource bsource = Okio.buffer(coachSource);
            String coach;
            while((coach = bsource.readUtf8Line()) != null){
                if(!coach.isEmpty()) {
                    String[] ss = coach.split(":");
                    allCoaches.put(ss[0], ss[1]);
                }
            }
            bsource.close();
            coachSource.close();
            return allCoaches;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static int getMinTime(){
        return Integer.parseInt((String) properties.getOrDefault("time.min", "13"));
    }

    public static int getMaxTime(){
        return Integer.parseInt((String) properties.getOrDefault("time.max", "19"));
    }

    public static long getRegWaitTime(){
        return Long.parseLong((String) properties.getOrDefault("wait.reg", "10000"));
    }

    public static long getFastWaitTime(){
        return Long.parseLong((String) properties.getOrDefault("wait.fast", "1000"));
    }

    public static String[] getCoaches(){
        String coaches = (String) properties.getOrDefault("coach", "9114045123");
        return coaches.split(",");
    }

    public static int getBookType(){
        return Integer.parseInt((String)properties.getOrDefault("book.type", "0"));
    }

    public static String getHost(){
        return (String) properties.getOrDefault("host", "t1.ronganjx.com");
    }

    public static void main(String[] args) {
//        String LINK_REGEX = ".+\"(BookingCWStudy.aspx.+date=(201\\d\\-\\d{2}\\-\\d{2}).*timeLine=(\\d+).*)\".*";
//        String s = "javascript:WebForm_DoPostBackWithOptions(new WebForm_PostBackOptions(\"ctl00$ContentPlaceHolder2$GridView1$ctl04$ctl00\", \"\", true, \"\", \"BookingCWStudy.aspx?coachName=9113037425&date=2016-11-05&beginTime=1000&trainType=%e5%9c%ba%e5%a4%96&timeLine=11\", false, true))";
//        Pattern pattern = Pattern.compile(LINK_REGEX);
//        Matcher m = pattern.matcher(s);
//        if(m.find()){
//                System.out.println(m.group(1));
//            System.out.println(m.group(2));
//            System.out.println(m.group(3));
//        }
        System.out.println(getAllCoaches());
    }

}
