-- ==============================================================================
-- FinanceUP - Supabase PostgreSQL Schema & Security Policies (RLS) & RPC Functions
-- ==============================================================================

-- 1. EXTENSIONS
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. PUBLIC PROFILES TABLOSU (auth.users ile senkronize)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT UNIQUE NOT NULL,
    full_name TEXT,
    profile_picture_url TEXT,
    push_token TEXT,
    created_at TIMESTAMPTZ DEFAULT now() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT now() NOT NULL
);

-- 3. BIST HISSELERI TABLOSU
CREATE TABLE IF NOT EXISTS public.stocks (
    symbol VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) DEFAULT 'BIST' NOT NULL,
    is_active BOOLEAN DEFAULT true NOT NULL,
    last_price NUMERIC(19, 4),
    updated_at TIMESTAMPTZ DEFAULT now() NOT NULL
);

-- 4. KULLANICI TAKIP LISTESI (WATCHLIST)
CREATE TABLE IF NOT EXISTS public.watchlist (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    stock_symbol VARCHAR(20) NOT NULL REFERENCES public.stocks(symbol) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT now() NOT NULL,
    CONSTRAINT uq_watchlist_user_stock UNIQUE(user_id, stock_symbol)
);

CREATE INDEX IF NOT EXISTS idx_watchlist_user ON public.watchlist(user_id);

-- 5. PORTFOY TABLOSU
CREATE TABLE IF NOT EXISTS public.portfolios (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE UNIQUE,
    balance NUMERIC(19, 4) DEFAULT 750000.0000 NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT now() NOT NULL
);

-- 6. PORTFOY HISSE DETAYLARI
CREATE TABLE IF NOT EXISTS public.portfolio_items (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL REFERENCES public.portfolios(id) ON DELETE CASCADE,
    stock_symbol VARCHAR(20) NOT NULL REFERENCES public.stocks(symbol) ON DELETE CASCADE,
    quantity BIGINT NOT NULL CHECK (quantity >= 0),
    average_cost NUMERIC(19, 4) NOT NULL,
    CONSTRAINT uq_portfolio_stock UNIQUE(portfolio_id, stock_symbol)
);

CREATE INDEX IF NOT EXISTS idx_portfolio_items_portfolio ON public.portfolio_items(portfolio_id);

-- 7. ISLEM GECMISI (TRADE HISTORY)
CREATE TABLE IF NOT EXISTS public.trade_history (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    stock_symbol VARCHAR(20) NOT NULL REFERENCES public.stocks(symbol) ON DELETE CASCADE,
    type VARCHAR(10) NOT NULL CHECK (type IN ('BUY', 'SELL')),
    quantity BIGINT NOT NULL CHECK (quantity > 0),
    price NUMERIC(19, 4) NOT NULL,
    commission NUMERIC(19, 4) NOT NULL,
    total_amount NUMERIC(19, 4) NOT NULL,
    timestamp TIMESTAMPTZ DEFAULT now() NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_trade_history_user ON public.trade_history(user_id, timestamp DESC);

-- 8. PIYASA SINYALLERI (Python Taraması ile Doldurulur)
CREATE TABLE IF NOT EXISTS public.market_signals (
    id BIGSERIAL PRIMARY KEY,
    ticker VARCHAR(20) NOT NULL,
    signal_type VARCHAR(50) NOT NULL, -- GOLDEN_CROSS, DEAD_CROSS
    price DOUBLE PRECISION,
    cross_date VARCHAR(50),
    created_at TIMESTAMPTZ DEFAULT now() NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_signals_ticker ON public.market_signals(ticker);
CREATE INDEX IF NOT EXISTS idx_signals_created ON public.market_signals(created_at DESC);

-- 9. HABERLER (KAP & Piyasa Haberleri)
CREATE TABLE IF NOT EXISTS public.news (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    content TEXT,
    published_at TIMESTAMPTZ NOT NULL,
    stock_symbol VARCHAR(20),
    external_uid VARCHAR(255) UNIQUE NOT NULL,
    source_url VARCHAR(1000),
    created_at TIMESTAMPTZ DEFAULT now() NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_news_stock ON public.news(stock_symbol);
CREATE INDEX IF NOT EXISTS idx_news_published ON public.news(published_at DESC);

-- ==============================================================================
-- ROW LEVEL SECURITY (RLS) POLİTİKALARI
-- ==============================================================================

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.stocks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.watchlist ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.portfolios ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.portfolio_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.trade_history ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.market_signals ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.news ENABLE ROW LEVEL SECURITY;

-- Herkese Açık Okuma Politikaları (Public Read)
CREATE POLICY "Stocks are viewable by everyone" ON public.stocks FOR SELECT USING (true);
CREATE POLICY "Signals are viewable by everyone" ON public.market_signals FOR SELECT USING (true);
CREATE POLICY "News are viewable by everyone" ON public.news FOR SELECT USING (true);

-- Kullanıcı Özel Politikaları (User Isolation)
CREATE POLICY "Users can view their own profile" ON public.profiles FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Users can update their own profile" ON public.profiles FOR UPDATE USING (auth.uid() = id);

CREATE POLICY "Users can manage their own watchlist" ON public.watchlist FOR ALL USING (auth.uid() = user_id);

CREATE POLICY "Users can view their own portfolio" ON public.portfolios FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can view their portfolio items" ON public.portfolio_items FOR SELECT USING (
    EXISTS (SELECT 1 FROM public.portfolios p WHERE p.id = portfolio_items.portfolio_id AND p.user_id = auth.uid())
);

CREATE POLICY "Users can view their own trade history" ON public.trade_history FOR SELECT USING (auth.uid() = user_id);

-- ==============================================================================
-- OTOMATIK PROFIL VE PORTFOY OLUŞTURUCU TETİKLEYİCİ (TRIGGER)
-- ==============================================================================

CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS trigger AS $$
BEGIN
    -- Profil oluştur
    INSERT INTO public.profiles (id, email, full_name, profile_picture_url)
    VALUES (
        new.id,
        new.email,
        COALESCE(new.raw_user_meta_data->>'full_name', split_part(new.email, '@', 1)),
        new.raw_user_meta_data->>'avatar_url'
    );

    -- Başlangıç Portföyü oluştur (750.000 TL bakiye)
    INSERT INTO public.portfolios (user_id, balance)
    VALUES (new.id, 750000.0000);

    RETURN new;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- ==============================================================================
-- ATOMİK ALIM-SATIM SQL FONKSİYONLARI (RPC)
-- ==============================================================================

-- 1. HISSE ALIM FONKSİYONU (BUY)
CREATE OR REPLACE FUNCTION public.buy_stock(
    p_symbol VARCHAR,
    p_quantity BIGINT,
    p_price NUMERIC
)
RETURNS JSON AS $$
DECLARE
    v_user_id UUID;
    v_portfolio_id BIGINT;
    v_balance NUMERIC(19, 4);
    v_total_volume NUMERIC(19, 4);
    v_commission NUMERIC(19, 4);
    v_total_deduction NUMERIC(19, 4);
    v_existing_quantity BIGINT := 0;
    v_existing_avg_cost NUMERIC(19, 4) := 0;
    v_new_quantity BIGINT;
    v_new_avg_cost NUMERIC(19, 4);
BEGIN
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Giriş yapılmamış! (Unauthorized)';
    END IF;

    IF p_quantity <= 0 OR p_price <= 0 THEN
        RAISE EXCEPTION 'Geçersiz miktar veya fiyat!';
    END IF;

    -- Portföyü ve kilitli bakiyeyi çek (FOR UPDATE)
    SELECT id, balance INTO v_portfolio_id, v_balance
    FROM public.portfolios
    WHERE user_id = v_user_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Portföy bulunamadı!';
    END IF;

    -- Hesaplamalar (%1 Komisyon Oranı)
    v_total_volume := p_quantity * p_price;
    v_commission := ROUND(v_total_volume * 0.01, 4);
    v_total_deduction := v_total_volume + v_commission;

    IF v_balance < v_total_deduction THEN
        RAISE EXCEPTION 'Yetersiz bakiye! İşlem masrafları dahil % TL gerekiyor. Mevcut bakiye: % TL', v_total_deduction, v_balance;
    END IF;

    -- 1. Bakiyeyi Düş
    UPDATE public.portfolios
    SET balance = balance - v_total_deduction,
        updated_at = now()
    WHERE id = v_portfolio_id;

    -- 2. Portföy Kalemini Güncelle veya Ekle
    SELECT quantity, average_cost INTO v_existing_quantity, v_existing_avg_cost
    FROM public.portfolio_items
    WHERE portfolio_id = v_portfolio_id AND stock_symbol = p_symbol;

    IF FOUND THEN
        v_new_quantity := v_existing_quantity + p_quantity;
        v_new_avg_cost := ROUND(((v_existing_quantity * v_existing_avg_cost) + v_total_volume) / v_new_quantity, 4);

        UPDATE public.portfolio_items
        SET quantity = v_new_quantity,
            average_cost = v_new_avg_cost
        WHERE portfolio_id = v_portfolio_id AND stock_symbol = p_symbol;
    ELSE
        INSERT INTO public.portfolio_items (portfolio_id, stock_symbol, quantity, average_cost)
        VALUES (v_portfolio_id, p_symbol, p_quantity, ROUND(p_price, 4));
    END IF;

    -- 3. İşlem Geçmişine Ekle
    INSERT INTO public.trade_history (user_id, stock_symbol, type, quantity, price, commission, total_amount)
    VALUES (v_user_id, p_symbol, 'BUY', p_quantity, p_price, v_commission, v_total_deduction);

    RETURN json_build_object(
        'success', true,
        'message', 'Alım emri başarıyla gerçekleşti.',
        'remaining_balance', v_balance - v_total_deduction,
        'symbol', p_symbol,
        'quantity', p_quantity,
        'price', p_price
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 2. HISSE SATIM FONKSİYONU (SELL)
CREATE OR REPLACE FUNCTION public.sell_stock(
    p_symbol VARCHAR,
    p_quantity BIGINT,
    p_price NUMERIC
)
RETURNS JSON AS $$
DECLARE
    v_user_id UUID;
    v_portfolio_id BIGINT;
    v_existing_quantity BIGINT;
    v_total_volume NUMERIC(19, 4);
    v_commission NUMERIC(19, 4);
    v_net_proceeds NUMERIC(19, 4);
    v_new_balance NUMERIC(19, 4);
BEGIN
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Giriş yapılmamış! (Unauthorized)';
    END IF;

    IF p_quantity <= 0 OR p_price <= 0 THEN
        RAISE EXCEPTION 'Geçersiz miktar veya fiyat!';
    END IF;

    SELECT id INTO v_portfolio_id
    FROM public.portfolios
    WHERE user_id = v_user_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Portföy bulunamadı!';
    END IF;

    -- Hisse sahipliğini kontrol et
    SELECT quantity INTO v_existing_quantity
    FROM public.portfolio_items
    WHERE portfolio_id = v_portfolio_id AND stock_symbol = p_symbol
    FOR UPDATE;

    IF NOT FOUND OR v_existing_quantity < p_quantity THEN
        RAISE EXCEPTION 'Yetersiz hisse miktarı! Elinizde % adet var.', COALESCE(v_existing_quantity, 0);
    END IF;

    -- Hesaplamalar (%1 Komisyon Oranı)
    v_total_volume := p_quantity * p_price;
    v_commission := ROUND(v_total_volume * 0.01, 4);
    v_net_proceeds := v_total_volume - v_commission;

    -- 1. Bakiyeyi Artır
    UPDATE public.portfolios
    SET balance = balance + v_net_proceeds,
        updated_at = now()
    WHERE id = v_portfolio_id
    RETURNING balance INTO v_new_balance;

    -- 2. Portföy Kalemini Güncelle ya da Sıfırlandıysa Sil
    IF v_existing_quantity = p_quantity THEN
        DELETE FROM public.portfolio_items
        WHERE portfolio_id = v_portfolio_id AND stock_symbol = p_symbol;
    ELSE
        UPDATE public.portfolio_items
        SET quantity = quantity - p_quantity
        WHERE portfolio_id = v_portfolio_id AND stock_symbol = p_symbol;
    END IF;

    -- 3. İşlem Geçmişine Ekle
    INSERT INTO public.trade_history (user_id, stock_symbol, type, quantity, price, commission, total_amount)
    VALUES (v_user_id, p_symbol, 'SELL', p_quantity, p_price, v_commission, v_net_proceeds);

    RETURN json_build_object(
        'success', true,
        'message', 'Satış işlemi başarıyla gerçekleşti.',
        'new_balance', v_new_balance,
        'symbol', p_symbol,
        'quantity', p_quantity,
        'net_proceeds', v_net_proceeds
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
