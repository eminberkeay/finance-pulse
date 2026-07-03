package dev.eminberkeay.financepulse.binance;

import dev.eminberkeay.financepulse.market.Ticker;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class TickerMapperTest {

    private final TickerMapper mapper = new TickerMapper();

    @Test
    void mapsMiniTickerMessage() {
        String json = """
                {"stream":"btcusdt@miniTicker","data":{
                  "e":"24hrMiniTicker","E":1700000000000,"s":"BTCUSDT",
                  "c":"62000.50","o":"60000.00","h":"62500.00","l":"59800.00",
                  "v":"12345.678","q":"765432100.00"}}
                """;

        Optional<Ticker> result = mapper.fromCombinedStream(json);

        assertThat(result).isPresent();
        Ticker ticker = result.get();
        assertThat(ticker.symbol()).isEqualTo("BTCUSDT");
        assertThat(ticker.price()).isEqualTo(62000.50);
        assertThat(ticker.openPrice()).isEqualTo(60000.00);
        assertThat(ticker.highPrice()).isEqualTo(62500.00);
        assertThat(ticker.lowPrice()).isEqualTo(59800.00);
        assertThat(ticker.volume()).isEqualTo(12345.678);
        assertThat(ticker.updatedAt()).isEqualTo(1700000000000L);
        assertThat(ticker.changePercent()).isCloseTo(3.334, within(0.001));
    }

    @Test
    void computesNegativeChangePercent() {
        String json = """
                {"data":{"s":"ETHUSDT","c":"1900.00","o":"2000.00","h":"2010.00","l":"1890.00","v":"1","E":1}}
                """;

        assertThat(mapper.fromCombinedStream(json))
                .hasValueSatisfying(t -> assertThat(t.changePercent()).isCloseTo(-5.0, within(0.001)));
    }

    @Test
    void zeroOpenPriceDoesNotDivideByZero() {
        String json = """
                {"data":{"s":"NEWUSDT","c":"1.00","o":"0","h":"1","l":"1","v":"1","E":1}}
                """;

        assertThat(mapper.fromCombinedStream(json))
                .hasValueSatisfying(t -> assertThat(t.changePercent()).isZero());
    }

    @Test
    void ignoresMessagesWithoutSymbol() {
        assertThat(mapper.fromCombinedStream("{\"result\":null,\"id\":1}")).isEmpty();
    }

    @Test
    void ignoresMalformedJson() {
        assertThat(mapper.fromCombinedStream("not json at all")).isEmpty();
    }
}
