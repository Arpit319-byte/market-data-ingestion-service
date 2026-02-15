package com.example.stockanalyzer.marketdata.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.stockanalyzer.marketdata.entites.DataSource;
import com.example.stockanalyzer.marketdata.entites.Stock;
import com.example.stockanalyzer.marketdata.repository.DataSourceRepository;
import com.example.stockanalyzer.marketdata.repository.StockRepository;
import com.example.stockanalyzer.marketdata.service.MarketDataService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketDataScheduler {

    private final DataSourceRepository dataSourceRepository;
    private final StockRepository stockRepository;
    private final MarketDataService marketDataService;


    @Scheduled()
    public void fetchMarketData(){
          
          log.info("Scheduller is being called to fetch the data from the Groww");

          DataSource dataSource=dataSourceRepository.findAll().stream()
                                .filter(ds-> ds.getIsActive().equals(Boolean.TRUE))
                                .findFirst()
                                .orElse(null);

          if(dataSource == null){
            log.info("No active dataSource is present in the DB , Scheduler is not being called");
             return;
          }

          List<Stock> stockList = stockRepository.findByIsActiveTrue();

          if(stockList.isEmpty()){
            log.info("No active stock to fetch. Scheduler is not being called");
            return ;
          }

          for(Stock stock: stockList){
            log.info("Calling the fetch OHLC method for the stock->"+stock.toString());
            try{

            }catch(Exception ex){
              log.error("Fetch failed for the stock->"+stock.getSymbol()+" from->"+dataSource.getName());
              ex.printStackTrace();
            }

            try {
              Thread.sleep(throttleMs);
           } catch (InterruptedException ie) {
              Thread.currentThread().interrupt();
              log.warn("Scheduler interrupted");
              break;
            }

          }
    }

    
}
