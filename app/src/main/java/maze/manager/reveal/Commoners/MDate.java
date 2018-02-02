package maze.manager.reveal.Commoners;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Monroe on 6/25/2016.
 */
public class MDate  {

    private Date date = null;
    SimpleDateFormat smartFormat = new SimpleDateFormat("EEE, MMM d, yyyy");
    SimpleDateFormat displayFormat = new SimpleDateFormat("d MMM yyyy");
    SimpleDateFormat detailFormat = new SimpleDateFormat("dd MMM, h:mm a");
    SimpleDateFormat normalFormat = new SimpleDateFormat("dd/MM/yyyy");
    SimpleDateFormat format = new SimpleDateFormat("d/M/yyyy");//NEVER CHANGE THIS
    SimpleDateFormat shortMonth = new SimpleDateFormat("MMM d");
    SimpleDateFormat shortFormat = new SimpleDateFormat("EEE dd");
    SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM");
    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
    SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
    SimpleDateFormat saveFormat = new SimpleDateFormat("MM_dd_mm_ss");
    Calendar cal = Calendar.getInstance(),today = Calendar.getInstance();

    public MDate(String ... dates) {
        String dateText = dates.length == 0?System.currentTimeMillis()+"":dates[0];
        try {
            date = format.parse(dateText);
        } catch (ParseException e) {
        }
        try {
            if (date == null){
                date = normalFormat.parse(dateText);
            }
        } catch (ParseException e) {

        }
        try {
            if (date == null){
                date = smartFormat.parse(dateText);
            }
        } catch (ParseException e) {

        }
        try {
            if (date == null){ date = format.parse(dateText);}
        } catch (ParseException e) {

        }
        try {
            if (date == null){ date = timeFormat.parse(dateText);}
        } catch (ParseException e) {

        }
        try {
            if (date == null){
                date = new Date(Long.valueOf(dateText));
            }
        } catch (Exception e) {

        }
        cal.setTime(date);
    }

    public String getMonth(){
        return monthFormat.format(date);
    }


    public String getSaveDate(){
        return saveFormat.format(date);
    }

    public String getShort(){
        return shortFormat.format(date);
    }

    public String getDay(){
        return dayFormat.format(date);
    }

    public String getFormat(){
        return format.format(date);
    }

    public String getShortMonth(){
        return shortMonth.format(date);
    }

    public String smartDate(){
        return smartFormat.format(date);
    }

    public String displayDate(){
        return displayFormat.format(date);
    }

    public String detailDate(){
        return detailFormat.format(date);
    }
    public String getTime(){
        return timeFormat.format(date);
    }

    public long getTimeValue(){
        return date.getTime();
    }

    public int getDayOfYear(){
        return cal.get(Calendar.DAY_OF_YEAR);
    }

    public int getHour(){
        return cal.get(Calendar.HOUR);
    }

    public String getRelativeTime(){
        int then = cal.get(Calendar.DAY_OF_YEAR),now = today.get(Calendar.DAY_OF_YEAR);
        if (then == now){
            return getTime();
        }
        if (then < now){
            if ((now-then)==1){
                return getTime();
            }
            if ((now-then)<7){
                return getShort();
            }
        }
        return getShortMonth();
    }

    public int getHourOfDay(){
        return cal.get(Calendar.HOUR_OF_DAY);
    }


    public int getMinutes(){
        return cal.get(Calendar.MINUTE);
    }

}
