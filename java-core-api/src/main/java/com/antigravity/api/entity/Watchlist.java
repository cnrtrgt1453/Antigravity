package com.antigravity.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Watchlist entity representing a user's tracked stocks.
 * Implements SRP by separating watchlist management from the User entity.
 */
@Entity
@Table(name = "watchlist", indexes = {
    @Index(name = "idx_watchlist_user_stock", columnList = "user_id, stock_symbol", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Watchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "stock_symbol", nullable = false, length = 20)
    private String stockSymbol;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
