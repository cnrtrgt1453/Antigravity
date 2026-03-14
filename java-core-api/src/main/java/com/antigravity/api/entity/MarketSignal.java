package com.antigravity.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_signals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ticker;

    @Column(nullable = false)
    private String signalType; // GOLDEN_CROSS, DEAD_CROSS

    private Double price;

    private String crossDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
