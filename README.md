# Identity-X Android

A highly scalable, enterprise-grade Android Authentication & Identity framework built with a **100% Jetpack Compose Greenfield Architecture**, **Ktor**, **Apollo GraphQL**, and **Hilt**. This project showcases production-ready CIAM (Customer Identity and Access Management) integration implementing the highest standards of mobile security and modern asynchronous stream handling.

## 🚀 Architectural & Technical Highlights

- **Hybrid Network Pipeline**: Cascading data-stream orchestration merging standard RESTful endpoints (via Ktor) and complex security profile aggregation (via Apollo GraphQL) into unified reactive pipelines using Kotlin **Flow**.
- **Modern UDF & MVI Pattern**: Strict adherence to Unidirectional Data Flow. Extracted view layer state into a single immutable `UiState`, driven deterministically by explicit user `UiIntent` via StateFlow.
- **Type-Safe Multi-Module Navigation**: Zero history-baggage navigation implementation using `kotlinx.serialization` type-safe routes, establishing full decoupling between independent feature feature modules (`:feature:login`, `:feature:dashboard`) and eliminating traditional Fragment boilerplate.
- **Hardware-Level Decoupling & Sidestepping**: Reactive execution of Android `BiometricPrompt` and runtime permissions within target Composable functions via inverse context unwrapping (`findFragmentActivity`), keeping the centralized `ViewModel` 100% pure and unit-testable.
- **KMP-Ready Engine Blueprint**: Architectural positioning utilizing Ktor's Coroutine-based I/O (**CIO**) engine strategy for core microservices and future cross-platform (Kotlin Multiplatform) extension compatibility.

## 🧱 System Topology & Dependency Graph

The project strictly enforces the **Single-Dependency Principle** to optimize Gradle's incremental compilation speed and ensure feature isolation:

```text
                       ┌──────────────────────────┐
                       │       :app (Container)   │ 
                       └────────────┬─────────────┘
                                    │ (Assembles & Coordinates)
         ┌──────────────────────────┼──────────────────────────┐
         ▼                          ▼                          ▼
┌──────────────────┐       ┌──────────────────┐       ┌──────────────────┐
│  :feature:login  │       │:feature:dashboard│       │ :feature:account │ ... (Feature Isolation)
└────────┬─────────┘       └────────┬─────────┘       └────────┬─────────┘
         │                          │                          │
         └──────────────────────────┼──────────────────────────┘
                                    ▼
                       ┌──────────────────────────┐
                       │     :core:navigation     │ (Type-Safe Route Contracts)
                       └────────────┬─────────────┘
                                    ▼
                       ┌──────────────────────────┐
                       │      :core:network       │ (Ktor / Apollo GraphQL Core Engine)
                       └──────────────────────────┘