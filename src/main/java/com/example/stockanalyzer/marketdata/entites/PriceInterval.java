package com.example.stockanalyzer.marketdata.entites;

public enum PriceInterval {
    ONE_MINUTE("1min"),
    FIVE_MINUTE("5min"),
    FIFTEEN_MINUTE("15min"),
    THIRTY_MINUTE("30min"),
    ONE_HOUR("1hour"),
    FOUR_HOUR("4hour"),
    ONE_DAY("1day"),
    ONE_WEEK("1week"),
    ONE_MONTH("1month");

    private final String value;

    PriceInterval(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
