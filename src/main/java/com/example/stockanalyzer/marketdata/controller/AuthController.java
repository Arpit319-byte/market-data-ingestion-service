package com.example.stockanalyzer.marketdata.controller;


import com.example.stockanalyzer.marketdata.auth.ProviderAuthRegistry;
import com.example.stockanalyzer.marketdata.dto.AuthLoginResponse;
import com.example.stockanalyzer.marketdata.entites.DataSource;
import com.example.stockanalyzer.marketdata.exception.MarketDataException;
import com.example.stockanalyzer.marketdata.repository.DataSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ProviderAuthRegistry providerAuthRegistry;
    private final DataSourceRepository dataSourceRepository;

    /**
     * Returns the Zerodha login URL.
     *
     * Example:
     * GET /api/v1/auth/1/login
     */
    @GetMapping("/{dataSourceId}/login")
    public ResponseEntity<String> login(
            @PathVariable Long dataSourceId) {

        DataSource dataSource = getDataSource(dataSourceId);

        String loginUrl = providerAuthRegistry.resolve(dataSource).getLoginUrl(dataSource);

        return ResponseEntity.ok(loginUrl);
    }

    /**
     * Zerodha redirects the browser to this URL after
     * successful login.
     *
     * Example:
     * http://localhost:8080/api/v1/auth/1/callback?request_token=xxxx
     */
    @GetMapping("/{dataSourceId}/callback")
    public ResponseEntity<AuthLoginResponse> callback(
            @PathVariable Long dataSourceId,
            @RequestParam("request_token") String requestToken) {

        DataSource dataSource = getDataSource(dataSourceId);

        AuthLoginResponse result = providerAuthRegistry.resolve(dataSource).completeLogin(dataSource, requestToken);

        return ResponseEntity.ok(result);
    }

    private DataSource getDataSource(Long dataSourceId) {
        return dataSourceRepository.findById(dataSourceId)
                .orElseThrow(() -> new MarketDataException("Data source not found: " + dataSourceId));
    }

}