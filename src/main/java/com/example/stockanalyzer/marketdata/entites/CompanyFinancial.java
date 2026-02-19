package com.example.stockanalyzer.marketdata.entites;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "company_financial")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyFinancial extends BaseModel {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    // Financial period info
    @Enumerated(EnumType.STRING)
    private FinancialPeriodType periodType; // QUARTERLY or YEARLY

    private Integer year;

    private Integer quarter; // 1,2,3,4 (null for yearly)

    private LocalDate periodStartDate;

    private LocalDate periodEndDate;

    // Income statement
    private BigDecimal revenue;

    private BigDecimal netProfit;

    private BigDecimal operatingProfit;

    private BigDecimal eps;

    // Balance sheet
    private BigDecimal totalAssets;

    private BigDecimal totalLiabilities;

    private BigDecimal totalEquity;

    private BigDecimal debt;

    // Cash flow
    private BigDecimal operatingCashFlow;

    private BigDecimal freeCashFlow;

    // Ratios
    private BigDecimal roe;

    private BigDecimal debtToEquity;

    private BigDecimal peRatio;

    private BigDecimal pbRatio;
    
}
