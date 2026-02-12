 # Grow API Integration Setup

## Overview
The Grow API provider fetches real-time OHLC (Open, High, Low, Close) data from Grow's API.

**Important**: The Grow API OHLC endpoint returns **real-time snapshot data**, not historical interval-based candles. For historical data, you would need to use Grow's Historical Data API.

## API Endpoint
```
GET https://api.groww.in/v1/live-data/ohlc
```

## Request Format
```
GET https://api.groww.in/v1/live-data/ohlc?segment=CASH&exchange_symbols=NSE_RELIANCE,BSE_SENSEX
Headers:
  - Accept: application/json
  - Authorization: Bearer {ACCESS_TOKEN}
  - X-API-VERSION: 1.0
```

## Parameters
- **segment**: `CASH` for stocks, `FNO` for derivatives, `COMMODITY` for commodities
- **exchange_symbols**: Comma-separated list of exchange_symbols (e.g., `NSE_RELIANCE`, `BSE_SENSEX`)
  - Format: `{EXCHANGE_CODE}_{SYMBOL}` (e.g., `NSE_RELIANCE`, `BSE_SENSEX`)

## Response Format
```json
{
  "status": "SUCCESS",
  "payload": {
    "NSE_RELIANCE": "{open: 149.50,high: 150.50,low: 148.50,close: 149.50}",
    "BSE_SENSEX": "{open: 149.50,high: 150.50,low: 148.50,close: 149.50}"
  }
}
```

## Setup Instructions

### Step 1: Create Exchange
```sql
INSERT INTO exchange (name, code, country, currency, open_time, close_time, is_active, created_at, updated_at)
VALUES ('National Stock Exchange', 'NSE', 'India', 'INR', '2024-01-01 09:15:00+00', '2024-01-01 15:30:00+00', true, NOW(), NOW());
```

### Step 2: Create Stock
```sql
INSERT INTO stocks (symbol, name, exchange_id, isactive, created_at, updated_at)
VALUES ('RELIANCE', 'Reliance Industries Ltd', 1, true, NOW(), NOW());
```

### Step 3: Create Data Source
```sql
INSERT INTO data_sources (
    name, 
    provider_type, 
    api_endpoint, 
    api_key, 
    is_active, 
    timeout_seconds,
    created_at, 
    updated_at
)
VALUES (
    'Grow API',
    'REST_API',
    'https://api.groww.in/v1/live-data/ohlc',
    'YOUR_ACCESS_TOKEN_HERE',
    true,
    30,
    NOW(),
    NOW()
);
```

**Important**: 
- Set `api_endpoint` to: `https://api.groww.in/v1/live-data/ohlc`
- Set `api_key` to your Grow API access token
- The provider will automatically detect this as a Grow API data source

### Step 4: Fetch Data
```bash
POST /api/market-data/fetch?stockId=1&dataSourceId=1&interval=ONE_DAY
```

## How It Works

1. **Provider Detection**: The `GrowApiProvider` automatically detects Grow API data sources by checking:
   - API endpoint contains "groww.in" or "grow"
   - Provider type contains "grow"
   - Data source name contains "grow"

2. **Exchange Symbol Building**: 
   - The provider looks up the stock by symbol
   - Builds exchange_symbol format: `{EXCHANGE_CODE}_{SYMBOL}`
   - Example: `NSE_RELIANCE`, `BSE_SENSEX`

3. **Segment Determination**:
   - Defaults to `CASH` for regular stocks
   - Automatically detects `FNO` for derivatives (symbols containing FUT, CE, PE)
   - You can extend `determineSegment()` method for custom logic

4. **Response Parsing**:
   - Parses OHLC string format: `"{open: 149.50,high: 150.50,low: 148.50,close: 149.50}"`
   - Converts to standard `OhlcApiResponse` format
   - Uses current timestamp (since it's real-time data)

## Limitations

1. **Real-time Data Only**: The OHLC endpoint returns current snapshot, not historical candles
2. **No Volume**: The API response doesn't include volume data
3. **Single Symbol**: Currently fetches one symbol at a time (API supports up to 50)

## Future Enhancements

1. **Historical Data API**: Implement support for Grow's Historical Data API for interval-based candles
2. **Batch Requests**: Support fetching multiple symbols in one API call
3. **Volume Data**: Integrate with other endpoints to get volume information
4. **Segment Detection**: Improve segment detection based on stock metadata

## Example Usage

```java
@Autowired
private MarketDataService marketDataService;

public void fetchGrowData() {
    // Fetch real-time OHLC for stockId=1 from Grow API (dataSourceId=1)
    marketDataService.fetchAndSaveOhlcData(1L, 1L, PriceInterval.ONE_DAY)
        .subscribe(
            prices -> System.out.println("Saved " + prices.size() + " price records"),
            error -> System.err.println("Error: " + error.getMessage())
        );
}
```

## Troubleshooting

### Error: "Stock not found with symbol"
- Ensure the stock exists in the `stocks` table
- Check that the symbol matches exactly (case-sensitive)

### Error: "No provider found for data source"
- Verify the API endpoint contains "groww.in" or "grow"
- Check that the data source name or provider_type contains "grow"

### Error: "Failed to parse OHLC string"
- The API response format may have changed
- Check the actual API response format
- Update the `OHLC_PATTERN` regex if needed
