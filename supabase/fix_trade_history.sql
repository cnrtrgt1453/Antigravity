-- ==============================================================================
-- FinanceUP - Alım/Satım İşlemlerini Kullanıcı Bazlı Veritabanına Kaydetme Düzeltmesi
-- ==============================================================================
-- Bu SQL scriptini Supabase SQL Editor'da çalıştırarak al-sat işlemlerinin
-- 'trade_history', 'portfolios' ve 'portfolio_items' tablolarına sorunsuz
-- ve kullanıcı bazlı kaydedilmesini sağlayabilirsiniz.

-- 1. Tablolardaki kısıtlayıcı stocks(symbol) foreign key engellerini kaldır
-- (Hisse sembollerinin stocks tablosunda olmaması veya .IS ile bitmesi durumundaki 23503 hatasını önler)
ALTER TABLE public.portfolio_items DROP CONSTRAINT IF EXISTS portfolio_items_stock_symbol_fkey;
ALTER TABLE public.trade_history DROP CONSTRAINT IF EXISTS trade_history_stock_symbol_fkey;
ALTER TABLE public.watchlist DROP CONSTRAINT IF EXISTS watchlist_stock_symbol_fkey;

-- portfolio_items üzerinde (portfolio_id, stock_symbol) tekillik (UNIQUE) güvencesi (ON CONFLICT için gerekli)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'portfolio_items_portfolio_id_stock_symbol_key'
    ) THEN
        BEGIN
            ALTER TABLE public.portfolio_items ADD CONSTRAINT portfolio_items_portfolio_id_stock_symbol_key UNIQUE (portfolio_id, stock_symbol);
        EXCEPTION WHEN duplicate_table OR duplicate_object THEN
            NULL;
        END;
    END IF;
END $$;

-- 2. Tablo index ve RLS kontrolleri
CREATE INDEX IF NOT EXISTS idx_trade_history_user ON public.trade_history(user_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_portfolio_items_portfolio ON public.portfolio_items(portfolio_id);

ALTER TABLE public.trade_history ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.portfolios ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.portfolio_items ENABLE ROW LEVEL SECURITY;

-- 3. RLS Politikalarının tanımlanması (Önce temizle, sonra yeniden oluştur)
DO $$
BEGIN
    -- trade_history politikaları
    DROP POLICY IF EXISTS "Users can view their own trade history" ON public.trade_history;
    DROP POLICY IF EXISTS "Users can insert their own trade history" ON public.trade_history;
    CREATE POLICY "Users can view their own trade history" ON public.trade_history FOR SELECT USING (auth.uid() = user_id);
    CREATE POLICY "Users can insert their own trade history" ON public.trade_history FOR INSERT WITH CHECK (auth.uid() = user_id);

    -- portfolios politikaları
    DROP POLICY IF EXISTS "Users can view their own portfolio" ON public.portfolios;
    DROP POLICY IF EXISTS "Users can insert their own portfolio" ON public.portfolios;
    DROP POLICY IF EXISTS "Users can update their own portfolio" ON public.portfolios;
    CREATE POLICY "Users can view their own portfolio" ON public.portfolios FOR SELECT USING (auth.uid() = user_id);
    CREATE POLICY "Users can insert their own portfolio" ON public.portfolios FOR INSERT WITH CHECK (auth.uid() = user_id);
    CREATE POLICY "Users can update their own portfolio" ON public.portfolios FOR UPDATE USING (auth.uid() = user_id);

    -- portfolio_items politikaları
    DROP POLICY IF EXISTS "Users can view their portfolio items" ON public.portfolio_items;
    DROP POLICY IF EXISTS "Users can insert their portfolio items" ON public.portfolio_items;
    DROP POLICY IF EXISTS "Users can update their portfolio items" ON public.portfolio_items;
    DROP POLICY IF EXISTS "Users can delete their portfolio items" ON public.portfolio_items;
    DROP POLICY IF EXISTS "Users can manage their portfolio items" ON public.portfolio_items;
    DROP POLICY IF EXISTS "portfolio_items_authenticated_all" ON public.portfolio_items;

    CREATE POLICY "Users can view their portfolio items" ON public.portfolio_items FOR SELECT USING (
        EXISTS (SELECT 1 FROM public.portfolios p WHERE p.id = portfolio_items.portfolio_id AND p.user_id = auth.uid())
    );
    CREATE POLICY "Users can insert their portfolio items" ON public.portfolio_items FOR INSERT WITH CHECK (
        EXISTS (SELECT 1 FROM public.portfolios p WHERE p.id = portfolio_items.portfolio_id AND p.user_id = auth.uid())
    );
    CREATE POLICY "Users can update their portfolio items" ON public.portfolio_items FOR UPDATE USING (
        EXISTS (SELECT 1 FROM public.portfolios p WHERE p.id = portfolio_items.portfolio_id AND p.user_id = auth.uid())
    );
    CREATE POLICY "Users can delete their portfolio items" ON public.portfolio_items FOR DELETE USING (
        EXISTS (SELECT 1 FROM public.portfolios p WHERE p.id = portfolio_items.portfolio_id AND p.user_id = auth.uid())
    );
END $$;

-- Yetkilendirmeler (Tablolar, Sequence'lar ve Fonksiyonlar)
GRANT ALL ON TABLE public.portfolios TO authenticated, anon, service_role;
GRANT ALL ON TABLE public.portfolio_items TO authenticated, anon, service_role;
GRANT ALL ON TABLE public.trade_history TO authenticated, anon, service_role;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO authenticated, anon, service_role;

-- 4. ATOMİK HISSE ALIM FONKSİYONU (BUY)
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
    v_clean_symbol VARCHAR;
BEGIN
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Giriş yapılmamış! (Unauthorized)';
    END IF;

    IF p_quantity <= 0 OR p_price <= 0 THEN
        RAISE EXCEPTION 'Geçersiz miktar veya fiyat!';
    END IF;

    v_clean_symbol := split_part(UPPER(TRIM(p_symbol)), '.', 1);

    -- Portföyü ve kilitli bakiyeyi çek (FOR UPDATE)
    SELECT id, balance INTO v_portfolio_id, v_balance
    FROM public.portfolios
    WHERE user_id = v_user_id
    FOR UPDATE;

    IF NOT FOUND THEN
        -- Otomatik olarak başlangıç portföyü oluştur (500.000 TL bakiye)
        INSERT INTO public.portfolios (user_id, balance)
        VALUES (v_user_id, 500000.0000)
        RETURNING id, balance INTO v_portfolio_id, v_balance;
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

    -- 2. Portföy Kalemini Güncelle veya Ekle (ATOMİK UPSERT)
    INSERT INTO public.portfolio_items (portfolio_id, stock_symbol, quantity, average_cost)
    VALUES (v_portfolio_id, v_clean_symbol, p_quantity, ROUND(p_price, 4))
    ON CONFLICT (portfolio_id, stock_symbol) DO UPDATE
    SET average_cost = ROUND(((portfolio_items.quantity * portfolio_items.average_cost) + (p_quantity * p_price)) / (portfolio_items.quantity + p_quantity), 4),
        quantity = portfolio_items.quantity + p_quantity;

    -- 3. İşlem Geçmişine Ekle (Kullanıcıya özel işlem listesi)
    INSERT INTO public.trade_history (user_id, stock_symbol, type, quantity, price, commission, total_amount, timestamp)
    VALUES (v_user_id, v_clean_symbol, 'BUY', p_quantity, p_price, v_commission, v_total_deduction, now());

    RETURN json_build_object(
        'success', true,
        'message', 'Alım emri başarıyla gerçekleşti.',
        'remaining_balance', v_balance - v_total_deduction,
        'symbol', v_clean_symbol,
        'quantity', p_quantity,
        'price', p_price
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

-- 5. ATOMİK HISSE SATIM FONKSİYONU (SELL)
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
    v_item_id BIGINT;
    v_total_volume NUMERIC(19, 4);
    v_commission NUMERIC(19, 4);
    v_net_proceeds NUMERIC(19, 4);
    v_new_balance NUMERIC(19, 4);
    v_clean_symbol VARCHAR;
BEGIN
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Giriş yapılmamış! (Unauthorized)';
    END IF;

    IF p_quantity <= 0 OR p_price <= 0 THEN
        RAISE EXCEPTION 'Geçersiz miktar veya fiyat!';
    END IF;

    v_clean_symbol := split_part(UPPER(TRIM(p_symbol)), '.', 1);

    SELECT id INTO v_portfolio_id
    FROM public.portfolios
    WHERE user_id = v_user_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Portföy bulunamadı!';
    END IF;

    -- Hisse sahipliğini kontrol et
    SELECT id, quantity INTO v_item_id, v_existing_quantity
    FROM public.portfolio_items
    WHERE portfolio_id = v_portfolio_id AND (stock_symbol = v_clean_symbol OR stock_symbol = p_symbol)
    LIMIT 1
    FOR UPDATE;

    IF v_item_id IS NULL OR v_existing_quantity < p_quantity THEN
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
        WHERE id = v_item_id;
    ELSE
        UPDATE public.portfolio_items
        SET quantity = quantity - p_quantity
        WHERE id = v_item_id;
    END IF;

    -- 3. İşlem Geçmişine Ekle (Kullanıcıya özel işlem listesi)
    INSERT INTO public.trade_history (user_id, stock_symbol, type, quantity, price, commission, total_amount, timestamp)
    VALUES (v_user_id, v_clean_symbol, 'SELL', p_quantity, p_price, v_commission, v_net_proceeds, now());

    RETURN json_build_object(
        'success', true,
        'message', 'Satış işlemi başarıyla gerçekleşti.',
        'new_balance', v_new_balance,
        'symbol', v_clean_symbol,
        'quantity', p_quantity,
        'net_proceeds', v_net_proceeds
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

GRANT EXECUTE ON FUNCTION public.buy_stock(VARCHAR, BIGINT, NUMERIC) TO authenticated, anon;
GRANT EXECUTE ON FUNCTION public.sell_stock(VARCHAR, BIGINT, NUMERIC) TO authenticated, anon;
