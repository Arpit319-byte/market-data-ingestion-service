package com.example.stockanalyzer.marketdata.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Grow API OHLC response
 * Response format:
 * {
 *   "status": "SUCCESS",
 *   "payload": {
 *     "NSE_RELIANCE": "{open: 149.50,high: 150.50,low: 148.50,close: 149.50}",
 *     "BSE_SENSEX": "{open: 149.50,high: 150.50,low: 148.50,close: 149.50}"
 *   }
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GrowApiResponse {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("payload")
    private Map<String, String> payload; // Key: exchange_symbol, Value: OHLC string
    
    @JsonProperty("error")
    private String error;
    
    @JsonProperty("message")
    private String message;
}
