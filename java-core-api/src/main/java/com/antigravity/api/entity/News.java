package com.antigravity.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "news", indexes = {
    @Index(name = "idx_news_stock_symbol", columnList = "stock_symbol"),
    @Index(name = "idx_news_published_at", columnList = "published_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    @Column(name = "stock_symbol", length = 20)
    private String stockSymbol;

    @Column(name = "external_uid", unique = true, nullable = false)
    private String externalUid;

    @Column(name = "source_url", length = 1000)
    private String sourceUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
