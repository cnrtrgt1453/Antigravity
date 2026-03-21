package com.antigravity.api.repository;

import com.antigravity.api.entity.TradeHistory;
import com.antigravity.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeHistoryRepository extends JpaRepository<TradeHistory, Long> {
    List<TradeHistory> findByUserOrderByTimestampDesc(User user);
}
