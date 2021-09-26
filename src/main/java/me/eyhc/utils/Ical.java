package me.eyhc.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Dictionary;

public class Ical {
    BufferedWriter calStr;
    File file;
    public Ical(String fileName) throws IOException {
        file = new File(fileName + ".ics");
        if (!file.exists()) {
            file.createNewFile();
        }
        calStr = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
        calStr.write("BEGIN:VCALENDAR\n");
        calStr.write("VERSION:2.0\n");
        calStr.write("PRODID:-//UW Schedule parser by eyhc1\n");
        calStr.write("BEGIN:VTIMEZONE\n");
        calStr.write("TZID:America/Los_Angeles\n");
        calStr.write("BEGIN:STANDARD\n");
        calStr.write("DTSTART:19710101T020000\n");
        calStr.write("TZOFFSETTO:-0800\n");
        calStr.write("TZOFFSETFROM:-0700\n");
        calStr.write("TZNAME:PST\n");
        calStr.write("RRULE:FREQ=YEARLY;BYMONTH=11;BYDAY=1SU\n");
        calStr.write("END:STANDARD\n");
        calStr.write("BEGIN:DAYLIGHT\n");
        calStr.write("DTSTART:19710101T020000\n");
        calStr.write("TZOFFSETTO:-0700\n");
        calStr.write("TZOFFSETFROM:-0800\n");
        calStr.write("TZNAME:PDT\n");
        calStr.write("RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=2SU\n");
        calStr.write("END:DAYLIGHT\n");
        calStr.write("END:VTIMEZONE\n");
    }

    public void close() throws IOException {
        calStr.write("END:VCALENDAR\n");
        calStr.close();
    }

    public void event(String summary,
                      String startDate,
                      String endDate,
                      String startTime,
                      String endTime,
                      String days,
                      float lat,
                      float lng,
                      String discription)
            throws IOException {
        calStr.write("BEGIN:VEVENT\n");
        calStr.write("RRULE:FREQ=WEEKLY;BYDAY=" + days + ";UNTIL=" + endDate + "T230000Z\n");
        calStr.write("DTSTART:" + startDate + "T" + startTime + "00\n");
        calStr.write("DTEND:" + startDate + "T" + endTime + "00\n");
        calStr.write("SUMMARY:" + summary + "\n");
        calStr.write("DESCRIPTION:room: " + discription + "\n");
        calStr.write("LOCATION:" + lat + "," + lng + "\n");
        calStr.write("END:VEVENT\n");
    }

    public String pmConvert(String time){
        System.out.println("correcting time...");
        int result = Integer.parseInt(time);
        if (result < 8) {
            System.out.println("converting to correct PM time");
            result += 12;
        }
        if (result < 10) {
            return "0" + result;
        } else {
            return "" + result;
        }
    }

}
