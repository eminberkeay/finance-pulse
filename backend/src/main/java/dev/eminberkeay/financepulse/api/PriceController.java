package dev.eminberkeay.financepulse.api;

import dev.eminberkeay.financepulse.market.PriceStore;
import dev.eminberkeay.financepulse.market.Ticker;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api")
public class PriceController {

    private final PriceStore priceStore;

    public PriceController(PriceStore priceStore) {
        this.priceStore = priceStore;
    }

    @GetMapping("/prices")
    public Collection<Ticker> prices() {
        return priceStore.all();
    }
}
