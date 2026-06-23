package com.example.stockanalyzer.marketdata.entites;

public enum IntervalType {

    ONE_MINUTE(1),
    FIVE_MINUTE(5),
    FIFTEEN_MINUTE(15),
    THIRTY_MINUTE(30),
    ONE_HOUR(60),
    ONE_DAY(1440);

    private final int minutes;
    IntervalType(int minutes) {
        this.minutes = minutes;
    }
    public int getMinutes() {
        return minutes;
    }

}
