package com.antigravity.api.service.trading;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class StandardCommissionCalculator implements CommissionCalculator {

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.01"); // %1 rate

    @Override
    public BigDecimal calculateCommission(Long quantity, BigDecimal price) {
        BigDecimal totalVolume = price.multiply(BigDecimal.valueOf(quantity));
        return totalVolume.multiply(COMMISSION_RATE).setScale(4, RoundingMode.HALF_UP);
    }
}
