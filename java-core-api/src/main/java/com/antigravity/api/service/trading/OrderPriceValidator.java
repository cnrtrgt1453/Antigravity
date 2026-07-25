package com.antigravity.api.service.trading;

import java.math.BigDecimal;

public interface OrderPriceValidator {
    /**
     * Validates that the requested order execution price is valid and not spoofed.
     */
    void validatePrice(String symbol, BigDecimal requestedPrice);
}
