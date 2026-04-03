package com.antigravity.api.repository;

import com.antigravity.api.entity.Portfolio;
import com.antigravity.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    Optional<Portfolio> findByUser(User user);
    
    void deleteByUser(User user);
}
