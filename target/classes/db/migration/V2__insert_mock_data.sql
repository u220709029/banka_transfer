-- =============================================
-- V2__insert_mock_data.sql
-- Başlangıç (seed) verileri: gönderen ve alıcı kullanıcılar + hesaplar
-- Şifreler BCrypt ile hash'lenmiş haliyle insert ediliyor (düz metin: 123456)
-- =============================================

-- 1. Kullanıcıları ekle
-- BCrypt hash of "123456" → $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO users (first_name, last_name, username, email, password, phone_number, role, created_at, updated_at)
VALUES
    ('Gönderen', 'Kullanıcı', 'gonderen', 'gonderen@banka.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     '05550000001', 'USER', NOW(6), NOW(6)),

    ('Alıcı', 'Kullanıcı', 'alici', 'alici@banka.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     '05550000002', 'USER', NOW(6), NOW(6));

-- 2. Hesapları ekle
-- Gönderen hesabı: bakiye 10000, IBAN TR11...
INSERT INTO accounts (iban, account_number, balance, currency, active, user_id, created_at, updated_at)
VALUES
    ('TR110000000000000000000001', '1100000000000001', 10000.0000, 'TRY', 1,
     (SELECT id FROM users WHERE email = 'gonderen@banka.com'),
     NOW(6), NOW(6)),

-- Alıcı hesabı: bakiye 500, IBAN TR22...
    ('TR220000000000000000000002', '2200000000000002', 500.0000, 'TRY', 1,
     (SELECT id FROM users WHERE email = 'alici@banka.com'),
     NOW(6), NOW(6));
