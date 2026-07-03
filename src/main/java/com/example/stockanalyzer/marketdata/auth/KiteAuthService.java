package com.example.stockanalyzer.marketdata.auth;

import java.time.Instant;
import org.json.JSONException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.stockanalyzer.marketdata.entites.BrokerSession;
import com.example.stockanalyzer.marketdata.entites.BrokerSessionStatus;
import com.example.stockanalyzer.marketdata.entites.DataSource;
import com.example.stockanalyzer.marketdata.entites.ProviderType;
import com.example.stockanalyzer.marketdata.dto.AuthLoginResponse;
import com.example.stockanalyzer.marketdata.exception.AuthenticationException;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.TokenSet;
import com.zerodhatech.models.User;
import com.example.stockanalyzer.marketdata.service.BrokerSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KiteAuthService implements ProviderAuthService {

    private static final String KITE_DATA_SOURCE_NAME = "Kite API";

    private final KiteClientFactory kiteClientFactory;
    private final BrokerSessionService brokerSessionService;

    @Override
    public boolean supports(DataSource dataSource) {
        return dataSource != null
                && (ProviderType.KITE.equals(dataSource.getProviderType())
                || KITE_DATA_SOURCE_NAME.equalsIgnoreCase(dataSource.getName()));
    }

    @Override
    public boolean hasValidSession(DataSource dataSource) {
        if (!supports(dataSource)) {
            return false;
        }
        if (brokerSessionService.findActiveSession(dataSource).isEmpty()) {
            return false;
        }
        try {
            kiteClientFactory.createAuthenticated(dataSource).getProfile();
            return true;
        } catch (Exception e) {
            brokerSessionService.expireSession(dataSource);
            return false;
        } catch (KiteException e) {
            brokerSessionService.expireSession(dataSource);
            return false;
        }
    }

    @Override
    public String getLoginUrl(DataSource dataSource) {
        if (!supports(dataSource)) {
            throw new AuthenticationException("Unsupported data source for Kite auth: " + dataSource.getName());
        }
        return kiteClientFactory.create(dataSource).getLoginURL();
    }

    @Override
    public AuthLoginResponse completeLogin(DataSource dataSource, String requestToken) {
        if (!supports(dataSource)) {
            throw new AuthenticationException("Unsupported data source for Kite auth: " + dataSource.getName());
        }
        User user = completeKiteLogin(dataSource, requestToken, dataSource.getApiSecret());
        return new AuthLoginResponse(
                dataSource.getId(),
                dataSource.getProviderType(),
                user.userId,
                user.userName);
    }

    @Override
    public void invalidateSession(DataSource dataSource) {
        if (supports(dataSource)) {
            brokerSessionService.expireSession(dataSource);
        }
    }

    @Override
    @Transactional
    public void refreshSession(DataSource dataSource) {
        if (!supports(dataSource)) {
            throw new AuthenticationException("Unsupported data source for Kite auth: " + dataSource.getName());
        }
        BrokerSession session = brokerSessionService.findActiveSession(dataSource)
                .orElseThrow(() -> new AuthenticationException(
                        "No active Kite session found for data source: " + dataSource.getName()));
        String refreshToken = session.getRefreshToken();

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthenticationException("Kite refresh token missing; manual login required");
        }

        try {
            KiteConnect kiteConnect = kiteClientFactory.createAuthenticated(dataSource);
            TokenSet tokenSet = kiteConnect.renewAccessToken(refreshToken, dataSource.getApiSecret());
            saveSession(dataSource, tokenSet.userId, tokenSet.accessToken, tokenSet.refreshToken);
            log.info("Kite access token renewed for user {}", tokenSet.userId);
        } catch (KiteException | JSONException | java.io.IOException e) {
            throw new AuthenticationException("Kite token refresh failed", e);
        }
    }

    private User completeKiteLogin(DataSource dataSource, String requestToken, String apiSecret) {
        if (apiSecret == null || apiSecret.isBlank()) {
            throw new AuthenticationException("Kite API secret missing for data source: " + dataSource.getName());
        }
        try {
            KiteConnect kiteConnect = kiteClientFactory.create(dataSource);
            User user = kiteConnect.generateSession(requestToken, apiSecret);
            saveSession(dataSource, user.userId, user.accessToken, user.refreshToken);
            log.info("Kite login successful for user {}", user.userId);
            return user;
        } catch (KiteException | JSONException | java.io.IOException e) {
            throw new AuthenticationException("Kite login failed", e);
        }
    }

    private void saveSession(
            DataSource dataSource,
            String brokerUserId,
            String accessToken,
            String refreshToken) {

        brokerSessionService.expireSession(dataSource);

        BrokerSession session = new BrokerSession();
        session.setDataSource(dataSource);
        session.setBrokerUserId(brokerUserId);
        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);
        session.setLoginTime(Instant.now());
        session.setStatus(BrokerSessionStatus.ACTIVE);
        brokerSessionService.save(session);
    }
}
