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

}
