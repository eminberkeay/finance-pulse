package dev.eminberkeay.financepulse.binance;

import com.fasterxml.jackson.databind.JsonNode;
import dev.eminberkeay.financepulse.market.PricePoint;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Fetches recent kline (candlestick) history from Binance's public REST API.
 */
@Component
public class BinanceHistoryClient {

    private final RestClient restClient;

    public BinanceHistoryClient(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://api.binance.com").build();
    }

    /**
     * Returns the closing price of the last {@code limit} one-minute candles.
     * Kline format: [openTime, open, high, low, close, volume, closeTime, ...]
     */
    public List<PricePoint> recentPrices(String symbol, int limit) {
        JsonNode klines = restClient.get()
                .uri("/api/v3/klines?symbol={symbol}&interval=1m&limit={limit}",
                        symbol.toUpperCase(), limit)
                .retrieve()
                .body(JsonNode.class);

        List<PricePoint> points = new ArrayList<>();
        if (klines != null && klines.isArray()) {
            for (JsonNode kline : klines) {
                points.add(new PricePoint(kline.get(6).asLong(), kline.get(4).asDouble()));
            }
        }
        return points;
    }
}
