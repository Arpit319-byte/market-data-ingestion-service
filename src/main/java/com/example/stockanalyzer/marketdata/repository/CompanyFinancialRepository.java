package com.example.stockanalyzer.marketdata.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.stockanalyzer.marketdata.entites.CompanyFinancial;
import com.example.stockanalyzer.marketdata.entites.FinancialPeriodType;

interface CompanyFinancialRepository  extends JpaRepository<CompanyFinancial,Long>{

    List<CompanyFinancial> findByStockIdOrderByYearDescQuaterDesc(Long id);

    List<CompanyFinancial> findByStockIdAndPeriodType(Long id,FinancialPeriodType period);

    Optional<CompanyFinancial>  findByStockIdAndYearAndQuater(Long id ,Integer year,Integer quater);

    
    
}
