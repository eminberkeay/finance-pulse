package dev.eminberkeay.financepulse.market;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PriceStore {

    private final Map<String, Ticker> latest = new ConcurrentHashMap<>();

    public void update(Ticker ticker) {
        latest.put(ticker.symbol(), ticker);
    }

    public Collection<Ticker> all() {
        return latest.values();
    }
}
