# Market Data API Usage Guide

## Overview
This service fetches OHLC (Open, High, Low, Close) data from third-party APIs and stores it in the database.

## Setup

### 1. Configure Data Source
First, create a data source in the database:

```sql
INSERT INTO data_sources (name, provider_type, api_endpoint, api_key, is_active, priority, created_at, updated_at)
VALUES (
    'Alpha Vantage',
    'REST_API',
    'https://www.alphavantage.co/query',
    'YOUR_API_KEY_HERE',
    true,
    1,
    NOW(),
    NOW()
);
```

### 2. Create Stock and Exchange
```sql
-- Create exchange
INSERT INTO exchange (name, code, country, currency, open_time, close_time, is_active, created_at, updated_at)
VALUES ('New York Stock Exchange', 'NYSE', 'USA', 'USD', '2024-01-01 09:30:00+00', '2024-01-01 16:00:00+00', true, NOW(), NOW());

-- Create stock
INSERT INTO stocks (symbol, name, exchange_id, isactive, created_at, updated_at)
VALUES ('AAPL', 'Apple Inc.', 1, true, NOW(), NOW());
```

## API Endpoints

### Fetch OHLC Data
```http
POST /api/market-data/fetch?stockId=1&dataSourceId=1&interval=ONE_DAY
```

**Parameters:**
- `stockId` (required): ID of the stock
- `dataSourceId` (required): ID of the data source
- `interval` (optional): Price interval (default: ONE_DAY)
  - Options: ONE_MINUTE, FIVE_MINUTE, FIFTEEN_MINUTE, THIRTY_MINUTE, ONE_HOUR, FOUR_HOUR, ONE_DAY, ONE_WEEK, ONE_MONTH

**Example using curl:**
```bash
curl -X POST "http://localhost:8080/api/market-data/fetch?stockId=1&dataSourceId=1&interval=ONE_DAY"
```

**Example using Java:**
```java
@Autowired
private MarketDataService marketDataService;

public void fetchData() {
    marketDataService.fetchAndSaveOhlcData(1L, 1L, PriceInterval.ONE_DAY)
        .subscribe(
            prices -> System.out.println("Saved " + prices.size() + " price records"),
            error -> System.err.println("Error: " + error.getMessage())
        );
}
```

## Supported Providers

### Alpha Vantage
- Endpoint: `https://www.alphavantage.co/query`
- Requires API key
- Supports: Intraday (1min, 5min, 15min, 30min, 60min) and Daily data

### Yahoo Finance
- Endpoint: `https://query1.finance.yahoo.com/v8/finance/chart`
- No API key required
- Supports: Multiple intervals

## Error Handling

The service handles:
- API rate limits
- Network timeouts
- Invalid responses
- Duplicate data prevention

## Rate Limiting

Configure rate limits in the `data_sources` table:
- `rate_limit_per_minute`: Requests per minute
- `rate_limit_per_day`: Requests per day

The service respects these limits to avoid API throttling.

## WebSocket (real-time price updates)

When OHLC data is fetched and saved (e.g. via `POST /api/market-data/fetch`), new prices are broadcast over WebSocket so clients can receive them in real time.

### Endpoint
- **STOMP over SockJS:** `http://localhost:8080/ws` (use this URL in SockJS client)

### Topics to subscribe to
| Topic | Description |
|-------|-------------|
| `/topic/stock-prices` | All stock price updates (payload: array of `StockPriceMessage`) |
| `/topic/stock-prices/{stockId}` | Updates only for the given stock (e.g. `/topic/stock-prices/1`) |

### Payload shape (`StockPriceMessage`)
- `id`, `stockId`, `symbol`, `stockName`, `timestamp`, `interval`, `open`, `high`, `low`, `close`, `volume`

### Example (JavaScript with SockJS + STOMP)
```javascript
const socket = new SockJS('http://localhost:8080/ws');
const client = Stomp.over(socket);
client.connect({}, () => {
  client.subscribe('/topic/stock-prices', (msg) => {
    const updates = JSON.parse(msg.body);
    console.log('Price updates:', updates);
  });
  client.subscribe('/topic/stock-prices/1', (msg) => {
    console.log('Stock 1 updates:', JSON.parse(msg.body));
  });
});
```

Use the **sockjs-client** and **@stomp/stompjs** (or **stompjs**) npm packages in a frontend to connect.
