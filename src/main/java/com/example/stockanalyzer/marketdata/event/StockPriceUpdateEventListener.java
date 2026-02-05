package com.example.stockanalyzer.marketdata.event;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.example.stockanalyzer.marketdata.dto.StockPriceMessage;
import com.example.stockanalyzer.marketdata.entites.StockPrice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Listener for price update events. Broadcasts new prices over WebSocket so
 * subscribed clients receive real-time updates. Also logs and can publish to
 * a message queue for downstream consumers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockPriceUpdateEventListener {

    private static final String TOPIC_ALL = "/topic/stock-prices";
    private static final String TOPIC_STOCK = "/topic/stock-prices/%d";

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    @Async
    public void onStockPriceUpdate(StockPriceUpdateEvent event) {
        List<StockPrice> saved = event.getSavedPrices();
        if (saved.isEmpty()) {
            return;
        }
        log.debug("Stock price update: {} new record(s) for stock id {}",
                saved.size(), saved.get(0).getStock().getId());

        List<StockPriceMessage> messages = saved.stream()
                .map(StockPriceMessage::from)
                .collect(Collectors.toList());

        messagingTemplate.convertAndSend(TOPIC_ALL, messages);

        saved.stream()
                .collect(Collectors.groupingBy(sp -> sp.getStock().getId()))
                .forEach((stockId, prices) -> {
                    List<StockPriceMessage> forStock = prices.stream()
                            .map(StockPriceMessage::from)
                            .collect(Collectors.toList());
                    messagingTemplate.convertAndSend(TOPIC_STOCK.formatted(stockId), forStock);
                });
    }
}
