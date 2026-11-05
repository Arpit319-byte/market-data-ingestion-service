package com.example.stockanalyzer.marketdata.auth;

import com.example.stockanalyzer.marketdata.dto.AuthLoginResponse;
import com.example.stockanalyzer.marketdata.entities.DataSource;
import com.example.stockanalyzer.marketdata.entities.ProviderType;
import com.example.stockanalyzer.marketdata.exception.AuthenticationException;

public class UnavailableProviderAuthService implements ProviderAuthService {

    private final ProviderType providerType;
    private final String providerName;

    public UnavailableProviderAuthService(ProviderType providerType, String providerName) {
        this.providerType = providerType;
        this.providerName = providerName;
    }

    @Override
    public boolean supports(DataSource dataSource) {
        return dataSource != null && providerType.equals(dataSource.getProviderType());
    }

    @Override
    public boolean hasValidSession(DataSource dataSource) {
        return false;
    }

    @Override
    public String getLoginUrl(DataSource dataSource) {
        throw unavailable();
    }

    @Override
    public AuthLoginResponse completeLogin(DataSource dataSource, String requestToken) {
        throw unavailable();
    }

    @Override
    public void refreshSession(DataSource dataSource) {
        throw unavailable();
    }

    @Override
    public void invalidateSession(DataSource dataSource) {
        // Nothing to expire until this provider stores a session.
    }

    private AuthenticationException unavailable() {
        return new AuthenticationException(providerName + " login is not connected yet");
    }
}
