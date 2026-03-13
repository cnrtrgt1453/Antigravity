package com.antigravity.api.repository;

import com.antigravity.api.entity.MarketSignal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketSignalRepository extends JpaRepository<MarketSignal, Long> {
    List<MarketSignal> findByOrderByCreatedAtDesc();
}
