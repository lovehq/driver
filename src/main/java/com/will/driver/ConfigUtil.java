package com.will.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okio.BufferedSource;
import okio.Okio;
import okio.Source;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Will Sun
 */
public class ConfigUtil {
    private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);

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

    public static synchronized Queue<LocalDateTime> readBookingDates() throws IOException {
        if(bookingDates != null){
            return bookingDates;
        }
        Queue<LocalDateTime> result = new LinkedList<>();
        BufferedSource bsource = Okio.buffer(source);
        String dateTime;
        while((dateTime = bsource.readUtf8Line()) != null){
            if(!dateTime.isEmpty()) {
                result.offer(LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm")));
            }
        }
        bsource.close();
        source.close();
        return result;
    }

    public static void main(String[] args) {
        String LINK_REGEX = ".+\"(BookingCWStudy.aspx.+date=(201\\d\\-\\d{2}\\-\\d{2}).*timeLine=(\\d+).*)\".*";
        String s = "javascript:WebForm_DoPostBackWithOptions(new WebForm_PostBackOptions(\"ctl00$ContentPlaceHolder2$GridView1$ctl04$ctl00\", \"\", true, \"\", \"BookingCWStudy.aspx?coachName=9113037425&date=2016-11-05&beginTime=1000&trainType=%e5%9c%ba%e5%a4%96&timeLine=11\", false, true))";
        Pattern pattern = Pattern.compile(LINK_REGEX);
        Matcher m = pattern.matcher(s);
        if(m.find()){
                System.out.println(m.group(1));
            System.out.println(m.group(2));
            System.out.println(m.group(3));
        }
    }

}
