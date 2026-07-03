package dev.eminberkeay.financepulse.binance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.eminberkeay.financepulse.market.Ticker;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Maps raw Binance combined-stream miniTicker messages to {@link Ticker}.
 */
@Component
public class TickerMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Optional<Ticker> fromCombinedStream(String json) {
        try {
            JsonNode payload = objectMapper.readTree(json).path("data");
            if (!payload.has("s")) {
                return Optional.empty();
            }
            double open = payload.path("o").asDouble();
            double close = payload.path("c").asDouble();
            double changePercent = open == 0 ? 0 : (close - open) / open * 100;

            return Optional.of(new Ticker(
                    payload.path("s").asText(),
                    close,
                    open,
                    payload.path("h").asDouble(),
                    payload.path("l").asDouble(),
                    payload.path("v").asDouble(),
                    changePercent,
                    payload.path("E").asLong()
            ));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
