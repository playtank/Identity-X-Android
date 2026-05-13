# Identity-X Android

A robust, production-ready Android Authentication library built with **Jetpack Compose**, **Hilt**, and **Coroutines**. This project demonstrates industry-standard CIAM (Customer Identity and Access Management) integration.

## 🚀 Features
- **OAuth 2.0 & OpenID Connect**: Full implementation of Authorization Code Flow with **PKCE**.
- **Multi-Provider Architecture**: Pluggable support for **FusionAuth**, **Okta**, and Auth0.
- **Advanced Concurrency**: Thread-safe token refreshing using Kotlin **Mutex** and **OkHttp Authenticator**.
- **Security First**: Encrypted storage using `Security-crypto` and Biometric prompt integration.
- **UDF Pattern**: State management using Unidirectional Data Flow with Compose and ViewModel.

## 🛠 Tech Stack
- **UI**: Jetpack Compose
- **DI**: Hilt / Dagger
- **Async**: Kotlin Coroutines & Flow
- **Network**: Retrofit & OkHttp
- **Storage**: EncryptedSharedPreferences
