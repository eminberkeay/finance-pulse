package dev.eminberkeay.financepulse.binance;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.eminberkeay.financepulse.market.PriceStore;
import dev.eminberkeay.financepulse.ws.PriceBroadcastHandler;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Connects to Binance's public combined stream and republishes mini-ticker
 * updates to the price store and all connected dashboard clients.
 */
@Component
public class BinanceStreamClient implements WebSocket.Listener {

    private static final Logger log = LoggerFactory.getLogger(BinanceStreamClient.class);

    private final PriceStore priceStore;
    private final PriceBroadcastHandler broadcastHandler;
    private final TickerMapper tickerMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
    private final StringBuilder messageBuffer = new StringBuilder();
    private final List<String> symbols;

    private volatile WebSocket webSocket;
    private volatile boolean shuttingDown = false;

    public BinanceStreamClient(PriceStore priceStore,
                               PriceBroadcastHandler broadcastHandler,
                               TickerMapper tickerMapper,
                               @Value("${financepulse.symbols:btcusdt,ethusdt,solusdt,bnbusdt,xrpusdt,dogeusdt}") List<String> symbols) {
        this.priceStore = priceStore;
        this.broadcastHandler = broadcastHandler;
        this.tickerMapper = tickerMapper;
        this.symbols = symbols;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void connect() {
        String streams = symbols.stream()
                .map(s -> s.toLowerCase() + "@miniTicker")
                .collect(Collectors.joining("/"));
        URI uri = URI.create("wss://stream.binance.com:9443/stream?streams=" + streams);
        log.info("Connecting to Binance stream: {}", uri);

        HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(uri, this)
                .whenComplete((ws, err) -> {
                    if (err != null) {
                        log.error("Binance connection failed: {}", err.getMessage());
                        scheduleReconnect();
                    } else {
                        this.webSocket = ws;
                        log.info("Connected to Binance stream");
                    }
                });
    }

    @Override
    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
        messageBuffer.append(data);
        if (last) {
            handleMessage(messageBuffer.toString());
            messageBuffer.setLength(0);
        }
        ws.request(1);
        return null;
    }

    private void handleMessage(String json) {
        tickerMapper.fromCombinedStream(json).ifPresent(ticker -> {
            try {
                priceStore.update(ticker);
                broadcastHandler.broadcast(objectMapper.writeValueAsString(ticker));
            } catch (Exception e) {
                log.warn("Failed to broadcast ticker: {}", e.getMessage());
            }
        });
    }

    @Override
    public void onError(WebSocket ws, Throwable error) {
        log.error("Binance stream error: {}", error.getMessage());
        scheduleReconnect();
    }

    @Override
    public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
        log.warn("Binance stream closed ({}): {}", statusCode, reason);
        scheduleReconnect();
        return null;
    }

    private void scheduleReconnect() {
        if (shuttingDown) {
            return;
        }
        log.info("Reconnecting to Binance in 5s...");
        reconnectExecutor.schedule(this::connect, 5, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        shuttingDown = true;
        reconnectExecutor.shutdownNow();
        WebSocket ws = this.webSocket;
        if (ws != null) {
            ws.sendClose(WebSocket.NORMAL_CLOSURE, "shutdown");
        }
    }
}
