package dev.eminberkeay.financepulse.api;

import dev.eminberkeay.financepulse.binance.BinanceHistoryClient;
import dev.eminberkeay.financepulse.market.PricePoint;
import dev.eminberkeay.financepulse.market.PriceStore;
import dev.eminberkeay.financepulse.market.Ticker;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PriceController {

    private final PriceStore priceStore;
    private final BinanceHistoryClient historyClient;

    public PriceController(PriceStore priceStore, BinanceHistoryClient historyClient) {
        this.priceStore = priceStore;
        this.historyClient = historyClient;
    }

    @GetMapping("/prices")
    public Collection<Ticker> prices() {
        return priceStore.all();
    }

    @GetMapping("/history/{symbol}")
    public List<PricePoint> history(@PathVariable String symbol,
                                    @RequestParam(defaultValue = "60") int limit) {
        return historyClient.recentPrices(symbol, Math.clamp(limit, 1, 500));
    }
}
