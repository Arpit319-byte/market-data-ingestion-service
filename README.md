The Market Data Ingestion Service is the foundational component of a stock analyzer platform.
It is responsible for fetching, validating, normalizing, storing, and distributing stock market price data.
This service acts as the single source of truth for market prices and publishes price update events for downstream analytics services.

🎯 Responsibilities
This service is designed to do one job extremely well:
->Fetch stock market data from external providers
->Normalize raw data into a consistent OHLCV format
->Validate and clean incoming data
->Store time-series price data reliably
->Publish price update events for downstream services
->Expose read-only APIs for market data access
