# Özellik: Kullanıcı Kaydı ve Güvenlik

Antigravity platformu için kimlik doğrulama ve kullanıcı yönetimi stratejisidir.

## Kimlik Doğrulama Stratejisi
- **Hibrit Yaklaşım:** Yerel e-posta/şifre kaydı ve Firebase üzerinden Google Giriş desteği.
- **Firebase Entegrasyonu:** Mobil güvenlik için token doğrulaması yapar; temel kullanıcı bilgilerini dahili mantık için PostgreSQL'e kopyalar.

## Güvenlik Katmanları
- **Spring Security:** Tüm özel API uç noktalarını korur.
- **Firebase Admin SDK:** Mobil uygulamadan gelen ID token'larını doğrulamak için backend tarafında kullanılır.
- **Şifre Koruması:** Yerel şifreler `BCryptPasswordEncoder` kullanılarak hash'lenir.

## Kullanıcı Öznitelikleri
- `id`, `email`, `fullName`, `firebaseUid`, `isActive`.
- `lastLoginAt`, `lastReportDate` (özellik tetikleyicileri için).
- `createdAt`, `updatedAt` (Denetim/Auditing).
