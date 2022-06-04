package com.refinedmods.refinedstorage2.api.core.util;

public record LastModified(Type type, long amount) {
    private static final long SECOND = 1000;
    private static final long MINUTE = SECOND * 60;
    private static final long HOUR = MINUTE * 60;
    private static final long DAY = HOUR * 24;
    private static final long WEEK = DAY * 7;
    private static final long YEAR = DAY * 365;

    public enum Type {
        SECOND,
        MINUTE,
        HOUR,
        DAY,
        WEEK,
        YEAR
    }

    public static LastModified calculate(long time, long now) {
        long diff = now - time;

        if (diff < MINUTE) {
            return new LastModified(Type.SECOND, diff / SECOND);
        } else if (diff < HOUR) {
            return new LastModified(Type.MINUTE, diff / MINUTE);
        } else if (diff < DAY) {
            return new LastModified(Type.HOUR, diff / HOUR);
        } else if (diff < WEEK) {
            return new LastModified(Type.DAY, diff / DAY);
        } else if (diff < YEAR) {
            return new LastModified(Type.WEEK, diff / WEEK);
        }

        return new LastModified(Type.YEAR, diff / YEAR);
    }
}