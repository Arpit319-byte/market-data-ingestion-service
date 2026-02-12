# Market Data Provider

## Overview
This service fetches OHLC (Open, High, Low, Close) data from the Groww API.

## Grow API Provider (Groww)
- **Class**: `GrowApiProvider`
- **Supports**: Data sources with "grow" in name, endpoint, or provider_type
- **Live Format**: `https://api.groww.in/v1/live-data/ohlc?segment=CASH&exchange_symbols=NSE_RELIANCE`
- **Authentication**: Bearer token in Authorization header (or groww.api.key + groww.api.secret in config)

See [GROW_API_SETUP.md](impl/GROW_API_SETUP.md) for detailed setup instructions.
