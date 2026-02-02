package com.example.stockanalyzer.marketdata.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic DTO for OHLC API responses
 * Adapts to different provider formats (Alpha Vantage, Yahoo Finance, etc.)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OhlcApiResponse {
    
    // For Alpha Vantage format
    @JsonProperty("Time Series (Daily)")
    private Map<String, TimeSeriesData> timeSeriesDaily;
    
    @JsonProperty("Time Series (1min)")
    private Map<String, TimeSeriesData> timeSeries1min;
    
    @JsonProperty("Time Series (5min)")
    private Map<String, TimeSeriesData> timeSeries5min;
    
    @JsonProperty("Time Series (15min)")
    private Map<String, TimeSeriesData> timeSeries15min;
    
    @JsonProperty("Time Series (60min)")
    private Map<String, TimeSeriesData> timeSeries60min;
    
    // For Yahoo Finance format
    @JsonProperty("chart")
    private ChartData chart;
    
    // Error handling
    @JsonProperty("Error Message")
    private String errorMessage;
    
    @JsonProperty("Note")
    private String note;
    
    @JsonProperty("Information")
    private String information;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TimeSeriesData {
        @JsonProperty("1. open")
        private String open;
        
        @JsonProperty("2. high")
        private String high;
        
        @JsonProperty("3. low")
        private String low;
        
        @JsonProperty("4. close")
        private String close;
        
        @JsonProperty("5. volume")
        private String volume;
        
        // Alternative field names (Yahoo Finance)
        @JsonProperty("open")
        private BigDecimal openPrice;
        
        @JsonProperty("high")
        private BigDecimal highPrice;
        
        @JsonProperty("low")
        private BigDecimal lowPrice;
        
        @JsonProperty("close")
        private BigDecimal closePrice;
        
        @JsonProperty("volume")
        private Long volumeValue;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChartData {
        @JsonProperty("result")
        private List<Result> result;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        @JsonProperty("timestamp")
        private List<Long> timestamp;
        
        @JsonProperty("indicators")
        private Indicators indicators;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Indicators {
        @JsonProperty("quote")
        private List<Quote> quote;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Quote {
        @JsonProperty("open")
        private List<BigDecimal> open;
        
        @JsonProperty("high")
        private List<BigDecimal> high;
        
        @JsonProperty("low")
        private List<BigDecimal> low;
        
        @JsonProperty("close")
        private List<BigDecimal> close;
        
        @JsonProperty("volume")
        private List<Long> volume;
    }
}
