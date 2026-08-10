# Landing Kit

Landing Kit is an offline-first Android companion for the first 90 minutes after arriving somewhere new. It keeps the address to show a driver, booking details, transfer instructions, a currency note, emergency information, a dietary phrase, and a short readiness checklist in one device-local card.

The durable product vision, research findings, roadmap, non-goals, and decision history live in [`docs/product-plan.md`](docs/product-plan.md).

## Current milestone

- Create, edit, view, and delete arrival kits.
- Customise each readiness checklist by adding, renaming, completing, reordering, and removing tasks.
- Apply curated checklist starters or save personal device-local templates for future kits.
- Validate required arrival details explicitly, retain past kits for reference, and recover from stale local routes.
- Import ticket images, QR screenshots, and PDFs through a compact evidence gallery or Android's Sharesheet, then preview, open, or delete them from the arrival card.
- Encrypt attachment copies with an Android Keystore-backed AES-GCM key and keep their metadata in Room.
- Optionally require biometrics or the device screen lock before decrypting attachments for viewing.
- Keep evidence until explicit deletion and offer confirmed bulk cleanup on past arrivals; nothing expires silently.
- Enter an address-first Arrival Mode for large local-language details, stay and booking information, transfer guidance, emergency help, and fast offline evidence access.
- Explore a polished sample kit for Kyoto.
- Persist trips and checklist progress in an app-private Room database.
- Persist lightweight user preferences with AndroidX DataStore.
- No account, analytics, ads, or network permission.
- Kotlin, Jetpack Compose, and Material 3 in a pragmatic feature/core module hierarchy.
- Hilt-managed dependency injection with constructor-injected ViewModels and application-scoped data services.
- Explicit Kotlin Coroutines and Flow dependencies for main-safe asynchronous work and reactive state.
- A custom editorial design system with locally bundled Fraunces display type and Manrope UI type.
- Unit tests for core domain validation, item state, and checklist editing operations.

## Architecture

```text
:app
  ├── :feature:kits
  │     ├── :core:data
  │     ├── :core:model
  │     └── :core:designsystem
  ├── :core:data ──→ :core:model
  └── :core:designsystem
```

- `:app` owns the application object and launcher activity.
- `:feature:kits` owns the Compose flow and its ViewModel.
- `:core:model` is a pure Kotlin module containing domain types and validation.
- `:core:data` owns repository contracts, Room persistence, exported database schemas, DataStore preferences, and its Hilt bindings.
- `:core:designsystem` owns the Material 3 theme and reusable visual foundations.

Within `:feature:kits`, the app shell only coordinates state and navigation. Home, create, detail, and Arrival Mode each own a focused screen package; shared presentation primitives, navigation types, form models, security coordinators, and sample data live in their own packages. This keeps screen wiring readable without promoting feature-specific UI into the global design system.

## Visual direction

The interface uses a travel-journal/boarding-pass visual language: expressive editorial headlines, calm paper surfaces, deep-teal arrival cards, warm wayfinding accents, and a four-part guided form. Directional screen transitions, a once-per-session Home reveal, animated new-trip insertion, readiness progress, and checklist completion feedback add calm spatial continuity without replaying the full choreography on every return. All illustration is drawn with Compose primitives, so there are no remote image requests or image-loading dependencies.

Fraunces and Manrope are bundled in `core/designsystem/src/main/res/font` and remain available offline. Their OFL license texts live in `licenses/`.

Room now provides the device-local source of truth through coroutine-aware DAO operations and observable `Flow` queries. Trips, personal checklist templates, and attachment metadata have separate repositories. Room migration `1 → 2` adds template tables and `2 → 3` adds attachment metadata without modifying existing trip records. Exported schemas are checked into `core/data/schemas`, giving future database changes an explicit migration and test surface. Room entities deliberately remain inside the data module; UI and domain code continue to depend only on repository contracts and domain models.

Hilt owns the application dependency graph. `LandingKitViewModel` uses constructor injection, while the data module's `DataModule` provides singleton database, repository, and preferences instances. This replaces the previous handwritten service locator in `Application`, keeps data wiring beside its implementations, and keeps dependencies explicit and testable.

The project uses only public Android/Kotlin repositories and dependencies. Grab/MyTeksi package coordinates, internal artifact repositories, services, and infrastructure are explicitly out of scope.

Dependency coordinates, plugin versions, Compose BOM, and SDK/toolchain levels are centralised in `gradle/libs.versions.toml`. Individual modules use type-safe `libs` aliases and declare only the dependencies required by that module.

## Privacy posture

The manifest intentionally omits `android.permission.INTERNET` and broad storage permissions. Trip records are stored under the app sandbox, Android backup is disabled, and nothing is exported automatically. Uninstalling the app removes its data.

User-selected ticket images and PDFs are copied into app-private storage and encrypted with AES-GCM using a non-exportable Android Keystore key. Image thumbnails are generated during import and encrypted separately, allowing the gallery to render without decrypting every full attachment. Opening an attachment creates a temporary decrypted cache file exposed through a narrowly scoped, read-only `FileProvider` grant; deleting the attachment also deletes its encrypted thumbnail and viewing preview. The original selected document is never modified. Imports are limited to 25 MB per file.

Passport scans and other identity documents remain explicitly out of scope pending a dedicated security review. Android Sharesheet intake routes single or multiple PDF/image streams through the same encrypted import pipeline after the user chooses an arrival kit. Optional biometric/device-credential protection gates attachment viewing for five minutes or until the app leaves the foreground. This is an app-level re-authorization gate; it does not make the existing encryption key biometric-bound. Evidence never expires automatically; past arrivals provide explicit bulk cleanup that leaves source files unchanged. Encrypted backup/export is not implemented yet.

## Build

Requirements: JDK 17 and Android SDK 35.

```shell
./gradlew :core:model:test :app:assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Next product slices

1. Add a local notification shortcut into Arrival Mode.
2. Complete large-text and screen-reader validation for Arrival Mode.
3. Add encrypted local export/import through Android's document picker.
4. Add accessibility, instrumentation, and screenshot tests.
5. Validate the first-90-minutes card with frequent and anxious travellers before broad itinerary features.
