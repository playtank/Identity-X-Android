## 🚀 Summary of Changes: Isolated Edge Ingestion Framework

This PR delivers a fully decoupled, high-performance offline telemetry data ingestion system. Because this feature operates as an envelope operation without an active production server infrastructure, it includes a robust, platform-agnostic network mocking sandbox layer powered natively by Ktor's `MockEngine`.

The design enforces a strict separation of concerns, ensuring high-frequency camera-telemetry actions are safely sandboxed away from mature presentation dashboards and sensitive payment listener workflows.

---

## 🏗️ Architectural Decisions & Implementation Details

### 1. Zero-Side-Effect Sandbox Isolation
The core ingestion logic is isolated inside the `:shared:edge-sync` Kotlin Multiplatform (KMP) module group. It operates on a single-entry-point layout via explicit, bounded Intents from the presentation shell. It interacts with existing utilities through a one-way path, consuming security tokens from `:core:auth` without emitting side effects back into payment flows.

### 2. Mocking Ingestion with Ktor MockEngine
To ensure the PR compiles cleanly and executes deterministic server behavior, we avoid brittle placeholder endpoints. Instead, `MockHttpClientProvider` intercepts outgoing binary transmissions. Reviewers can easily toggle `networkSimulationMode` between `SUCCESS` (200 OK), `SERVER_ERROR` (503 Service Unavailable), and `TIMEOUT` (Physical failure) to evaluate system resilience.

### 3. Transactional Offline-First Queue (Room + WorkManager Watchdog)
* **Single Source of Truth:** Captured asset bytes are directly committed to `AssetDao` (Room) before any network transmission is initiated.
* **Watchdog Controller:** `OperationalManager` queries active queue states via `getWorkInfosForUniqueWork`. If a prior worker hangs in a zombie terminal state (`FAILED` / `CANCELLED`), it forces an `ExistingWorkPolicy.REPLACE` execution pool to maintain flow safety.
* **Resilience:** If the mock server simulates a failure, `OfflineSyncWorker` returns `Result.retry()`, cleanly activating backpressure scheduling via WorkManager's exponential backoff policy.

### 4. CameraX Edge Memory Optimization
To prevent heavy object allocations, Garbage Collection thrashing, and out-of-memory (OOM) faults on wearable hardware:
* `IndustrialAssetAnalyzer` intercepts the raw Y-plane (Luminance) byte stream directly.
* A tactical physical crop matches the UI scanning reticle bounding box.
* The minimized primitive byte slice is fed directly down the I/O storage pipe without scaling heavy, uncompressed Bitmaps.
* High-frequency buffers are strictly protected via explicit `imageProxy.close()` hooks inside a guarded `finally` lock block.

---

## 🛠️ Verification & Test Scenarios Performed
1. Verified that camera preview rendering maintains a fluent 60fps frame cycle on background threads while data slices stream to disk.
2. Verified that changing `networkSimulationMode` to `SERVER_ERROR` triggers automated Backoff criteria sequentially through the custom `CoroutineWorker`.
3. Verified that Hilt dependency graph bindings compile with accurate `@AssistedInject` scopes at runtime when process instances are re-instantiated by the OS.


---

# ⚡ Module: :shared:edge-sync

An isolated, headless Kotlin Multiplatform (KMP) sub-module responsible for high-frequency data ingestion, transactional serialization, and offline-first queue synchronization.

This framework operates as a self-contained sandbox to process industrial asset metrics safely without introducing side effects or regressions into the host application's primary presentation screens or sensitive payment listener workflows.

---

## 🏗️ Internal Directory Architecture

To support clear separation of concerns and maintain a future-proof roadmap for platform portability, the module follows strict KMP source set separation:

```text
edge-sync/
 ├── build.gradle.kts     # Configures KMP multi-targets and Ktor Multiplatform dependencies
 └── src/
      ├── commonMain/     # 100% Pure Kotlin Domain & Data Infrastructure (Shared Asset)
      │    ├── domain/
      │    │    └── repository/AssetRepository.kt     # Ingestion contract abstraction
      │    └── data/
      │         ├── remote/IndustrialKtorApi.kt       # High-performance asynchronous Ktor stream handler
      │         ├── remote/MockHttpClientProvider.kt  # Native Ktor MockEngine simulation toggle
      │         └── repository/AssetRepositoryImpl.kt # Orchestrates Room DAO & Ktor API transactions
      │
      └── androidMain/    # Android-Specific System Bindings
           └── sync/
                ├── OperationalManager.kt # WorkManager self-healing watchdog controller
                └── OfflineSyncWorker.kt # CoroutineWorker with Hilt @AssistedInject capabilities