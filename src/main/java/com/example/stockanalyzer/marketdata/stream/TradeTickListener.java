package com.example.stockanalyzer.marketdata.stream;

import java.util.List;

public interface TradeTickListener {

    void onTicks(List<TradeTick> ticks);
}
