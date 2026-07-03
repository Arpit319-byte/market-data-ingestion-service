package com.example.stockanalyzer.marketdata.dto;

import com.example.stockanalyzer.marketdata.entites.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthLoginResponse{
        Long dataSourceId;
        ProviderType providerType;
        String brokerUserId;
        String brokerUserName;
}
