package dev.eminberkeay.financepulse.api;

import dev.eminberkeay.financepulse.binance.BinanceHistoryClient;
import dev.eminberkeay.financepulse.market.PricePoint;
import dev.eminberkeay.financepulse.market.PriceStore;
import dev.eminberkeay.financepulse.market.Ticker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PriceController.class)
class PriceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PriceStore priceStore;

    @MockitoBean
    private BinanceHistoryClient historyClient;

    @Test
    void pricesReturnsStoredTickers() throws Exception {
        when(priceStore.all()).thenReturn(List.of(
                new Ticker("BTCUSDT", 61000, 60000, 62000, 59000, 100, 1.66, 1700000000000L)));

        mockMvc.perform(get("/api/prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("BTCUSDT"))
                .andExpect(jsonPath("$[0].price").value(61000.0));
    }

    @Test
    void historyReturnsPricePoints() throws Exception {
        when(historyClient.recentPrices(eq("BTCUSDT"), anyInt()))
                .thenReturn(List.of(new PricePoint(1700000000000L, 60500.0)));

        mockMvc.perform(get("/api/history/BTCUSDT?limit=30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].time").value(1700000000000L))
                .andExpect(jsonPath("$[0].price").value(60500.0));
    }

    @Test
    void historyClampsLimit() throws Exception {
        when(historyClient.recentPrices("BTCUSDT", 500)).thenReturn(List.of());

        mockMvc.perform(get("/api/history/BTCUSDT?limit=99999"))
                .andExpect(status().isOk());
    }
}
