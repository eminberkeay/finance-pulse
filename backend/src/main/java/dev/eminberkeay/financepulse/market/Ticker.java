package dev.eminberkeay.financepulse.market;

public record Ticker(
        String symbol,
        double price,
        double openPrice,
        double highPrice,
        double lowPrice,
        double volume,
        double changePercent,
        long updatedAt
) {
}
