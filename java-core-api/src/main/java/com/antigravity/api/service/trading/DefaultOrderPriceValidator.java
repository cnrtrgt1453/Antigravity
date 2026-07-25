package com.antigravity.api.service.trading;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class DefaultOrderPriceValidator implements OrderPriceValidator {

    @Override
    public void validatePrice(String symbol, BigDecimal requestedPrice) {
        if (requestedPrice == null || requestedPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Geçersiz işlem fiyatı: Fiyat sıfırdan büyük olmalıdır.");
        }
        // Additional real-time price boundary checks can be performed here against market feed
    }
}
