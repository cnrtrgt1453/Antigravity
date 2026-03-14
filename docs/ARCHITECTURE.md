# Antigravity - Project Architecture Overview

This document outlines the hybrid (Polyglot Microservices) architecture for the Antigravity market analysis and signals application.

## Technology Stack

### 1. Analysis Engine (Python)
- **Role**: Data fetching, mathematical calculations, and signal generation.
- **Tech**: Python 3.9+, FastAPI, Pandas, TA-Lib, `yfinance`.
- **Logic**: Performs technical analysis (SMA, Golden/Dead Cross) and exposes results via a REST API.

### 2. Core API & Orchestration (Java Spring Boot)
- **Role**: Backend orchestration, security, persistence, and external service coordination.
- **Tech**: Java 17+, Spring Boot 3, Spring Security, Spring Data JPA.
- **Communication**: Interacts with the Python engine for analysis results and serves the mobile frontend.

### 3. Mobile Frontend (React Native)
- **Role**: User interface for viewing market signals and news.
- **Tech**: React Native (Expo), TypeScript.

### 4. Database Layer (PostgreSQL)
- **Role**: Persistent storage for user data, market signals, and news.
- **Indices**: Optimized with composite indices for frequent queries (e.g., Watchlist).

## Core Workflows

### Daily Analysis Cycle
1. **Trigger**: Scheduled task in Python or manual trigger from mobile.
2. **Analysis**: Python engine fetches OHLC data and calculates crossovers.
3. **Persistence**: Results are stored in PostgreSQL for historical tracking.
4. **Notification**: (Future) Push notifications to users.

### News Synchronization
1. **Trigger**: Weekday 18:15 cron job in Java.
2. **Fetch**: Integrates with external financial news providers.
3. **Smart Updates**: Deduplication based on external UIDs/Links.
