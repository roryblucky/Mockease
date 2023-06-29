package com.rory.apimock.utils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    public static String getDateTimeString() {
        return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
    }

    public static OffsetDateTime getOffsetDateTime() {
        return ZonedDateTime.now(ZoneOffset.UTC).toOffsetDateTime();
    }
}
