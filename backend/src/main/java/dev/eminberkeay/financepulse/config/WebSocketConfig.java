package dev.eminberkeay.financepulse.config;

import dev.eminberkeay.financepulse.ws.PriceBroadcastHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final PriceBroadcastHandler priceBroadcastHandler;

    public WebSocketConfig(PriceBroadcastHandler priceBroadcastHandler) {
        this.priceBroadcastHandler = priceBroadcastHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(priceBroadcastHandler, "/ws/prices")
                .setAllowedOriginPatterns("*");
    }
}
