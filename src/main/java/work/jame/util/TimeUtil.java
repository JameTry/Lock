package work.jame.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author : Jame
 * @date : 2022-06-10 14:58
 **/
public class TimeUtil {


    public static String timestampCastStringTime(long timestamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSSS");
        Date date = new Date(timestamp);
        return simpleDateFormat.format(date);
    }

}
