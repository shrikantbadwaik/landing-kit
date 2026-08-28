# Landing Kit Product Plan

Last updated: 21 August 2026

## Product vision

Landing Kit is a private, offline-first Android companion for the first 90 minutes after arriving somewhere unfamiliar.

It gives travellers one calm, dependable place for the information they need immediately after landing: where they are staying, what to show a driver, how to get there, their booking reference, essential local notes, and the tasks they need to complete.

Landing Kit is not intended to become another full itinerary planner.

## Positioning

> Your calm, offline arrival pack — not another trip planner.

### Primary value proposition

- Critical arrival information remains available without connectivity.
- Everything important is reachable within one or two taps.
- No account, ads, or product backend is required, and every arrival feature remains available offline.
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
2. **Device-local travel data by design**: no account, product backend, ads, or automatic upload of kit content. Firebase Analytics is limited to product telemetry, with Advertising ID collection and ad-personalization signals disabled, and must never receive destinations, addresses, booking details, notes, checklist content, or attachments.
3. **Arrival-focused**: solve the first 90 minutes exceptionally well before expanding scope.
4. **Calm under pressure**: important content should be readable, predictable, and quickly accessible.
5. **Explicit user control**: importing, exporting, sharing, and deleting are always user-initiated.
6. **Sensitive by assumption**: travel details and attachments receive privacy-conscious defaults.
7. **No feature theatre**: avoid live or social functionality that weakens offline reliability.
8. **Maintainable Android architecture**: feature boundaries and data ownership should remain explicit.
9. **Plain language at the surface**: explain what users can do and what will happen; keep implementation terms in engineering documentation rather than traveller-facing copy.

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
- Follow the device light or dark appearance across every app and system surface without requiring a separate setting.
- Use calm hybrid animations without replaying full choreography on every return.
- Offer an opt-in arrival-day notification that opens the correct kit directly in Arrival Mode.
- Keep every arrival feature functional without connectivity. Firebase Analytics may use the network for product telemetry, but kit content remains device-local and no travel data is included in custom analytics events.
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
- [x] Define longer-term retention and automatic-expiry policy.
- [x] Keep passport scans and similarly high-risk identity documents out of the first attachment release.

Success condition: a user can retrieve a booking confirmation or QR code offline within two taps.

### Phase 3 — Arrival Mode

Goal: provide a one-handed, high-legibility experience for the moment after landing.

- [x] Large local-language address.
- [x] Accommodation name and booking reference.
- [x] Transfer instructions.
- [x] Emergency contact.
- [x] Quick access to the user-selected most important attachment, with the newest saved file as a deterministic fallback.
- [x] Optional local notification for fast entry.
- [x] Complete large-text and screen-reader validation. Arrival Mode is verified at 200% text in light and dark themes, remains scroll-safe with long local-language content, exposes a pane title and navigable headings, and keeps labelled actions at or above the 48dp target minimum.

Success condition: the essential arrival flow remains usable while tired, carrying luggage, or communicating with a driver.

### Phase 3.5 — Arrival resilience

Goal: prove that the critical arrival path still works when connectivity, transport, language, check-in, or device conditions are less reliable than expected.

This is a post-MVP enhancement phase and is not a prerequisite for the first beta rollout. Its features can ship independently when research and traveller testing justify them.

#### 3.5.1 — Verified offline arrival test

Priority: P0

- [x] Add a “Check my arrival kit” rehearsal that verifies essential arrival details are present.
- [x] Prove that the selected ticket or confirmation exists in app-owned storage, decrypts successfully, and can be rendered locally.
- [x] Use concrete results such as “Ready on this phone”, “Needs attention”, and “Not checked” instead of a percentage or safety score.
- [x] Record when the kit was last checked and invalidate that result after relevant details, attachments, or quick-access choices change.
- [x] Clearly distinguish a static saved copy from a live pass and explain that rotating codes or changed travel details may still require the original provider.
- [x] State honestly that readiness on this phone does not protect against a lost, broken, or discharged device.

Verification completed 11 August 2026: model, data, feature-unit, app-assembly,
Room migration, encrypted-store, and Compose accessibility suites passed. On a
Pixel 9 Pro Android 17 emulator at 200% font scale, the selected PDF produced
“Ready on this phone”, persisted after a force-stop/relaunch, changed to “Not
checked” when quick access was removed, and could be rendered from Arrival Mode
in airplane mode alongside the preferred local-language address.

Exit condition: in airplane mode, the traveller can open Arrival Mode, retrieve the local-language address, and display the selected ticket or confirmation without unavailable content or an unexpected dependency.

#### 3.5.2 — Operational Arrival Plan B

Priority: P0

- [x] Store another way to get there, user-recorded last-departure information, accommodation check-in cutoff, late-arrival instructions, and stay contact details.
- [x] Allow an optional expected-cost or payment note and another-place-to-stay note without turning Landing Kit into a live transport or booking service.
- [x] Record when volatile details were last checked in Offline Arrival Check and treat manually entered details as saved notes, never live status.
- [x] Add a focused “If plans change” card to Arrival Mode.
- [x] Extend the offline arrival test with independent Plan B readiness rules.

Verification completed 21 August 2026: model and feature unit tests, the Room
6-to-7 migration test, and Compose accessibility tests passed. On a Pixel 9 Pro
Android 17 emulator at 200% font scale, Plan B independently reported missing
recovery details, persisted its result and timestamp after force-stop/relaunch,
hid unconfigured and completed check actions when they were no longer useful,
and showed the prepared instructions without check metadata directly after the
primary transfer instructions in Arrival Mode with airplane mode enabled.

Exit condition: when the primary transfer or check-in plan fails, the traveller can find an actionable, previously prepared fallback without connectivity.

#### 3.5.3 — Private Handoff Mode

Priority: P1

- [x] Show only traveller-selected local-language content such as the stay name, address, phone number, or prepared phrase.
- [x] Hide booking references, attachments, notes, and navigation paths that could expose the rest of the kit while the phone is handed to another person.
- [x] Use large, high-contrast, scroll-safe text with screen-reader support.
- [x] Return to protected app content when the handoff ends or times out.

Verification completed 21 August 2026: feature unit tests and all ten Compose
accessibility tests passed, including selected-content isolation and automatic
timeout coverage. On a Pixel 9 Pro Android 17 emulator at 200% font scale, the
traveller could independently select the saved stay name, stay address, and
local-language address, enter an optional session-only checked phrase, present
only those details on a dedicated high-contrast screen, and return directly to
Arrival Mode with the pinned End
handoff action. Booking references, attachments, trip notes, and navigation
controls were absent from the active handoff screen.

Deferred from this sub-milestone: offline text-to-speech. Reconsider it only when the required on-device voice exists and user testing shows that speech improves the handoff.

Exit condition: a traveller can show a driver or receptionist the needed information without revealing unrelated private trip details.

#### 3.5.4 — Honest Ticket Presentation

Priority: P1

- [x] Present saved images, PDFs, tickets, and QR codes without cropping, with fit-to-screen and deliberate zoom.
- [x] Support rotation, temporary display brightness, and screen-awake behaviour while presenting the ticket.
- [x] Avoid implying that imported evidence is live, without adding saved-date or status chrome.
- [x] Keep exit and return-to-arrival actions obvious and accessible.

Research-locked presentation direction:

- Preserve the imported image or PDF as the source of truth. Do not reconstruct
  provider data as a decorative wallet card, because doing so can omit or alter
  checkpoint evidence.
- Open on a dark, distraction-free stage with the complete image or PDF page
  fitted and uncropped. Let the traveller deliberately isolate a PDF page or
  pinch and pan an image, with a visible Fit reset.
- Include every attachment from the active arrival kit. Open the pinned file
  first when one exists, order the rest newest-first, and provide explicit
  previous/next navigation. Decrypt and render only the file currently shown.
- Use only a short fade between Arrival Mode and the presenter. Avoid ticket
  folding, flipping, or other decorative choreography while someone is trying
  to scan the evidence.
- Keep a persistent close action, but omit saved-date, freshness, and status
  labels that compete with the evidence.
- Temporarily raise display brightness and keep the screen awake while the
  presenter is visible, restoring both settings on exit. The layout must remain
  usable in portrait and landscape.

References used to establish this direction: [Dribbble boarding-pass
patterns](https://dribbble.com/search/boarding-pass-ui), including the focused
[tap-to-enlarge QR interaction](https://dribbble.com/shots/2643696-Boarding-Pass-Animation);
the [Figma Apple Wallet templates](https://www.figma.com/community/file/1116649452413390881/apple-wallet-templates)
for information hierarchy; [Behance ticketing case
studies](https://www.behance.net/search/projects/ticketing%20travel%20app%20design)
and [Canva boarding-pass templates](https://www.canva.com/templates/s/boarding-pass/)
for visual comparison; and Android's [PDF viewer
guidance](https://developer.android.com/develop/ui/views/layout/pdf/pdf-viewer)
for platform rendering, zoom, pagination, and accessibility expectations.

Verification completed 22 August 2026: the targeted presenter accessibility
test, feature unit tests, and Android lint passed. On a Pixel 9 Pro Android 17
emulator at 200% text, a six-file kit opened its older pinned ticket first,
then navigated newest-first through an opaque QR screenshot, portrait and
landscape images, an eight-page mixed-orientation PDF, and an existing booking
PDF. Complete-edge fitting, focused PDF pages and return to all pages, portrait
and landscape safe areas, background return, and direct exit to Arrival Mode
were verified. Temporary brightness and screen-awake activation and restoration
are covered by the passing presenter test.

Exit condition: common portrait, landscape, long-document, and QR-code evidence can be presented reliably at a checkpoint without exposing unrelated attachments.

#### 3.5.5 — Travel-day Actions

Priority: P2

Status: Complete

- [x] Add one-tap, user-initiated call, message, copy-address, and open-in-map actions through Android system capabilities.

Deferred research: validate whether travellers want an optional prefilled “I
arrived” system share before adding it. This is tracked in Future Opportunities
and does not block this sub-milestone.

Research-locked interaction direction:

- Keep actions beside the information they use: map and copy actions belong
  with the address; call and message actions belong with the saved stay
  contact. Avoid a generic overflow menu or a separate action dashboard.
- Keep every action deliberate and reversible inside Landing Kit. Open the
  system dialler rather than placing a call, open the message composer without
  sending, and hand map queries to an installed map app. Check that a receiving
  app exists and explain when one is unavailable.
- Copy both visible address forms when a local-language address exists, mark
  the clipboard content as sensitive where Android supports it, and use the
  regular address plus stay name for the map query.
- Use compact circular symbol controls paired with short, visible labels. Keep
  their touch targets usable one-handed and their meaning clear at 200% text;
  do not hide frequent travel-day actions behind an icon-only control, gesture,
  or confirmation sheet.
- Keep the optional “I arrived” message out of this implementation until
  traveller research establishes demand and acceptable wording. If promoted,
  use Android's Sharesheet rather than a custom recipient picker, and never
  infer arrival or send in the background.

Research sources: Android's [common-intent
guidance](https://developer.android.com/guide/components/intents-common) for
safe dialler, messaging, and map handoffs; the [Android
Sharesheet](https://developer.android.com/develop/ui/compose/sharing/send) for
user-controlled sharing; [Google Maps intent
guidance](https://developer.android.com/guide/components/google-maps-intents)
for encoded location queries and receiver checks; Figma's [real-world action
option pattern library](https://www.figma.com/community/file/1597505948890421166/ios-action-options-screens-500-real-world-patterns)
for contextual placement and risk-based hierarchy; and travel-app comparisons
across [Dribbble](https://dribbble.com/search/travel-app-actions) and
[Behance](https://www.behance.net/search/projects/travel%20app%20actions) for
visible, destination-adjacent quick actions.

Excluded from this sub-milestone: background location, automatic monitoring, missed-check-in escalation, and emergency-response claims.

Exit condition: the traveller can contact the accommodation or hand an address
to another app without Landing Kit tracking them or requiring a backend.

#### 3.5.6 — Faster Entry Experiment

Priority: P2

- [ ] Prototype privacy-conscious Android app shortcuts and a home-screen widget for the next relevant Arrival Mode.
- [ ] Keep sensitive destination and accommodation details hidden by default outside the app.
- [ ] Compare app launch, notification, shortcut, and widget retrieval time with representative travellers.
- [ ] Retain only the entry points that are measurably faster and clearly understood.

Exit condition: a validated shortcut reduces time and interaction cost without leaking sensitive arrival information or cluttering the product.

Integration rules:

- The 3.5.1 readiness engine must accept independent rules so 3.5.2 and later Phase 5 preparation checks can extend it without becoming one monolithic score.
- Phase 4 handles failures that an on-device readiness test cannot solve: device loss, damage, battery exhaustion, uninstall, and device replacement.
- Phase 5 prepares reusable information and tasks that the Phase 3.5 rehearsal can verify.
- Phase 6 validates the complete chain in airplane mode, with one-handed use, large text, TalkBack, and simulated arrival failures.

Success condition: before departure, a traveller can rehearse the arrival flow and confirm that their primary details, fallback plan, human handoff, and selected ticket remain usable without Landing Kit connectivity or an account.

### Phase 4 — Controlled backup and handoff

Goal: address device loss without introducing a mandatory backend

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

## Considered, deferred, and superseded register

This register preserves ideas that came up during planning but were not required
to complete the milestone in which they appeared. An entry here is not a roadmap
commitment. It must have a concrete revisit trigger before being promoted into a
milestone.

Status meanings:

- **Future candidate**: worth retaining, but not scheduled.
- **Promoted**: now tracked by a later roadmap milestone; do not duplicate it here
  as unfinished earlier work.
- **Deferred decision**: a product or technical choice that must be resolved at a
  specific gate.
- **Superseded or rejected**: intentionally not backlog unless new evidence changes
  the decision.

This register was reconstructed on 21 August 2026 from the Phase 1–3 decision
log, Codex session `019fd6c6-a757-7791-a003-f8ffb4e35b26`, and the subsequent
Claude project handoff history.

### Phase 1 — Kit lifecycle

| Considered item | Status | Why it was parked | Revisit trigger or destination |
|---|---|---|---|
| Final product name and the possible “Thikana” rebrand | Deferred decision | Extensive naming exploration reached diminishing returns, so Landing Kit remained the working product name even though the Android package later adopted `travel.with.thikana`. | Resolve before store listing, trademark work, public privacy copy, and permanent application ID decisions. |
| Arrival date and time | Superseded | The product deliberately stores an arrival date only. A required time added form friction and implied precision the core arrival flow did not need. | Reconsider only if traveller testing demonstrates a task that cannot be handled by arrival date, check-in cutoff, or saved last-departure information. |
| Creating kits for historical arrival dates | Rejected for creation | New and edited kits allow today or a future date, while existing past arrivals remain readable and maintainable as references. | Reconsider only if importing or reconstructing past trips becomes a validated job. |

### Phase 2 — Arrival evidence

| Considered item | Status | Why it was parked | Revisit trigger or destination |
|---|---|---|---|
| Passport scans and similarly high-risk identity documents | Future candidate | The first attachment release was designed for tickets, confirmations, images, PDFs, and QR codes. Identity documents materially increase disclosure impact and recovery requirements. | Require a dedicated threat model, security review, recovery policy, and direct evidence that travellers want Landing Kit to store them. |
| Dedicated Privacy & Security settings | Future candidate | Attachment protection was retained as an optional inline control so Phase 2 could ship, but it is too prominent and repetitive as a per-kit surface. | Add when settings architecture or another privacy control is introduced; move the existing protection preference without weakening discoverability. |
| Biometric- or credential-bound encryption keys | Future security candidate | The shipped design encrypts files at rest and uses authentication as an app-level viewing gate. Binding decryption keys to authentication would change recovery, invalidation, and device-credential behaviour. | Reconsider alongside identity-document support or a higher-assurance security tier, with migration and credential-removal recovery designed first. |
| Silent attachment expiry or scheduled deletion | Rejected | Tickets and receipts may remain useful after arrival. Silent deletion conflicts with an offline reliability product, so evidence remains until explicit per-file or confirmed bulk cleanup. | Do not revive without strong retention research and a user-controlled, recoverable policy. |

### Phase 3 — Arrival Mode

| Considered item | Status | Why it was parked | Revisit trigger or destination |
|---|---|---|---|
| Proper launcher and notification icon | Future release-polish item | Notification behaviour was complete, but Android still used the default application icon. | Complete before a public beta or store release, together with final branding. |
| Exact 8:00 AM arrival reminder | Rejected | A battery-friendly inexact alarm avoids exact-alarm permission and Play policy costs. The UI therefore promises availability “from” or “around” 8:00 AM. | Reconsider only if user research proves exact-minute delivery is essential and policy costs are justified. |
| In-app ticket presentation with fit, zoom, rotation, brightness, and screen-awake behaviour | Promoted | The Phase 3 path reliably opens protected evidence, but a checkpoint-quality presenter needed its own reliability and privacy milestone. | Phase 3.5.4 — Honest Ticket Presentation. |
| Home-screen widget and app shortcuts | Promoted | Faster entry looked useful, but it was not proven faster and could expose private destination details outside the app. | Phase 3.5.6 — Faster Entry Experiment. |
| Controlled export, printable summary, encrypted backup, and trusted-person copy | Promoted | These address phone loss, damage, battery failure, uninstall, and replacement rather than the on-device Arrival Mode itself. | Phase 4 — Controlled backup and handoff. |

### Post-Phase 3 opportunity backlog

These ideas surfaced during the arrival-resilience review but were intentionally
kept outside the Phase 3.5 commitments shown above.

| Considered item | Status | Why it was parked | Revisit trigger or destination |
|---|---|---|---|
| Offline text-to-speech for handoff content | Future candidate | The required language voice may not be installed offline, and pronunciation quality varies. Visual handoff already satisfies 3.5.3 without making an unreliable speech promise. | Reconsider after user testing shows speech improves handoff and the offline readiness check can verify that the required voice is installed and tested. |
| Connectivity preparation pack | Future candidate | Roaming, eSIM, offline-map, offline-translation, Wi-Fi, and manual-network notes may help, but can easily become generic travel preparation scope. | Validate repeated arrival failures and fit it into Phase 5 preparation plus the independent readiness engine. |
| Bag recovery pack | Future candidate | Luggage photos, baggage tags, airline contacts, and missing-bag references are adjacent to arrival resilience but form a distinct recovery workflow. | Promote only after research shows it is frequent and urgent enough to remain inside the first-90-minutes boundary. |
| User-initiated “I arrived” message | Future candidate | A prefilled system share avoids tracking, but traveller demand is mixed and formal check-ins can create unwanted safety expectations. | Validate demand during 3.5.5; keep it explicit, user-initiated, and free of background monitoring. |
| Duplicate an arrival kit | Future candidate | Reuse may help frequent travellers, but curated and personal checklist templates already cover part of the need. | Validate whether users need whole-kit duplication before adding it to Phase 5. |

### Explicit boundaries, not future enhancements

The following were discussed but should not be interpreted as parked work:

- Full itinerary planning, live transport or flight tracking, booking, discovery,
  expense splitting, social feeds, automatic email scanning, and AI itinerary
  generation remain product non-goals.
- Mandatory accounts, automatic cloud synchronisation, collaborative editing, and
  automated safety check-ins require a separate backend, privacy, security, cost,
  and operational decision.
- The rejected card-animation recreation is not a future roadmap item; only calm,
  maintainable motion that improves comprehension belongs in the product.

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

Firebase Analytics is used only for product telemetry and is not a storage, synchronisation, account, or delivery backend for arrival kits. The app's travel features must not depend on Firebase availability, and kit content must never be added to analytics parameters or user properties.

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
- Completed Phase 2 with a conservative retention policy: evidence never expires silently, remains until explicit deletion, and past arrivals offer a confirmed bulk-cleanup action.
- Bulk cleanup removes app-private encrypted files, thumbnails, previews, and Room metadata while leaving every original source document unchanged.
- Chose user-controlled cleanup over scheduled auto-deletion because travel receipts and booking evidence may remain useful after arrival, and silent deletion would undermine an offline reliability product.
- Began Phase 3 with a dedicated Arrival Mode route entered from kit detail. The first verified slice prioritises the local-language address, stay, booking reference, transfer guidance, and emergency contact in a one-handed scrollable layout.
- Reused the protected attachment-opening coordinator in Detail and Arrival Mode, preserving encryption and biometric/device-credential behaviour while providing fast access to the newest saved evidence.
- Kept attachment priority explicit: newest-first is only an interim deterministic default until users can pin the file they consider most important.
- Verified Arrival Mode on the emulator, including scrolling, encrypted attachment opening in an external viewer, returning to the mode, and system Back navigation to kit detail.
- Replaced traveller-facing implementation language with plain travel language across Home, the editor, saved lists, tickets and confirmations, protection prompts, sharing, cleanup, and kit removal. Internal security and storage terminology remains available in engineering documentation where precision matters.
- Let travellers choose the ticket or confirmation that opens first in Arrival Mode. The choice is stored locally, can be cleared from the preview, and safely falls back to the newest saved file if no choice remains.
- Added a non-destructive Room `3 → 4` migration for the per-arrival quick-access choice and verified the migration on the emulator without losing the existing Kyoto kit or its saved details.
- Added an opt-in “Keep this handy” shortcut for upcoming arrivals. Android schedules it locally from 8:00 AM on the arrival date, requests notification permission only when enabled, and opens the correct kit directly in Arrival Mode from both warm and cold app states.
- Stored the choice per kit with a non-destructive Room `4 → 5` migration. Edits reschedule it, disabling or deleting the kit cancels it, and reboot, app replacement, or timezone changes restore eligible reminders without a backend.
- Kept notification content private on the lock screen and verified permission timing, Room migration, alarm scheduling/cancellation, persisted state, and direct Arrival Mode routing on the emulator.
- Verified a real posted notification end to end: it displayed the intended destination-specific copy, opened the correct Arrival Mode when tapped, cleared itself afterward, and left no active alarm after the reminder was disabled.
- Replaced the parent trip's destructive SQLite `REPLACE` write with Room `Upsert`; this prevents kit edits from cascading into deletion of saved attachment rows. Verified on the emulator by saving an existing kit and confirming its original booking PDF remained available.
- Completed Phase 3 accessibility validation on the emulator at 200% text in both light and dark themes. The long Japanese address, transfer guidance, ticket action, emergency details, and footer remain readable without clipping or overlap.
- Added an on-device Compose accessibility regression test for the Arrival Mode pane title, heading structure, labelled actions, lazy-list sections, and 48dp Back target. The test uncovered and fixed a Back-button semantic action that TalkBack could focus but could not activate.
- Corrected theme-controlled Home headings and custom-card content colors so they remain legible when the system uses dark mode.
- Completed app-wide dark mode using the device appearance. Added a deliberate semantic dark palette, dark launch and system-bar resources, theme-safe custom surfaces across Home, editor, detail, Arrival Mode, attachments, and sharing, plus an on-device contrast regression test for every Material text/container pair.
- Added Phase 3.5 as a non-blocking, evidence-led arrival-resilience phase. It focuses on offline rehearsal, explicit fallback plans, privacy-safe human handoff, faster Android entry points, reliable ticket presentation, and user-initiated contact actions without broadening Landing Kit into a live itinerary or tracking service.
- Structured Phase 3.5 into six independently shippable sub-milestones: verified offline arrival, operational Plan B, private handoff, honest ticket presentation, travel-day actions, and a measured faster-entry experiment. Connected their readiness rules and validation explicitly to Phases 4, 5, and 6.
- Connected the Android app to Firebase using the Google Services plugin `4.5.0`, Firebase BoM `34.17.0`, and the main Firebase Analytics module through the central version catalog. This intentionally replaces the earlier no-analytics/no-network posture while keeping all kit content device-local and every arrival feature independent of Firebase availability. Disabled Advertising ID collection and ad-personalization signals and removed advertising-related SDK permissions.

### 21 August 2026

- Added the considered, deferred, and superseded register after reconstructing Phase 1–3 decisions from the Codex session and Claude handoff history. Future candidates now have explicit revisit triggers, promoted ideas point to their owning milestone, and rejected decisions are separated from backlog.

## Maintaining this plan

- Update the decision log whenever product scope or ordering changes.
- Move roadmap items to the completed section only after implementation and verification.
- Add research links as evidence, not as substitutes for direct user validation.
- Record any decision that would introduce a backend, account, network permission, or sensitive identity data before implementation.
