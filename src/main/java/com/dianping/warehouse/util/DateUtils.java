package com.dianping.warehouse.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created with IntelliJ IDEA.
 * User: yxn
 * Date: 14-1-8
 * Time: 上午11:38
 * To change this template use File | Settings | File Templates.
 */
public class DateUtils {
    public static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    public static DateTimeFormatter formatterDatabase = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SS");
    public static DateTime getCurrentTime() {
        return new DateTime();
    }
    public static DateTime getTargetTime(String process_day, String time){
        return formatter.parseDateTime(process_day+" "+time);
    }
    public static DateTime getEndTime(String process_day){
        DateTime latest = formatter.parseDateTime(process_day+" "+Constants.endTime);
        return latest;
    }
    public static String getDefaultDay(){
        return getCurrentTime().toString("yyyy-MM-dd");
    }

}
