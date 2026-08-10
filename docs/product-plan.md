# Landing Kit Product Plan

Last updated: 7 August 2026

## Product vision

Landing Kit is a private, offline-first Android companion for the first 90 minutes after arriving somewhere unfamiliar.

It gives travellers one calm, dependable place for the information they need immediately after landing: where they are staying, what to show a driver, how to get there, their booking reference, essential local notes, and the tasks they need to complete.

Landing Kit is not intended to become another full itinerary planner.

## Positioning

> Your calm, offline arrival pack — not another trip planner.

### Primary value proposition

- Critical arrival information remains available without connectivity.
- Everything important is reachable within one or two taps.
- No account, ads, analytics, or backend is required.
- Private travel information stays on the user's device.
- The experience reduces cognitive load at a stressful point in a journey.

## Target users

- Solo and anxious travellers.
- First-time international travellers.
- Gen Z and millennial travellers who prefer lightweight, polished tools.
- Frequent travellers who want critical details without itinerary clutter.
- Travellers visiting places with unreliable connectivity or language barriers.
- Privacy-conscious users who do not want another travel account or cloud service.
- Travellers with ADHD or executive-function challenges who benefit from reusable preparation structures.

## Jobs to be done

When I have just arrived somewhere unfamiliar, help me retrieve the few details I need immediately so I can reach my accommodation calmly without depending on connectivity, searching across apps, or remembering everything under stress.

Supporting jobs:

- Show a driver or local person my destination address in their language.
- Retrieve a booking reference or confirmation quickly.
- Remember the transfer plan from the airport or station.
- Access emergency, insurance, currency, and dietary notes.
- Know what remains unfinished before landing.
- Present a ticket, PDF, screenshot, or QR code without searching through email.
- Create a safe backup without giving a travel service permanent access to my data.

## Product principles

1. **Offline is the default**, not a paid upgrade or degraded fallback.
2. **Device-local by design**: no account, backend, ads, analytics, or automatic upload.
3. **Arrival-focused**: solve the first 90 minutes exceptionally well before expanding scope.
4. **Calm under pressure**: important content should be readable, predictable, and quickly accessible.
5. **Explicit user control**: importing, exporting, sharing, and deleting are always user-initiated.
6. **Sensitive by assumption**: travel details and attachments receive privacy-conscious defaults.
7. **No feature theatre**: avoid live or social functionality that weakens offline reliability.
8. **Maintainable Android architecture**: feature boundaries and data ownership should remain explicit.

## Validated problem signals

Public travel-community research repeatedly showed travellers creating an improvised offline arrival folder from:

- Screenshots and downloaded PDFs.
- Notes and calendar entries.
- Offline Google Drive or Dropbox folders.
- Shared WhatsApp chats.
- Spreadsheets and word-processing documents.
- Printed booking confirmations.

Repeated pain points included:

- Booking information failing to load after landing.
- Critical details being scattered across multiple apps and accounts.
- Difficulty retrieving confirmation codes, addresses, and tickets under pressure.
- Anxiety about check-in processes and unfamiliar arrival procedures.
- Reusable preparation lists for ADHD and anxious travellers.
- Sharing an itinerary or emergency information with a trusted person.
- Privacy concerns around email scanning, cloud accounts, and sensitive documents.
- Device loss, damage, and battery failure as the main limitation of device-only storage.

Representative research:

- [Creating an offline travel folder](https://www.reddit.com/r/traveladvice/comments/1vfaa0q/create_an_offline_travel_folder_before_every/)
- [Managing tickets, travel documents, and expenses](https://www.reddit.com/r/TravelHacks/comments/1gkkx39)
- [Keeping all travel bookings together](https://www.reddit.com/r/travel/comments/1qeeayz/travel_app_for_all_bookings/)
- [Keeping booking confirmations available](https://www.reddit.com/r/TravelHacks/comments/1sxzwpj/always_bring_a_hardcopy_of_your_confirmed_flight/)
- [ADHD travel preparation templates](https://www.reddit.com/r/HerOneBag/comments/1kqj5go)
- [Anxiety around hotel arrival and check-in](https://www.reddit.com/r/JapanTravelTips/comments/1gy6ofs)
- [Solo-travel safety check-ins](https://www.reddit.com/r/femaletravelers/comments/1u51qou/when_you_travel_alone_internationally_how_do_you/)
- [Travel app fatigue](https://www.flyertalk.com/forum/travel-tools/2220151-what-your-ultimate-travel-app-setup-right-now.html)
- [Privacy concerns around inferred travel plans](https://www.reddit.com/r/privacy/comments/19081tv)

Builder and app-promotion communities were treated as competition signals rather than primary evidence of user demand.

## MVP scope

### Completed

- Create and view arrival kits.
- Edit existing arrival kits while preserving checklist progress.
- Delete arrival kits through an explicit destructive confirmation.
- Add, rename, complete, reorder, and remove checklist items while preserving stable identity.
- Apply curated checklist templates and save reusable personal templates on the device.
- Guide incomplete forms with explicit inline validation and never fabricate missing arrival data.
- Keep past arrivals available in a clearly separated reference section.
- Recover gracefully when a saved navigation route points to a kit that is no longer on the device.
- Store a destination and arrival date.
- Store accommodation name and address.
- Store an address in the local language.
- Store booking reference and transfer instructions.
- Store currency, emergency, and dietary notes.
- Provide an arrival-readiness checklist.
- Persist trips and checklist state in an app-private Room database.
- Include a polished Kyoto sample kit.
- Provide Home, Create, and Detail experiences.
- Use a custom offline design system and bundled fonts.
- Use calm hybrid animations without replaying full choreography on every return.
- Keep the application free of the `INTERNET` permission.
- Use a modular Android architecture with Hilt, Coroutines, Flow, Room, DataStore, Compose, and Material 3.
- Centralise dependency versions and prohibit Grab/MyTeksi repositories and dependencies.

### Current constraints

- Data exists only on the device unless the user explicitly exports it.
- Uninstalling the app removes its data.
- Android backup is disabled.
- Images and PDFs can be imported intentionally through Android's document picker or shared into an existing kit through Android's Sharesheet.
- There is no cross-device synchronisation or real-time safety check-in.

## Prioritised roadmap

### Phase 1 — Complete the kit lifecycle

Goal: make existing kits maintainable before increasing the sensitivity of stored data.

- [x] Edit an existing arrival kit.
- [x] Delete a kit with explicit confirmation.
- [x] Add, edit, complete, reorder, and remove checklist items.
- [x] Provide reusable checklist templates.
- [x] Handle empty, invalid, and past-arrival states gracefully.

Success condition: a user can maintain a kit throughout its useful lifecycle without recreating it.

### Phase 2 — Arrival evidence attachments

Goal: eliminate searching through email, Files, and screenshots at the moment of arrival.

- [x] Import screenshots, images, PDFs, tickets, and QR codes through the Storage Access Framework.
- [x] Accept content through the Android Sharesheet.
- [x] Attach evidence to a dedicated arrival-kit section.
- [x] Encrypt imported files using Android Keystore-backed AES-GCM keys.
- [x] Add biometric or device-credential protection where appropriate.
- [x] Provide explicit per-attachment deletion and remove generated previews with the attachment.
- [ ] Define longer-term retention and automatic-expiry policy.
- [x] Keep passport scans and similarly high-risk identity documents out of the first attachment release.

Success condition: a user can retrieve a booking confirmation or QR code offline within two taps.

### Phase 3 — Arrival Mode

Goal: provide a one-handed, high-legibility experience for the moment after landing.

- Large local-language address.
- Accommodation name and booking reference.
- Transfer instructions.
- Emergency contact.
- Quick access to the most important attachment.
- Optional local notification for fast entry.
- Large text, strong contrast, and screen-reader support.

Success condition: the essential arrival flow remains usable while tired, carrying luggage, or communicating with a driver.

### Phase 4 — Controlled backup and handoff

Goal: address device loss without introducing a mandatory backend.

- Generate a concise printable arrival summary.
- Export and import an encrypted local backup.
- Share a user-selected summary through Android's system Sharesheet.
- Allow users to create a trusted-person copy without maintaining a Landing Kit account.
- Clearly warn when an export contains sensitive information.

Success condition: users can create a recoverable or trusted-person copy while remaining in control of its destination.

### Phase 5 — Preparation intelligence

Goal: reduce pre-travel cognitive load without requiring live services.

- Group tasks into before leaving, before landing, and after arrival.
- Provide local reminders using Android scheduling APIs.
- Offer reusable destination or trip-type templates.
- Surface incomplete high-priority tasks before the arrival date.
- Keep recommendations curated and packaged locally initially.

Success condition: users repeatedly reuse Landing Kit before travel rather than only opening it after landing.

### Phase 6 — Quality and product validation

- Accessibility audit and remediation.
- Instrumentation and screenshot tests.
- Room migration tests and encrypted-file tests.
- Validate with frequent, solo, first-time, anxious, and neurodivergent travellers.
- Test whether the first-90-minutes framing is immediately understandable.
- Measure whether users can find critical information faster than with their existing workaround.

## Non-goals

The following are intentionally outside the near-term product:

- Full day-by-day itinerary planning.
- Flight, train, or weather tracking.
- Booking travel inside the app.
- Live maps and navigation.
- Expense splitting or comprehensive budgeting.
- Social discovery, meetups, or public travel profiles.
- Automatic email inbox scanning.
- Mandatory accounts or cloud synchronisation.
- AI itinerary generation.
- Passport and identity-document vault functionality before a dedicated security review.

These areas are crowded, frequently require connectivity or backend infrastructure, and dilute Landing Kit's arrival-focused value.

## Backend posture

The planned product remains achievable without a proprietary backend:

- Room stores structured kit data.
- App-private encrypted storage holds attachments.
- Android Keystore protects encryption keys.
- DataStore holds lightweight preferences.
- WorkManager or alarm APIs provide local reminders.
- Android Sharesheet and Storage Access Framework handle user-controlled exchange.

A backend should only be reconsidered if validated demand emerges for real-time cross-device sync, collaborative editing, or automated safety check-ins. Those capabilities require a separate privacy, security, cost, and operational decision.

## Product success measures

Early validation should focus on behaviour rather than download volume:

- A user creates a real kit for an upcoming trip.
- A user returns to update or complete preparation tasks.
- Critical information can be retrieved offline within two taps.
- A traveller uses the local-language address or booking evidence during arrival.
- Users report lower arrival anxiety compared with Notes, screenshots, or email.
- Users understand the product without interpreting it as a general itinerary planner.
- Users trust the local-only privacy model and understand its backup trade-off.

## Decision log

### 7 August 2026

- Confirmed that Landing Kit remains arrival-focused rather than expanding into a general travel planner.
- Changed arrival information from date and time to arrival date only.
- Reordered the roadmap so edit, delete, and custom checklist functionality precede sensitive attachments.
- Chose encrypted ticket, PDF, screenshot, and QR attachments as the next major product capability after lifecycle essentials.
- Deferred passport and identity-document storage pending a dedicated threat model and security review.
- Kept the no-backend, no-account, and no-network-permission posture.
- Added this document as the durable product source of truth.
- Completed the first lifecycle slice: existing kits can be edited and deleted without losing checklist identity or completion state.
- Completed custom checklist management with inline editing, completion, ordering, addition, removal, and transactional local persistence.
- Added additive curated templates plus device-local personal templates with explicit deletion and a non-destructive Room 1-to-2 migration.
- Completed the kit lifecycle with explicit required-field validation, honest missing-kit recovery, calm empty states, and retained past-arrival references ordered behind upcoming travel.
- Limited arrival-date selection to today or later; existing kits naturally become past references over time and remain editable without allowing accidental historical dates.
- Began Phase 2 with a verified attachment vertical slice: SAF image/PDF intake, Room metadata migration `2 → 3`, 25 MB limits, Android Keystore-backed AES-GCM storage, two-tap offline viewing through a narrow `FileProvider`, and explicit deletion that leaves the source document unchanged.
- Kept Android Sharesheet intake, biometric/device-credential gating, and a long-term retention policy as open Phase 2 work rather than weakening the first secure slice.
- Replaced the small attachment action with a tappable evidence tray and compact preview gallery; image thumbnails remain encrypted at rest, PDF entries use an honest document treatment, and deletion stays available directly on each preview.

### 10 August 2026

- Completed Android Sharesheet intake for single and multiple PDF/image streams, including cold-start and already-open app delivery.
- Added an arrival-kit chooser before import, then routed shared content through the existing size validation and Android Keystore-backed encrypted attachment pipeline.
- Kept attachment intake honest when no kit exists: users are asked to create a kit first rather than silently creating incomplete trip data.
- Preserved the no-backend, no-account, and no-network-permission posture; biometric/device-credential policy and long-term retention remained the next Phase 2 decisions.
- Added optional biometric or device-credential confirmation before decrypting an attachment for viewing. A successful confirmation unlocks evidence for five minutes while Landing Kit remains foregrounded; leaving the app locks it immediately.
- Required confirmation when enabling protection and when disabling it from a locked session, preventing the preference control from becoming a bypass.
- Kept the security claim precise: attachment files remain encrypted at rest, while authentication is an app-level viewing gate rather than a biometric-bound encryption-key migration.
- Left long-term attachment retention and automatic expiry as the final open Phase 2 product decision.

## Maintaining this plan

- Update the decision log whenever product scope or ordering changes.
- Move roadmap items to the completed section only after implementation and verification.
- Add research links as evidence, not as substitutes for direct user validation.
- Record any decision that would introduce a backend, account, network permission, or sensitive identity data before implementation.
