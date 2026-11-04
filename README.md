# Travel with Thikana

Travel with Thikana is an offline-first Android companion for the first 90 minutes after arriving somewhere new. It keeps the address to show a driver, booking details, transfer instructions, a currency note, emergency information, a dietary phrase, and a short readiness checklist in one device-local card.

The durable product vision, research findings, roadmap, non-goals, and decision history live in [`docs/product-plan.md`](docs/product-plan.md).

The planned [journey roadmap](docs/product-plan.md#phase-7--offline-arrivals-throughout-a-journey) groups several arrivals and hotel stays under one trip while retaining the offline arrival focus. This extension is not implemented yet; the current app stores one destination and accommodation per trip.

## Current milestone

- Create, edit, view, and delete trips.
- Customise each readiness checklist by adding, renaming, completing, reordering, and removing tasks.
- Apply curated checklist starters or save personal device-local templates for future trips.
- Validate required arrival details explicitly, retain past trips for reference, and recover from stale local routes.
- Import ticket images, QR screenshots, and PDFs through a compact evidence gallery or Android's Sharesheet, then preview, open, or delete them from the arrival card.
- Encrypt attachment copies with an Android Keystore-backed AES-GCM key and keep their metadata in Room.
- Optionally require biometrics or the device screen lock before decrypting attachments for viewing.
- Keep evidence until explicit deletion and offer confirmed bulk cleanup on past arrivals; nothing expires silently.
- Enter an address-first Arrival Mode for large local-language details, stay and booking information, transfer guidance, emergency help, and fast offline evidence access.
- Explore a polished sample trip for Kyoto.
- Persist trips and checklist progress in an app-private Room database.
- Persist lightweight user preferences with AndroidX DataStore.
- No account or ads. Firebase Analytics provides product telemetry when connectivity is available; every arrival feature remains usable offline.
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

The user-facing [Privacy Policy](docs/privacy-policy.md) describes local storage, Firebase Analytics, sharing, backups, and deletion.

The app includes Firebase Analytics and therefore declares `android.permission.INTERNET` and `android.permission.ACCESS_NETWORK_STATE`. Firebase may collect its default app, device, and session telemetry when connectivity is available. Advertising ID collection and ad-personalization signals are disabled, and advertising-related permissions contributed by the SDK are removed during manifest merging. Travel with Thikana does not send destinations, addresses, booking details, notes, checklist content, or attachments to Firebase, and no custom analytics events containing travel content are implemented. Every arrival feature remains usable without connectivity.

Trip records are stored under the app sandbox, Android backup is disabled, and no trip content is exported automatically. Uninstalling the app removes its data. Broad storage permissions remain intentionally absent.

User-selected ticket images and PDFs are copied into app-private storage and encrypted with AES-GCM using a non-exportable Android Keystore key. Image thumbnails are generated during import and encrypted separately, allowing the gallery to render without decrypting every full attachment. Opening an attachment creates a temporary decrypted cache file exposed through a narrowly scoped, read-only `FileProvider` grant; deleting the attachment also deletes its encrypted thumbnail and viewing preview. The original selected document is never modified. Imports are limited to 25 MB per file.

Passport scans and other identity documents remain explicitly out of scope pending a dedicated security review. Android Sharesheet intake routes single or multiple PDF/image streams through the same encrypted import pipeline after the user chooses a trip. Optional biometric/device-credential protection gates attachment viewing for five minutes or until the app leaves the foreground. This is an app-level re-authorization gate; it does not make the existing encryption key biometric-bound. Evidence never expires automatically; past arrivals provide explicit bulk cleanup that leaves source files unchanged.

## Naming and upgrade compatibility

The product name is **Travel with Thikana**, and saved travel records are called **trips**. The application ID remains `com.shrikantbadwaik.travel.with.thikana`. Internal `landingkit` packages, the `:feature:kits` module, resource IDs, database and preference filenames, encryption key alias, Android component names, reminder identifiers and the `.lkb` backup format retain their existing names to preserve installed data, attachments, widgets, shortcuts and older backups. New backup and PDF filenames use `travel-with-thikana`.

## Build

Requirements: JDK 17 and Android SDK 36.

```shell
./gradlew :core:model:test :app:assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Next product slices

1. Validate real hotel-change, current/next, and return-to-base problems in Phase 7.0A, including comparison with existing traveller workarounds and a single-stay control.
2. Settle explicit Arrival creation/identity rules, conservative legacy checklist mapping, the smallest sufficient relationship model, prototypes, and migration fixtures in Phase 7.0B.
3. Independently reproduce and correct Delete/Undo evidence loss, stale ticket routing, plaintext-preview lifetime, interrupted restore, and staged file/database recovery in Phase 7.0C; this work can proceed while traveller validation runs.
4. Only after all three 7.0 gates pass, deliver non-destructive schema/backup migration and legacy single-arrival compatibility in Phase 7.1A.
5. Deliver the validated same-place accommodation-change slice with correct current/next arrivals and scoped documents/readiness in Phase 7.1B. Ship an ongoing-base return flow only if specifically validated; otherwise retain contract and persistence fixtures for it.
6. Extend to multiple places, shared bases, return visits, and saved transfers in Phase 7.2 only when direct traveller evidence justifies that UI.
7. Validate country context and advanced timing before Phase 7.3; retain open-ended journeys and date-only creation.
8. Evaluate [Phase 8](docs/product-plan.md#phase-8--import-an-existing-itinerary) independently using `ImportCandidate` and field/relationship uncertainty. Fixtures can start after the 7.0 contract stabilises; text ships first and PDF requires a separate measured release gate. OCR, translation, and remote AI need separate approval.

Problem, contract, reliability, compatibility, and vertical-slice evidence determine progress rather than a percentage-readiness estimate. The committed roadmap is a proposed direction, not permission to begin migration before the 7.0 gates pass. Accessibility, offline retrieval, encrypted-file preservation, and traveller validation apply throughout. Implementation remains pending; detailed scope and exit criteria are maintained in the product plan.
