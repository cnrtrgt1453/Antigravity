package com.antigravity.api.service.trading;

import java.math.BigDecimal;

public interface CommissionCalculator {
    /**
     * Calculates trading commission based on quantity and price per share.
     */
    BigDecimal calculateCommission(Long quantity, BigDecimal price);
}
