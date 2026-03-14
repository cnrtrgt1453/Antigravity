# Feature: User Registration & Security

Authentication and user management strategy for the Antigravity platform.

## Auth Strategy
- **Hybrid Approach**: Supports local e-mail/password registration and Google Login via Firebase.
- **Firebase Integration**: Authenticates tokens for mobile security; replicates essential user info to PostgreSQL for internal logic.

## Security Layers
- **Spring Security**: Protects all private API endpoints.
- **Firebase Admin SDK**: Used in the backend to verify ID tokens from the mobile app.
- **Password Protection**: Local passwords are hashed using `BCryptPasswordEncoder`.

## User Attributes
- `id`, `email`, `fullName`, `firebaseUid`, `isActive`.
- `lastLoginAt`, `lastReportDate` (for feature triggers).
- `createdAt`, `updatedAt` (Auditing).
