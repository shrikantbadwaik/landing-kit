# Landing Kit

Landing Kit is an offline-first Android companion for the first 90 minutes after arriving somewhere new. It keeps the address to show a driver, booking details, transfer instructions, a currency note, emergency information, a dietary phrase, and a short readiness checklist in one device-local card.

The durable product vision, research findings, roadmap, non-goals, and decision history live in [`docs/product-plan.md`](docs/product-plan.md).

## Current milestone

- Create, edit, view, and delete arrival kits.
- Explore a polished sample kit for Kyoto.
- Persist trips and checklist progress in an app-private Room database.
- Persist lightweight user preferences with AndroidX DataStore.
- No account, analytics, ads, or network permission.
- Kotlin, Jetpack Compose, and Material 3 in a pragmatic feature/core module hierarchy.
- Hilt-managed dependency injection with constructor-injected ViewModels and application-scoped data services.
- Explicit Kotlin Coroutines and Flow dependencies for main-safe asynchronous work and reactive state.
- A custom editorial design system with locally bundled Fraunces display type and Manrope UI type.
- Unit tests for core domain validation and item state.

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

Within `:feature:kits`, the app shell only coordinates state and navigation. Home, create, and detail each own a focused screen package and a nested `component` package; shared presentation primitives, navigation types, form models, and sample data live in their own packages. This keeps screen wiring readable without promoting feature-specific UI into the global design system.

## Visual direction

The interface uses a travel-journal/boarding-pass visual language: expressive editorial headlines, calm paper surfaces, deep-teal arrival cards, warm wayfinding accents, and a three-part guided form. Directional screen transitions, a once-per-session Home reveal, animated new-trip insertion, readiness progress, and checklist completion feedback add calm spatial continuity without replaying the full choreography on every return. All illustration is drawn with Compose primitives, so there are no remote image requests or image-loading dependencies.

Fraunces and Manrope are bundled in `core/designsystem/src/main/res/font` and remain available offline. Their OFL license texts live in `licenses/`.

Room now provides the device-local source of truth through coroutine-aware DAO operations and observable `Flow` queries. Its exported schema is checked into `core/data/schemas`, giving future database changes an explicit migration and test surface. The Room entities deliberately remain inside the data module; UI and domain code continue to depend only on `TripRepository` and domain models.

Hilt owns the application dependency graph. `LandingKitViewModel` uses constructor injection, while the data module's `DataModule` provides singleton database, repository, and preferences instances. This replaces the previous handwritten service locator in `Application`, keeps data wiring beside its implementations, and keeps dependencies explicit and testable.

The project uses only public Android/Kotlin repositories and dependencies. Grab/MyTeksi package coordinates, internal artifact repositories, services, and infrastructure are explicitly out of scope.

Dependency coordinates, plugin versions, Compose BOM, and SDK/toolchain levels are centralised in `gradle/libs.versions.toml`. Individual modules use type-safe `libs` aliases and declare only the dependencies required by that module.

## Privacy posture

The manifest intentionally omits `android.permission.INTERNET`. Trip records are stored under the app sandbox, Android backup is disabled, and nothing is exported automatically. Uninstalling the app removes its data.

This first milestone does **not** yet import passports, tickets, or other sensitive files. Before supporting those, add Android Keystore-backed encryption, explicit retention controls, and user-initiated encrypted backup/export through the Storage Access Framework.

## Build

Requirements: JDK 17 and Android SDK 35.

```shell
./gradlew :core:model:test :app:assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Next product slices

1. Encrypt imported documents and add QR/ticket attachments.
2. Add editable checklist items and trip editing/deletion.
3. Add a large-text, one-handed "arrival mode" available from a notification.
4. Add encrypted local export/import through Android's document picker.
5. Add accessibility, instrumentation, and screenshot tests.
6. Validate the first-90-minutes card with frequent and anxious travellers before broad itinerary features.
