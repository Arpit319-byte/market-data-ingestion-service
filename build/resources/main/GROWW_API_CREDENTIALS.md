# How to Use Groww API Key and Secret

## Two ways to authenticate

### Option 1: Key + Secret (recommended for production)

You provide **API Key** and **API Secret**. The app exchanges them for an **access token** and uses that for API calls. Token is cached and refreshed as needed.

**Where to set:**

1. **Environment variables (recommended – never commit secret):**
   ```bash
   export GROWW_API_KEY=your_api_key_here
   export GROWW_API_SECRET=your_api_secret_here
   ```
   Then start the app. No need to put the secret in any file.

2. **Or in `application.properties` (only for local dev, do not commit secret):**
   ```properties
   groww.api.key=your_api_key_here
   groww.api.secret=your_api_secret_here
   ```
   For production, use env vars or a secret manager, not properties in git.

**Data source in DB:**

- Create a Groww data source with **api_key empty or null** when using key+secret from config.
- The app will use `groww.api.key` and `groww.api.secret` to get a token and call Groww.

**Example data source (when using key+secret from config):**
```sql
INSERT INTO data_sources (name, provider_type, api_endpoint, api_key, is_active, created_at, updated_at)
VALUES (
    'Grow API',
    'REST_API',
    'https://api.groww.in/v1/live-data/ohlc',
    NULL,  -- leave empty; token comes from key+secret
    true,
    NOW(),
    NOW()
);
```

---

### Option 2: Access token only

You generate an **access token** yourself (e.g. from Groww dashboard: Profile → Settings → Trading APIs → Generate API keys → Access Token) and store it in the DB.

**Where to set:**

- In the **data_sources** table, in the **api_key** column for your Groww data source.

**Example:**
```sql
INSERT INTO data_sources (name, provider_type, api_endpoint, api_key, is_active, created_at, updated_at)
VALUES (
    'Grow API',
    'REST_API',
    'https://api.groww.in/v1/live-data/ohlc',
    'your_access_token_here',  -- paste token from Groww
    true,
    NOW(),
    NOW()
);
```

**Note:** Groww access tokens expire daily at 6:00 AM IST. With this option you must refresh the token (e.g. from dashboard) and update `api_key` in the DB when it expires. With Option 1, the app can refresh the token automatically using key+secret.

---

## Resolution order

1. If **groww.api.key** and **groww.api.secret** are set (in env or properties), the app uses them to get an access token and ignores **data_sources.api_key** for that call.
2. Otherwise, the app uses **data_sources.api_key** as the Bearer token. You must set it to a valid access token.

---

## Getting Key and Secret from Groww

1. Go to [Groww Cloud API Keys](https://groww.in/trade-api/api-keys).
2. Log in → **Generate API key** → enter a name → Continue.
3. Copy the **API Key** and **API Secret** (secret is shown only once).
4. Use them as in Option 1 above (env vars or local-only properties).

---

## Security

- **Do not** commit API secret (or access token) to git.
- Prefer **environment variables** (`GROWW_API_KEY`, `GROWW_API_SECRET`) in production.
- Optionally use a secret manager (e.g. AWS Secrets Manager) and set env vars from there at startup.
