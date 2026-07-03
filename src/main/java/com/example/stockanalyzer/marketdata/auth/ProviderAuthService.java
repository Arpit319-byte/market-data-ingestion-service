package com.example.stockanalyzer.marketdata.auth;

import com.example.stockanalyzer.marketdata.entites.DataSource;
import com.example.stockanalyzer.marketdata.dto.AuthLoginResponse;

public interface ProviderAuthService {

    boolean supports(DataSource dataSource);

    boolean hasValidSession(DataSource dataSource);

    String getLoginUrl(DataSource dataSource);

    AuthLoginResponse completeLogin(DataSource dataSource, String requestToken);

    void refreshSession(DataSource dataSource);

    void invalidateSession(DataSource dataSource);
}
