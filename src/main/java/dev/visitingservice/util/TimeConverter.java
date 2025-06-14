package dev.visitingservice.util;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeConverter {
    private static final ZoneId NIGERIA_ZONE = ZoneId.of("Africa/Lagos");

    public static ZonedDateTime convertUtcToNigeria(OffsetDateTime utcTime) {
        return utcTime.atZoneSameInstant(NIGERIA_ZONE);
    }

    
    public static String formatToReadableLagosTime(OffsetDateTime utcTime) {
        ZonedDateTime nigeriaTime = convertUtcToNigeria(utcTime);
        return nigeriaTime.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a"));
    }
}
