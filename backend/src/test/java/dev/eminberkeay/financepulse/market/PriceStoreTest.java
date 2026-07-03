package dev.eminberkeay.financepulse.market;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PriceStoreTest {

    private final PriceStore store = new PriceStore();

    private Ticker ticker(String symbol, double price) {
        return new Ticker(symbol, price, price, price, price, 0, 0, System.currentTimeMillis());
    }

    @Test
    void startsEmpty() {
        assertThat(store.all()).isEmpty();
    }

    @Test
    void storesLatestTickerPerSymbol() {
        store.update(ticker("BTCUSDT", 60000));
        store.update(ticker("ETHUSDT", 1700));
        store.update(ticker("BTCUSDT", 61000));

        assertThat(store.all()).hasSize(2);
        assertThat(store.all())
                .filteredOn(t -> t.symbol().equals("BTCUSDT"))
                .singleElement()
                .extracting(Ticker::price)
                .isEqualTo(61000.0);
    }
}
