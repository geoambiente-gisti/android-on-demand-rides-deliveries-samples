package com.google.mapsplatform.transportation.sample.consumer;

public class ConvertionUtils {

    public static Double convertMetersToKM(Long meters) {
        return Double.valueOf(meters / 1000);
    }

    public static Long convertSecondsToMinutes(Long seconds) {
        return seconds / 60;
    }

    public static Long convertSecondsToMilliseconds(Long seconds) {
        return seconds * 1000L;
    }

    public static String convertSecondsToMinutesFormatted(Long seconds) {
        Long minutes = convertSecondsToMinutes(seconds);
        return minutes + " minutos";
    }
}
