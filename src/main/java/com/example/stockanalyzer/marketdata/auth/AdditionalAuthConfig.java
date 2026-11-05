package com.example.stockanalyzer.marketdata.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.stockanalyzer.marketdata.entities.ProviderType;

@Configuration
public class AdditionalAuthConfig {

    @Bean
    ProviderAuthService upstoxAuthService() {
        return new UnavailableProviderAuthService(ProviderType.UPSTOX, "Upstox");
    }

    @Bean
    ProviderAuthService fyersAuthService() {
        return new UnavailableProviderAuthService(ProviderType.FYERS, "Fyers");
    }

    @Bean
    ProviderAuthService growwAuthService() {
        return new UnavailableProviderAuthService(ProviderType.GROWW, "Groww");
    }
}
