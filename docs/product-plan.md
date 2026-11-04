# Travel with Thikana Product Plan

Last updated: 4 November 2026

## Product vision

Travel with Thikana is a private, offline-first Android companion for the first 90 minutes after arriving somewhere unfamiliar.

It gives travellers one calm, dependable place for the information they need immediately after reaching a new place: where they are staying, what to show a driver, how to get there, their booking reference, essential local notes, and the tasks they need to complete.

A journey can contain several of these arrival moments, including a hotel change within the same city. The planned extension groups related arrivals under one trip while keeping each arrival's information focused and reachable offline. The first-90-minutes purpose applies at every stop; the trip overview exists to help the traveller open the correct arrival.

Travel with Thikana is not intended to become another full itinerary planner.

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
- Keep the current stay and next arrival distinct throughout a journey with several places or hotels.

## Product principles

1. **Offline is the default**, not a paid upgrade or degraded fallback.
2. **Device-local travel data by design**: no account, product backend, ads, or automatic upload of trip content. Firebase Analytics is limited to product telemetry, with Advertising ID collection and ad-personalization signals disabled, and must never receive destinations, addresses, booking details, notes, checklist content, or attachments.
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

- Create and view arrival trips.
- Edit existing arrival trips while preserving checklist progress.
- Delete arrival trips through an explicit destructive confirmation.
- Add, rename, complete, reorder, and remove checklist items while preserving stable identity.
- Apply curated checklist templates and save reusable personal templates on the device.
- Guide incomplete forms with explicit inline validation and never fabricate missing arrival data.
- Keep past arrivals available in a clearly separated reference section.
- Recover gracefully when a saved navigation route points to a trip that is no longer on the device.
- Store a destination and arrival date.
- Store accommodation name and address.
- Store an address in the local language.
- Store booking reference and transfer instructions.
- Store currency, emergency, and dietary notes.
- Provide an arrival-readiness checklist.
- Persist trips and checklist state in an app-private Room database.
- Include a polished Kyoto sample trip.
- Provide Home, Create, and Detail experiences.
- Use a custom offline design system and bundled fonts.
- Follow the device light or dark appearance across every app and system surface without requiring a separate setting.
- Use calm hybrid animations without replaying full choreography on every return.
- Offer an opt-in arrival-day notification that opens the correct trip directly in Arrival Mode.
- Keep every arrival feature functional without connectivity. Firebase Analytics may use the network for product telemetry, but trip content remains device-local and no travel data is included in custom analytics events.
- Use a modular Android architecture with Hilt, Coroutines, Flow, Room, DataStore, Compose, and Material 3.
- Centralise dependency versions and prohibit Grab/MyTeksi repositories and dependencies.

### Current constraints

- The shipped model represents one destination, one arrival date, and one accommodation per trip. Multiple arrivals within a journey are planned in Phase 7 and are not implemented.
- Data exists only on the device unless the user explicitly exports it.
- Uninstalling the app removes its data.
- Android backup is disabled.
- Images and PDFs can be imported intentionally through Android's document picker or shared into an existing trip through Android's Sharesheet.
- There is no cross-device synchronisation or real-time safety check-in.

## Prioritised roadmap

### Phase 1 — Complete the trip lifecycle

Goal: make existing trips maintainable before increasing the sensitivity of stored data.

- [x] Edit an existing arrival trip.
- [x] Delete a trip with explicit confirmation.
- [x] Add, edit, complete, reorder, and remove checklist items.
- [x] Provide reusable checklist templates.
- [x] Handle empty, invalid, and past-arrival states gracefully.

Success condition: a user can maintain a trip throughout its useful lifecycle without recreating it.

### Phase 2 — Arrival evidence attachments

Goal: eliminate searching through email, Files, and screenshots at the moment of arrival.

- [x] Import screenshots, images, PDFs, tickets, and QR codes through the Storage Access Framework.
- [x] Accept content through the Android Sharesheet.
- [x] Attach evidence to a dedicated arrival-trip section.
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

- [x] Add a “Check my arrival trip” rehearsal that verifies essential arrival details are present.
- [x] Prove that the selected ticket or confirmation exists in app-owned storage, decrypts successfully, and can be rendered locally.
- [x] Use concrete results such as “Ready on this phone”, “Needs attention”, and “Not checked” instead of a percentage or safety score.
- [x] Record when the trip was last checked and invalidate that result after relevant details, attachments, or quick-access choices change.
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
- [x] Allow an optional expected-cost or payment note and another-place-to-stay note without turning Travel with Thikana into a live transport or booking service.
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
- [x] Hide booking references, attachments, notes, and navigation paths that could expose the rest of the trip while the phone is handed to another person.
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
- Include every attachment from the active arrival trip. Open the pinned file
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
emulator at 200% text, a six-file trip opened its older pinned ticket first,
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
- Keep every action deliberate and reversible inside Travel with Thikana. Open the
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
to another app without Travel with Thikana tracking them or requiring a backend.

#### 3.5.6 — Faster Entry Experiment

Priority: P2

Status: Complete

- [x] Prototype privacy-conscious Android app shortcuts and a home-screen widget for the next relevant Arrival Mode.
- [x] Keep sensitive destination and accommodation details hidden by default outside the app.
- [x] Compare app launch, notification, shortcut, and widget retrieval cost through hands-on validation.
- [x] Retain the widget and launcher shortcut based on the current hands-on validation.

Research-locked interaction direction:

- Treat launcher surfaces as entry points, not miniature itineraries. The
  shortcut and widget use generic Arrival Mode wording and never expose the
  destination, stay, address, date, booking reference, or contact details.
- Publish the dynamic shortcut only when a current or upcoming trip exists.
  Resolve the actual next trip inside Travel with Thikana when the entry point is used,
  keeping trip identifiers and content out of launcher-facing metadata.
- Keep the widget to one branded, glanceable state and one clear Open action.
  Tapping either entry point opens the same Arrival Mode destination so the
  experiment compares retrieval cost rather than competing feature sets.
- Hands-on validation found that both surfaces reduce interaction cost compared
  with ordinary app launch and clearly open Arrival Mode. Retain both for now;
  revisit the decision if broader beta feedback shows discoverability problems
  or privacy concerns.

Research sources: Android's [shortcut
guidance](https://developer.android.com/develop/ui/compose/system/shortcuts/creating-shortcuts)
for context-sensitive dynamic shortcuts and concise labels; Android's [widget
overview](https://developer.android.com/develop/ui/views/appwidgets/overview)
for at-a-glance content, direct navigation, and resizing; travel-widget surveys
across [Figma Community](https://www.figma.com/community/search?query=travel%20widget),
[Dribbble](https://dribbble.com/search/travel-widget), and
[Behance](https://www.behance.net/search/projects/travel%20widget) for compact
card hierarchy and a single dominant action; and [Canva's mobile-app template
library](https://www.canva.com/templates/search/travel-mobile-app/) as a broader
visual-language comparison rather than platform guidance.

Exit condition: a validated shortcut reduces time and interaction cost without leaking sensitive arrival information or cluttering the product.

Integration rules:

- The 3.5.1 readiness engine must accept independent rules so 3.5.2 and later Phase 5 preparation checks can extend it without becoming one monolithic score.
- Phase 4 handles failures that an on-device readiness test cannot solve: device loss, damage, battery exhaustion, uninstall, and device replacement.
- Phase 5 prepares reusable information and tasks that the Phase 3.5 rehearsal can verify.
- Phase 6 validates the complete chain in airplane mode, with one-handed use, large text, TalkBack, and simulated arrival failures.

Success condition: before departure, a traveller can rehearse the arrival flow and confirm that their primary details, fallback plan, human handoff, and selected ticket remain usable without Travel with Thikana connectivity or an account.

### Phase 4 — Controlled backup and handoff

Goal: address device loss without introducing a mandatory backend

- [x] Generate a concise printable arrival summary.
- [x] Export and import an encrypted local backup.
- [x] Share a user-selected summary through Android's system Sharesheet.
- [x] Allow users to create a trusted-person copy without maintaining a Travel with Thikana account.
- [x] Clearly warn when a printable summary or encrypted backup may contain sensitive travel information.

The printable-summary design follows [Android's custom-document printing
guidance](https://developer.android.com/training/printing/custom-docs), with the
system print sheet owning printer, paper-size, page-range, cancellation, and PDF
destination choices. Travel-itinerary references from [Figma
Community](https://www.figma.com/community/search?query=travel%20itinerary%20printable),
[Dribbble](https://dribbble.com/search/travel-itinerary),
[Behance](https://www.behance.net/search/projects/travel%20itinerary%20app), and
[Canva](https://www.canva.com/templates/search/travel-itinerary/) informed the
destination hierarchy and modular paper layout; Travel with Thikana deliberately keeps
the result operational and concise rather than reproducing a decorative full
itinerary.

The selected-summary handoff follows [Android's Sharesheet
guidance](https://developer.android.com/training/sharing/send): Travel with Thikana uses
`ACTION_SEND` with `Intent.createChooser()` and leaves destination ranking and
recipient choice to the system. The app never supplies its own recipient list;
the traveller reviews and removes groups of details before the system picker is
shown.

The single share-arrival-plan path fulfils the trusted-person copy requirement:
it opens with only the stay/arrival and Plan B groups selected, and its
recipient-ready plain-text copy states that no Travel with Thikana account or
installation is needed. The traveller can still add or remove groups before
choosing an app.

Success condition: users can create a recoverable or trusted-person copy while remaining in control of its destination.

### Phase 5 — Preparation intelligence

Goal: reduce pre-travel cognitive load without requiring live services.

- Group tasks into before leaving, before landing, and after arrival.
- Provide local reminders using Android scheduling APIs.
- Offer reusable destination or trip-type templates.
- Surface incomplete high-priority tasks before the arrival date.
- Keep recommendations curated and packaged locally initially.

Success condition: users repeatedly reuse Travel with Thikana before travel rather than only opening it after landing.

### Phase 6 — Quality and product validation

- Accessibility audit and remediation.
- Instrumentation and screenshot tests.
- Room migration tests and encrypted-file tests.
- Validate with frequent, solo, first-time, anxious, and neurodivergent travellers.
- Test whether the first-90-minutes framing is immediately understandable.
- Measure whether users can find critical information faster than with their existing workaround.

### Phase 7 — Offline arrivals throughout a journey

Status: planned on 3 November 2026; validation, contract, and sequencing refined on 4 November 2026; implementation has not started.

Goal: support one journey with several destinations or accommodation changes while preserving the original calm, offline arrival experience.

This is a proposed, gated direction rather than authorisation to begin schema migration. Phase 7.0 validates the traveller problem, settles the smallest sufficient behavioural contract, and independently repairs reliability weaknesses that migration would otherwise amplify. Only passing all three 7.0 gates authorises Phase 7.1A implementation. Phase numbers preserve the existing roadmap history; unfinished preparation experiments in Phase 5 are not prerequisites. Phase 6 quality and traveller validation apply to every milestone below. No release date or effort estimate is committed.

The same-place accommodation change is the strongest first hypothesis because it extends the existing arrival job without requiring broad journey planning. Support for all four place/stay structures remains a design requirement so that the first schema does not create a dead end; it is not evidence that every structure should immediately receive product UI. Multi-place implementation remains conditional on traveller evidence from the earlier gates.

#### Product boundary and first delivery

The product promise remains essential arrival information ready offline, at every stop. A compact overview groups arrivals and helps choose the correct one. It does not require a daily activity schedule, complete transport bookings, exact arrival times, or a country hierarchy.

The first working demonstration is one trip with two accommodation stays in the same place: prepare the first arrival, move to the second accommodation, open the correct saved confirmation, and retain the first stay for reference. A single-place, single-stay trip must remain as simple to create and use as the current experience.

The trip owns place visits, stays, persisted arrivals, transfers, and evidence. Visits and stays are independent collections with explicit associations, not an exclusive `Trip → Stop → Stay` hierarchy. Each arrival is a stable operational target; Arrival Mode composes its display from that identity and its referenced information. This separation supports reliable ownership and navigation without requiring additional forms in the simple flow. Advanced transport timing, reusable country context, and border-entry preparation are later extensions with their own validation gates.

#### What qualifies as a place visit

A place visit records a place the traveller deliberately wants an offline arrival pack for, not every attraction, restaurant, or excursion. Apply this inclusion test:

> Does the traveller need offline destination, transfer, handoff, fallback, or return information for this place?

If not, it does not need a Thikana arrival. The traveller may prepare an incomplete visit before filling its pack, but recording sightseeing activity is not its purpose. Imports must not automatically turn every mentioned place into a visit or arrival. A hotel change or meaningful return to a base may need a new arrival within an existing visit; creating another place visit is not mandatory.

#### Reference journeys

These scenarios describe journey structures that the domain must remain capable of supporting without redesign. They are independent of destination names, specific routes, and domestic or international travel. Their inclusion here does not authorise immediate UI implementation; delivery remains subject to the milestone entry gates.

| Scenario | Representation | Required experience |
|---|---|---|
| One place, one accommodation stay | One trip, one stop, one stay | Familiar focused detail and Arrival Mode without unnecessary overview steps. |
| One place, multiple accommodation stays | One trip, one stop, several stays | Current accommodation and next arrival remain distinct. |
| Multiple places, one accommodation stay | One trip, ordered visits associated with one base stay | The base accommodation remains available throughout; visiting another place does not create another booking or check-in. |
| Multiple places, multiple accommodation stays | One trip, ordered visits associated with the relevant stays | Open the correct arrival without browsing unrelated bookings; a stay may remain the base for several visits. |
| Several areas within a region | One trip, distinct visits and relevant stay associations | Traveller chooses the grouping; accommodation location remains separate from each visited place. |
| Return to a previously visited place | A second visit with its own identity | Earlier accommodation details and completion state do not overwrite the return visit. |
| Several countries, potentially open-ended | Stops across countries, potentially without an end date | Unbooked sections, country revisits, and route changes remain possible. |

The four place/stay combinations above are acceptance scenarios, not separate creation modes. The traveller adds visits and stays as needed. Zero booked stays are also valid for drafts, transit, and visits without accommodation. “Single stay” means one accommodation period, not one unique property name: leaving and later returning under a separate booking creates another stay even at the same property.

Domestic/international context and route complexity are independent. The same model supports both; a domestic journey can have several stays, and an international journey can have just one.

#### Domain and ownership contract

| Concept | Responsibility | Rules |
|---|---|---|
| Trip | The overall journey: title, optional date range, ordered route, lifecycle; owns visits, stays, arrivals, transfers, and evidence | One Home card per journey. An unknown end date is supported. |
| Stop / place visit | A deliberate visit requiring an offline arrival pack, with optional stay associations | May be a city, region, island, neighbourhood, or traveller-defined label. Distinct repeat visits have different IDs; one visit can have several arrivals. |
| Stay | A trip-owned accommodation period, its actual location, and optional check-in/check-out dates | A visit may reference zero, one, or several stays. One stay can serve several visits. Separate accommodation periods/bookings at the same property retain separate IDs. |
| Visit–stay association | Explains how a stay relates to a particular visit | Distinguishes accommodation in the visited place from a base elsewhere; does not relocate, duplicate, or restart the stay. |
| Transfer | Saved movement instructions and evidence between locations | Optional; can cover the initial approach, an intercity journey, a hotel change, or return travel. Exact times are not required. |
| Evidence | Trip-owned protected source files with explicit associations to relevant records | One file may support several arrivals/stays/transfers without duplication; quick-access selection belongs to an arrival. |
| Arrival | A persisted operational event referencing one visit, optionally a stay and incoming transfer | Own immutable `arrivalId`; used by navigation, reminders, widget/shortcut targets, readiness snapshots, quick access, and arrival tasks. May remain invisible as a separate concept in simple UI. |
| Arrival context | A display composed from one persisted arrival and its referenced facts/evidence | A projection, not the source of operational identity. It cannot retarget an existing arrival through sorting or association changes. |

Engineering should choose concrete fields and navigation types during 7.0 within the persisted-arrival contract. An arrival without a stay can expose saved destination or transfer information and honestly show what is missing, including overnight-transit arrival needs without a fake hotel.

The domain must support a journey with one uninterrupted accommodation stay and several place visits without cloning the booking. Each stay retains its actual location/address; a base association never implies that accommodation is located in every visited place. Association rules must preserve trip ownership and referential integrity. Shared-stay changes invalidate dependent arrival checks where relevant, but neither completed check-in tasks nor readiness results are copied to newly created arrivals.

#### Arrival identity and state invariants

The minimum relationship contract is `Arrival(id, tripId, visitId, stayId?, incomingTransferId?)`. A trip owns zero or more arrivals; every non-deleted arrival references one visit in that trip and optionally a stay/transfer from the same trip. Draft visits can initially have zero arrivals; a visit or stay may be referenced by several distinct arrivals.

- Persist `arrivalId` independently of route order, labels, dates, and visit–stay associations. IDs are never reused.
- **Do not make `(visitId, stayId, incomingTransferId)` unique.** Several meaningful returns can reference the same ongoing base, visit, and transfer information while retaining separate arrival identities.
- Reordering and corrections to the same intended event preserve identity. Replacing that event with a different intended arrival creates a new ID; the previous target is cancelled or removed, not silently repurposed.
- Unlinking or deleting a referenced visit, stay, or transfer must follow explicit impact/recovery rules. It cannot redirect an existing arrival or notification to another booking. Define correction versus replacement behaviour in 7.0.
- Navigation routes, readiness snapshots, quick-access choices, checklists, and specific notification/widget/shortcut actions target `arrivalId`. A generic “next arrival” action selects a persisted eligible arrival; it does not alter that arrival's identity.
- Keep arrival lifecycle (`planned`, `cancelled`, `completed`) separate from offline readiness (`not checked`, `ready on this phone`, `needs attention`). Plan B readiness is also separate. Completion does not establish readiness, and readiness does not imply arrival occurred.
- Removal is represented by a tombstone or resolvable identity history, not an overloaded lifecycle/readiness value. Cancelled targets retain their resolvable identity; deleted targets resolve to honest removed-target recovery.
- Old notification taps show the intended arrival or an explicit cancelled/deleted response. They never automatically open a replacement. Choosing another arrival is a new traveller action.
- Cancellation/removal cancels scheduled reminders and updates published targets, while stale entry points remain safe to resolve.
- Tombstones/history retain only the minimum ID/removal metadata needed for recovery, not deleted addresses, booking references, documents, or checklist content. Define retention, explicit history purge, backup handling, and parent-trip deletion in 7.0; after purge, an unknown ID still shows honest missing-target recovery and is never recycled.

Information ownership:

- Trip: reusable overall preparation tasks and shared journey information.
- Stop: local destination notes.
- Stay: accommodation name, regular/local-language address, contact, booking reference, check-in guidance, accommodation Plan B facts, and stay-level check-in tasks.
- Arrival: event-specific transport/return tasks, quick-access selection, reminder intent, lifecycle, and offline/Plan B readiness snapshots. Referenced stay/transfer facts can be shared without copying event completion state.
- Transfer: incoming travel instructions, ticket, transport fallback, and optional endpoint timing.
- Country-entry event, if validated in 7.3: preparation for that particular crossing, including a repeated entry.
- Evidence: one encrypted file with explicit associations to the relevant contexts. A document may support more than one stay or transfer.

Checklists reuse descriptions through templates, not completed state. New arrival-level transport/return tasks start incomplete unless the traveller explicitly changes them. Stay-level check-in tasks belong to the accommodation period and remain complete on a return to that ongoing stay. A new arrival gets its own unchecked readiness snapshot; it can reuse the same evidence and stay facts, not inherit another arrival's readiness result. A return reminder is distinct from the original check-in reminder and is opt-in.

#### Interaction and design requirements

- Start with trip name or destination, first place, optional dates, and optional stay. Save an incomplete draft without inventing a hotel, address, or booking.
- Reveal “Add another stay here”, “Add another place”, and optional travel details only when needed.
- When adding a visit, allow linking an existing base stay, choosing a different stay, or leaving accommodation unspecified. Do not force a place/stay combination selector.
- Keep one Home card for the overall journey, with a clear current/next arrival indication where known.
- Use a compact ordered arrival overview for longer journeys. Collapse unnecessary hierarchy for a single stay.
- Retain the existing visual theme, background illustration behaviour, elevated detail panel, handle, and scroll treatment on focused arrival detail.
- Keep Arrival Mode address-first, readable with large text, and usable one-handed.
- Show current stay and next arrival independently, particularly on a hotel-change day.
- Keep the current base stay accessible while another place is selected. Clearly distinguish the selected place's arrival details from the base accommodation's location and return information.
- Manual selection overrides suggestions. Ambiguous or undated arrivals use an explicit chooser rather than a guessed target.
- Share, show-someone, and printable-summary flows default to the selected arrival. Whole-trip export, if offered, requires an explicit scope choice and preview.
- Do not add accounts, automatic email scanning, location tracking, live route optimisation, sightseeing discovery, or daily activity planning as dependencies.

#### Dates, lifecycle, and target selection

- Trip lifecycle, booking status, arrival lifecycle, offline readiness, and Plan B readiness answer different questions and must remain separate.
- A journey does not become past merely because its first arrival date has passed. Completion must account for known duration and explicit user control.
- Unknown end dates remain unknown; do not complete an open-ended journey from its last currently planned stay.
- Stay dates are local dates. Count nights using inclusive check-in and exclusive check-out dates without implying a midnight physical departure.
- Reject check-out before check-in when both dates are known. Gaps are valid; overlaps need a review warning rather than automatic rejection. Same-day stays need an explicit, supported interpretation.
- Permit adding a stay that has already begun within an ongoing journey. Reconstructing arbitrary historical trips is not a prerequisite.
- Reordering changes route order without silently shifting booking dates, moving evidence, or merging repeated place names.
- Visiting another place while retaining a base does not imply check-out or another check-in. A return to the same ongoing stay is distinct from a new stay at the same property.
- Refresh date-derived state at local midnight, on resume, and after relevant clock/timezone changes.
- Do not switch current accommodation at midnight solely because another booking starts that day. Suggestions must remain distinct from actual arrival/check-out confirmation.
- Reminder and notification targets use stable arrival identities. A tapped reminder opens its intended arrival, not whichever record is currently earliest.
- Cancelled/completed arrivals are excluded from automatic next-arrival suggestions; current stay access remains distinct. Deleted targets resolve through removal history or honest missing-target recovery.
- Each arrival owns its optional planned local date and reminder intent. A return arrival cannot inherit its date from the original stay check-in or a reused transfer. Unknown dates remain unknown; precise times are optional.
- Define a deterministic selection contract across overlapping trips as well as within a trip. An explicit current-trip/arrival choice takes priority among eligible targets; tied or undated candidates require a chooser. Route order, local dates, and operational completion are distinct inputs, not interchangeable sorts.
- Preserve opt-in, battery-friendly inexact reminders; exact-minute alarms are not introduced.
- Country and IANA timezone metadata can be optional foundations from the first schema design. A missing timezone remains unknown rather than inferred from nationality, phone locale, or a free-text place name.
- When destination-local reminders or optional transfer times are introduced, use the relevant location/event timezone, not the phone's current timezone. Departure and arrival endpoints can have different local dates and zones.
- Define and show the date-only reminder fallback in 7.0 before implementing it. Do not claim destination-local scheduling where no destination timezone is known.

#### Evidence, readiness, and deletion

- Persist quick access against the selected arrival, drawing from explicitly associated stay/transfer/evidence records; unrelated documents must not become its implicit default.
- Store shared evidence once and retain stable storage identity separately from route order or display names.
- Use explicit associations with referential integrity. Removing one association must preserve a file that another arrival still uses.
- Removing a visit or its base-stay association does not delete the trip-owned stay, shorten its dates, or remove its documents. Explicit stay deletion reviews affected visits and preserves independent visit/transfer information.
- Apply the arrival recovery contract when an association or referenced entity is removed. Do not let cascades destroy the ability to resolve an old arrival target honestly.
- Define explicit unlink versus delete-everywhere behaviour with clear impact confirmation. Delete an orphaned encrypted file only after its associations are safely removed; preserve original source documents.
- Scope offline and Plan B checks to their arrival dependencies. An overall count may summarise checked arrivals but must not imply that one check verifies the whole journey.
- Invalidate affected results after relevant address, instruction, document, or quick-access changes; unaffected arrivals retain their results.
- Support a deliberate “not applicable” evidence requirement for arrivals that genuinely have no booking document. Missing required evidence must not be hidden by that mechanism.
- Draft completeness, task completion, and readiness on this phone remain different states. Readiness does not guarantee live booking validity, border eligibility, personal safety, or recovery from phone loss.
- Define readiness requirements by arrival purpose: accommodation check-in, destination-only arrival, transit, or return to an ongoing base. These are requirement profiles, not mandatory creation modes. A missing stay is not automatically a failure for a non-accommodation arrival; a return does not require another check-in rehearsal. The relevant destination/return address, transfer, fallback, and evidence requirements must remain explicit.
- Fingerprint the selected arrival's effective requirements and referenced content. Apply an asynchronous check result only if its target, requirement profile, and dependency revision still match; a shared address/document edit invalidates affected results without copying another arrival's readiness.

#### Durable operations and restore contracts

- Commit related structured edits consistently, then reconcile external effects such as alarms and widgets idempotently. Check target existence, lifecycle, reminder enablement, and relevant revision again when an alarm fires; cancellation races must not post a reminder for a removed arrival.
- Use distinct Android pending-intent identity and notification tag/ID namespaces per arrival, not only intent extras or a hash-derived integer. Several arrivals on the same day must not replace each other's alarms or notifications. Preserve explicit legacy mapping during upgrade.
- Define recovery after process termination during save, unlink, file cleanup, restore, and reminder publication. Database transactions do not by themselves make encrypted-file deletion or Android scheduling atomic. Cleanup must preserve files still referenced; reconciliation must not recreate cancelled targets.
- Export a consistent snapshot of the graph, evidence references, and file content. Concurrent edits/deletion must not create dangling associations or a backup that silently omits required evidence.
- Preserve restored readiness as history, not proof of readiness on the receiving installation. Imported/restored arrivals need a fresh effective “Not checked” state until verified locally; in-place migration may retain an equivalent check where its semantics and local evidence remain unchanged.
- Define conflict handling between existing records/removal history and older backups. Deleted or cancelled arrivals must not silently become active or publish reminders on restore. Explicit reviewed restoration may recover the same original event; an intentional copy uses new IDs.
- Preserve saved reminder preferences as intent, but review/reconcile eligibility, permissions, and dates before restoring active alarms. Restoration is not permission to publish stale reminders.

These are bounded 7.0 contracts and 7.1A/7.1B verification cases, not additional itinerary functionality.

#### 7.0 — Validation, contracts, and reliability preparation

Phase 7.0 contains three required gates, not a serial work queue. Problem validation precedes final architecture approval, although behavioural design may be explored alongside research. Existing reliability repairs in 7.0C can proceed immediately and independently because they benefit the current product. No migration implementation begins until all three gates pass.

Working evidence, research protocol, and acceptance fixtures: [Phase 7 validation](phase-7-validation.md). The Tokyo accommodation-change scenario was generated synthetically during planning at the product owner’s request. It is an engineering acceptance fixture, not traveller evidence or completed traveller validation.

##### 7.0A — Problem-validation gate

Goal: establish that grouping several arrival packs solves demonstrated traveller problems without turning preparation into itinerary management.

- [ ] Study real recent or upcoming hotel-change journeys, current-versus-next confusion, and meaningful returns to an ongoing base. Use actual notes, screenshots, separate records, and retrieval workarounds rather than hypothetical feature reactions.
- [ ] Compare the existing separate-trip/card approach with simple grouped-arrival prototypes for preparation effort, correct-target selection, and offline retrieval.
- [ ] Include single-stay travellers as a control and verify that journey grouping introduces no additional organisational work for them.
- [ ] Record whether travellers describe the value as preparing for arrivals rather than organising daily activities.
- [ ] Treat any wrong address, booking, or arrival opened during evaluation as a critical design failure requiring correction and retest.
- [ ] Keep multi-place UI conditional on evidence. Designing all four place/stay structures for compatibility does not authorise implementing all four flows.

Exit condition — problem gate: repeated real-journey evidence supports the same-place accommodation-change problem; travellers can identify and retrieve the intended arrival more reliably or with less effort; and the single-stay experience remains familiar. If this evidence does not pass, narrow or stop Phase 7 before migration.

##### 7.0B — Behavioural-contract and prototype gate

Goal: settle arrival identity, creation semantics, cardinalities, selection rules, product boundaries, prototypes, and data-preservation contracts before app implementation.

- [ ] Specify all four place/stay combinations, including a shared base across several visits, draft creation, current/next selection, and incomplete dates. Preserve the validated same-place accommodation-change flow as the first implementation slice.
- [ ] Specify persisted Arrival fields/cardinalities and identity invariants, including several distinct IDs for the same reference tuple.
- [ ] Define creation commands explicitly: saving a simple trip creates one visit and one planned arrival without exposing domain terminology; accommodation remains optional when omitted; an incomplete draft visit may have no arrival; “Add another stay” and explicit “Prepare return” actions create new arrivals; linking records alone never does.
- [ ] Define correction versus replacement: edits to the same intended event preserve `arrivalId`; deliberate replacement cancels/removes the previous target and creates another; sorting, imports, date calculations, and association changes never create or retarget arrivals silently.
- [ ] Define arrival lifecycle transitions separately from offline/Plan B readiness, ambiguous-date selection, cancellation behaviour, evidence requirements, and the reminder timezone fallback.
- [ ] Define arrival-owned dates, purpose-specific readiness, and current/next selection across overlapping trips; include same-reference return arrivals on different dates.
- [ ] Specify tombstone/resolvable-history behaviour for removal, stale entry points, parent deletion, retention/purge, and backup restoration without retaining deleted travel content.
- [ ] Map existing fields to trip, visit, stay, arrival, transfer, and readiness ownership without dropping saved content.
- [ ] Preserve every ambiguous legacy checklist item together under its original migrated arrival, retaining item ID, order, wording, category, and completion. Move an item to journey or stay ownership only where reliable existing provenance or explicit traveller choice supports it; never infer ownership from task wording alone.
- [ ] Preserve existing transfer instructions through a conservative migration location. A complete transfer subsystem is not required for the accommodation-change slice.
- [ ] Define trip-owned visits/stays, their associations, accommodation-location versus base semantics, and separate bookings at a repeated property. Include unlink, cancellation, deletion, and shared-dependency behaviour.
- [ ] Separate stay-level check-in tasks from arrival-level transport/return tasks. Prototype a new return arrival to an ongoing base with no duplicate check-in or inherited readiness.
- [ ] Apply the place-visit inclusion test and distinguish a deliberate offline pack from an ordinary excursion.
- [ ] Specify stable IDs, navigation compatibility, scoped attachment associations, and deletion semantics.
- [ ] Design non-destructive Room migration and independently versioned backup conversion with representative fixtures.
- [ ] Specify consistent backup snapshots, receiving-installation readiness, restore-versus-removal-history conflicts, distinct alarm/notification identities, stale check rejection, and recovery from interrupted database/file/scheduling operations.
- [ ] Keep the implementation model no broader than the validated slice: avoid generic itinerary events, country hierarchy, polymorphic ownership, automatic transfer generation, or traveller-facing relationship terminology.
- [ ] Review simple and multi-stay wireframes against the existing design and the two-tap offline retrieval goal.

Exit condition — contract gate: product behaviour, creation/replacement commands, cardinalities, identity/recovery invariants, conservative state/task ownership, migration mapping, wireframes, and acceptance fixtures are agreed and reviewable. The model can represent all four combinations without requiring their immediate UI implementation, including distinct ongoing-base return arrivals with the same reference tuple. No readiness percentage substitutes for this gate.

##### 7.0C — Reliability-baseline gate

Goal: repair and verify existing behaviours that would create data loss, privacy, or recovery risk when shared arrival relationships are introduced.

Code inspection on 4 November 2026 identified the concerns below; the review did not reproduce them on-device. Reproduction, correction, and failure testing are part of this gate.

First repair slice on 4 November 2026: removed the misleading Undo path, added missing-trip/document ticket recovery, and made explicit document targets take precedence over pinned files. Five focused emulator tests, 53 unit tests, and the debug build passed; feature lint completed with zero errors and 13 warnings. [Delivery evidence and remaining verification](phase-7-validation.md#first-reliability-delivery--4-november-2026) record the bounded coverage. The reliability gate remains open.

Second repair slice on 4 November 2026: made viewing previews session-bounded, added startup recovery and grant-aware external-viewer retention, and coordinated PDF resource disposal. Nineteen emulator tests, 53 unit tests, the debug build, and app lint passed (zero errors, 16 dependency/version warnings). Manual rehearsal verified external URI reopening after Thikana process death and cleanup after both external and in-app viewers closed. [Preview delivery evidence](phase-7-validation.md#viewing-preview-reliability-delivery--4-november-2026) records the coverage. Interrupted restore and staged filesystem/database recovery remain pending.

Third repair slice on 4 November 2026: added durable interrupted-restore recovery, atomic metadata publication, conflict-safe document/task handling, startup access gating and retry, temporary-payload cleanup, and multi-document stream ownership. Review fixes moved startup/retry recovery into an application coroutine with loading/failure UI and deferred intent consumption, and bounded backup/journal IDs to 64 ASCII characters. All 26 core-data emulator tests, 53 unit tests, the debug build, and app lint passed (zero errors, 16 dependency/version warnings). Actual process termination/restart passed at seven restore boundaries with an isolated two-document fixture; production-app startup cleanup, asynchronous loading during a 60,000-file synthetic cleanup, and failed-recovery retry with a waiting shortcut were rehearsed while preserving Kyoto. [Restore delivery evidence and coverage limits](phase-7-validation.md#interrupted-restore-reliability-delivery--4-november-2026) record the results. Broader staged filesystem/database recovery and final combined verification remain pending; 7.0C remains open.

Fourth repair slice on 4 November 2026: journalled current single-owner document imports, document removal, clearing a trip's documents, and permanent trip deletion. Metadata commits an import; confirmed deletion intent is completed on restart. Failed file cleanup retains its journal and blocks access until retry, and trip-removal errors no longer escape the ViewModel. All repository writes and file access use the same operation lock as backup export/restore; export captures its metadata in one Room transaction and keeps the referenced files alive. Verification covered 38 core-data test cases across the full and follow-up emulator runs, 53 unit tests, build, and lint (zero errors, 16 warnings), plus 17 actual process-termination boundaries. A production-UI import/removal/filesystem-failure/retry rehearsal preserved Kyoto and removed the synthetic trip and files. [Staged-operation evidence and limits](phase-7-validation.md#staged-document-operation-delivery--4-november-2026) record the results. Shared-association unlinking, the policy for older unclaimed files, and final combined verification remain outstanding; 7.0C stays open.

Review follow-up on 4 November 2026: closed the deletion-to-reminder-cancellation gap with a durable cancellation queue independent of trip rows. The deletion journal remains until the ID is queued; scheduler reconciliation cancels the old alarm and notification before acknowledging that request or scheduling live trips. Stale scheduling reads current persisted state. Verification passed 78 distinct emulator cases across full and focused runs, 53 unit tests, build, and lint (zero errors, 16 warnings). Five actual process-termination rehearsals confirmed alarm/notification cleanup and preserved an unrelated reminder. [Reminder-cancellation evidence](phase-7-validation.md#durable-reminder-cancellation-review--4-november-2026) records the boundaries and limits. This does not close the remaining 7.0C gate.

- [x] Resolve trip Delete/Undo semantics. Removed the misleading Undo path and structured-trip-only restore method; permanent deletion now requires explicit confirmation that includes saved document copies. Verified on the emulator in the first reliability slice.
- [x] Distinguish loading from a completed empty/missing result in ticket presentation. Stale trip and deleted attachment routes show honest recovery instead of indefinite loading. Explicit document targets take precedence over pinned documents and never silently open another booking. Verified through focused emulator tests and a manual stale-trip rehearsal in the first reliability slice.
- [x] Make decrypted viewing previews session-bounded and restart-safe. Clean them after the viewing grant is no longer needed, authentication expiry, and startup recovery without revoking a file while an external viewer is actively using its granted access.
- [x] Design and verify durable recovery for interrupted backup restore. A durable journal and atomic metadata transaction support rollback before commit and completion after commit. Verified through on-disk tests and actual process termination/restart at seven restore boundaries, plus production-app startup/retry rehearsal.
- [x] Recover current single-owner document import/removal and permanent trip deletion through durable intent, metadata publication/removal, idempotent file cleanup, and gated retry. Coordinate mutations with a consistent backup snapshot. Verified by emulator tests, 17 actual process-termination boundaries, and a production-UI rehearsal.
- [ ] Use transactional metadata/association changes plus staged, idempotent filesystem operations; Room transactions cannot include file deletion. Verify restart between every stage, orphan cleanup, shared-file unlinking, and delete/restore failure paths.
- [ ] Confirm these corrections through focused automated tests and an on-device rehearsal before changing the persistence model.

Exit condition — reliability gate: the four inspected weaknesses are reproduced or disproved, accepted corrections pass automated and on-device checks, and staged file/metadata operations recover safely after interruption. No migration implementation begins with these issues unresolved.

#### 7.1A — Compatibility foundation

Goal: introduce the new persisted structure without breaking existing single-arrival use or saved data.

Entry condition: the 7.0A problem-validation, 7.0B behavioural-contract, and 7.0C reliability-baseline gates have all passed. Documentation of a proposed schema alone does not satisfy this condition.

- [ ] Introduce only the trip-owned visits, stays, persisted arrivals, evidence associations, and resolvable removal history required by the validated slice through non-destructive schema changes.
- [ ] Preserve existing transfer instructions in the conservative location agreed in 7.0B and support the nullable arrival relationship without requiring a complete transfer subsystem.
- [ ] Map each existing record to its compatible arrival identity and preserve existing single-arrival screens/operations.
- [ ] Keep ambiguous legacy checklist items on the migrated legacy arrival with exact identity, order, wording, category, and completion; do not classify them from their text.
- [ ] Preserve legacy routes, encrypted attachment storage, checklists, readiness history where valid, reminder intent, widget/shortcut routing, and quick-access choices.
- [ ] Implement version 1 backup conversion and new-format round trips, including stable arrivals and removal-history rules.
- [ ] Verify migration paths, encrypted-file readability, stale/cancelled/deleted target recovery, and interrupted/duplicate restore handling.
- [ ] Verify consistent backup export during edits, removal-history conflicts on restore, local readiness reset, and idempotent file/alarm/widget reconciliation after process termination.
- [ ] Prove the schema permits all four combinations and repeated arrival reference tuples before the multi-place UI ships.

Exit condition — compatibility gate: representative existing records and backups survive migration, single-arrival operations remain usable, and old entry points open the intended arrival or honest recovery. No unrelated arrival is substituted and no saved evidence is lost. This is independently verifiable engineering delivery; a separate public release is optional.

#### 7.1B — Multi-stay vertical delivery

Goal: support accommodation changes within one place while preserving a simple single-stay experience.

- [ ] Add optional duration and draft support on the compatibility foundation.
- [ ] Add several stays within one place and the compact arrival overview.
- [ ] Keep the journey active beyond its first arrival and expose current stay plus next arrival.
- [ ] Route detail, Arrival Mode, notifications, widgets, and shortcuts to the correct persisted arrival; apply cancellation/removal recovery.
- [ ] Verify same-day alarm/notification coexistence, cancellation races, and explicit selection across overlapping trips.
- [ ] Scope documents, quick access, readiness, Plan B, and handoff to the selected arrival, with separately owned stay check-in and arrival transport tasks.
- [ ] If 7.0A finds evidence for a traveller-facing ongoing-base return flow, deliver a distinct return arrival without duplicating a stay, completed check-in tasks, or the check-in reminder; keep its transport/return tasks and readiness independent. Otherwise verify the same-reference arrival structure through contract/persistence fixtures without shipping this flow.
- [ ] Validate the new multi-stay data through backup round trips and encrypted evidence retrieval.
- [ ] Preserve the current theme and scroll treatment; verify long content, large text, TalkBack, and airplane-mode retrieval.

Exit condition — vertical-slice gate: in airplane mode, a traveller can move between two accommodation stays within one place, retrieve the correct address and confirmation within two taps from the relevant arrival entry, and keep the first stay's content and completion history intact. A single-stay trip requires no extra organisational work. If the return flow was validated and included, it must also preserve one ongoing stay's completed check-in while retaining independent return preparation; otherwise persistence fixtures prove that later return arrivals will not require another migration.

Shared evidence, correct entry targets, and verified compatibility are required for this working slice, not follow-up reliability work.

#### 7.2 — Several places and saved transfers

Goal: support journeys through several places and return visits without turning the route into an activity itinerary.

Entry condition: the 7.1B vertical slice has passed and direct traveller evidence shows that multi-place grouping improves arrival preparation or retrieval. Structural support in the schema is not sufficient authorisation for this UI.

- [ ] Add, edit, cancel, and reorder distinct visits to places.
- [ ] Support multiple visits using one base stay, several stays associated with a visit, and visits without booked accommodation.
- [ ] Let the traveller associate an existing stay with another visit without copying its booking, documents, or check-in reminder. A new operational arrival gets a distinct ID and unchecked readiness.
- [ ] Keep base accommodation location and return details distinct from the selected visit's destination address.
- [ ] Add optional incoming travel instructions and linked transfer evidence, including same-city hotel movement and initial approach.
- [ ] Preserve booking dates, document associations, readiness, and stable targets when stops move.
- [ ] Represent gaps and overnight transit without fake accommodation.
- [ ] Keep earlier and return visits to the same place separate.
- [ ] Distinguish returning to an uninterrupted base stay from returning to the same property for a new accommodation period.
- [ ] Apply the place-visit inclusion test; ordinary sightseeing mentions do not automatically create arrival records.

Exit condition: all four place/stay combinations work without configuration modes. The traveller can navigate several places and a repeated place, with the correct incoming travel and stay information available offline for every selected arrival. A shared base remains accessible without false relocation, duplicate check-in, or lost booking information.

#### 7.3 — Discovery-gated country context and advanced timing

Goal: support flexible multi-country arrivals without adding live travel advice or mandatory precision.

Open-ended trip storage and lifecycle are foundational in 7.1A/7.1B. This milestone validates longer routes and introduces only advanced context justified by arrival needs.

- [ ] Validate reusable country notes, country revisits, and border-entry preparation with travellers before exposing additional forms.
- [ ] Support explicit reuse of country notes with visible local overrides; do not silently copy stale information.
- [ ] Keep each entry event distinct, even when returning to a previously visited country.
- [ ] Introduce optional endpoint times and IANA timezones only where needed for reminders or understanding a transfer.
- [ ] Preserve date-only creation and show unknown timing honestly.
- [ ] Support open-ended route changes, cancellations, and unbooked future sections without premature completion.
- [ ] Keep saved official links and user-checked information distinct from current entry rules or eligibility guarantees.

Exit condition: an open-ended, multi-country journey can change route and revisit a country while each selected arrival remains understandable and useful offline. Advanced fields remain optional and show a demonstrated arrival benefit.

#### Compatibility and migration release gate

Baseline reviewed on 3 November 2026: Room schema version 7; manual encrypted backup format version 1; encrypted attachment paths include the existing trip ID. Confirm the baseline again before implementation.

- Wrap each legacy record in one journey with one visit, one trip-owned stay, and one persisted arrival, retaining its legacy target ID or an explicit compatibility mapping.
- Preserve legacy IDs or an explicit compatibility mapping for attachment paths, reminders, navigation, widgets, and shortcuts. Internal names need not change for the new product model.
- Preserve checklist identity/completion, booking details, notes, reminder preferences, quick-access choices, and readable encrypted attachments.
- Leave check-out, country, timezone, and other absent fields unknown. Do not derive facts from free-text destination names.
- Preserve historical archived presentation through an explicit legacy mapping; let the traveller expand/reactivate a record. Do not reactivate all old arrivals because they lack end dates.
- Retain readiness history, but keep a result valid only where its dependency semantics remain equivalent. Changed semantics require a fresh check.
- Treat backup restoration separately from in-place migration: retained history never establishes readiness on the receiving installation, and conflicts with current removal history require explicit review.
- Read version 1 backups through an explicit conversion path. Version the new backup independently from Room, preserve shared visit–stay associations, distinct arrival IDs, and defined removal-history semantics in new-format backups; validate relationships and roll back partial restore failures.
- Test duplicate restore and shared-file deletion. Unsupported future backup versions fail clearly without changing existing records.
- Test every supported database migration path and representative backup fixtures with actual encrypted evidence. Do not use destructive migration fallback.

#### Acceptance matrix

| Scenario | Required result | Owning milestone |
|---|---|---|
| Real hotel-change problem | Grouped arrivals improve preparation or correct-target retrieval over existing workarounds; no wrong target is accepted | 7.0A |
| One place, one stay | Focused creation/detail; no mandatory multi-stop setup | 7.0A validation; 7.1A compatibility; 7.1B flow |
| One place, multiple stays | One active journey; separate stay details and completion | 7.1B |
| Multiple places, one stay | One base booking and accommodation period; stay check-in state shared, arrival transport tasks and readiness independently owned | 7.0B/7.1A model; 7.2 flow |
| Multiple places, multiple stays | Correct visit/stay associations and incoming travel; no forced one-to-one mapping | 7.0B/7.1A model; 7.2 flow |
| Hotel-change day | Current and next accommodation accessible together; no silent midnight swap | 7.1B |
| Stay already underway | Earlier check-in allowed within an ongoing journey | 7.1B |
| Missing dates or overlapping bookings | Explicit choice/review; no guessed arrival target | 7.1B |
| Independent lifecycle and readiness | Completed is not ready; ready is not completed; cancellation does not overload readiness | 7.0B contract; 7.1A persistence; 7.1B flow |
| Independent readiness | Checking or editing one arrival does not mark another ready; shared dependency changes invalidate affected snapshots | 7.1B |
| Ongoing-base return | New arrival ID and fresh transport/return tasks/readiness; same stay, completed check-in tasks, and original check-in reminder | 7.0B/7.1A model; conditional 7.1B flow when validated; later evidence-gated flow otherwise |
| Identical arrival reference tuples | Distinct arrivals persist with the same visit/stay/transfer tuple and retain independent operational state | 7.0B/7.1A persistence; conditional 7.1B flow |
| Shared confirmation PDF | Unlinking one stay leaves the other association readable | 7.1A persistence; 7.1B flow |
| Scoped handoff | Address sharing exposes only the selected arrival's reviewed details | 7.1B |
| Notification after route edit | Opens its stable intended arrival; corrected event keeps its ID, replacement never silently reuses it | 7.1A compatibility; 7.1B flow |
| Cancelled/deleted arrival target | Explicit recovery through retained identity/tombstone or honest missing-target handling; never opens a replacement automatically | 7.1A |
| Removal-history purge / parent deletion | Deleted travel content is removed; stale IDs still cannot be recycled or silently retargeted | 7.0B contract; 7.1A |
| Existing reliability baseline | Delete/restore, missing ticket routes, preview cleanup, and interrupted restore recover without data loss or indefinite loading | 7.0C |
| Ordinary excursion without an offline pack need | No automatic arrival record; deliberate meaningful arrivals remain supported | 7.0B contract; 7.2 flow |
| Midnight while app remains open | Date-derived state refreshes without false physical-arrival assumptions | 7.1B |
| Existing database and version 1 backup | Saved content and encrypted files remain usable; legacy routes resolve; no invented dates | 7.1A |
| Airplane mode, large text, TalkBack | Selected address and evidence remain reachable and readable | Every milestone |
| Return to a previously visited place | Separate visit identity; reuse an uninterrupted base or create a distinct stay for a new accommodation period as appropriate | 7.2 |
| Base outside the selected place | Clearly labelled actual accommodation location; selected destination address is not replaced by the base address | 7.2 |
| Remove a visit using a base | Stay, dates, documents, and other visit associations remain intact | 7.2 |
| Shared base backup round trip | One stay, all visit associations, and distinct arrival IDs restored; evidence remains readable without duplicated bookings | 7.1A persistence; 7.2 end-to-end |
| Overnight bus / unbooked stop | Gap is allowed; no fabricated hotel | 7.2 |
| Reordered route | Booking dates and document ownership stay unchanged | 7.2 |
| Open-ended journey | Remains open until explicitly completed or an agreed known-end rule applies | 7.1A model; 7.1B flow; extended in 7.3 |
| Phone timezone change | Known destination-local reminder intent is preserved; unknown-zone fallback is explicit | 7.0B contract; 7.1B reminders; advanced timing in 7.3 |
| Return arrivals on different dates with identical references | Each keeps its own planned date and reminder; original check-in is not reused as the return date | 7.0B contract; 7.1B |
| Destination/transit arrival without a stay | Purpose-specific readiness verifies relevant information without requiring a fake hotel | 7.0B contract; 7.2 flow |
| Overlapping trips / tied next arrivals | Explicit eligible choice or chooser; no unintended cross-trip target | 7.0B contract; 7.1B |
| Edit while an offline check is running | Stale result cannot establish readiness for changed dependencies | 7.1B |
| Two same-day reminders / cancellation race | Independent alarms/notifications; cancelled target does not post | 7.1A identity; 7.1B flow |
| Process termination during mutation / backup | Consistent graph and evidence; idempotent recovery without lost shared files or recreated reminders | 7.0C baseline; 7.1A graph migration |
| Restore older backup on a receiving installation | Historical checks are not current readiness; removed arrivals and old reminders do not reactivate without review | 7.1A |
| Re-entry to a country | New entry context; old completed entry tasks do not silently carry over | 7.3 |

Validate that travellers can identify the intended arrival and retrieve its address/evidence without increased friction in the single-stay flow. Organisation alone is not the success measure; correct offline retrieval remains the primary outcome.

#### Implementation planning and delivery gates

Track concrete evidence instead of a percentage estimate of roadmap readiness:

1. **Problem gate — 7.0A:** real-journey evidence that grouping solves hotel-change/current-next retrieval without burdening single-stay use.
2. **Contract gate — 7.0B:** reviewed creation commands, identity/cardinality invariants, lifecycle/readiness separation, conservative legacy mapping, inclusion rule, task ownership, selection/recovery rules, prototypes, and migration fixtures.
3. **Reliability gate — 7.0C:** verified delete/restore, missing-route, preview-lifetime, interrupted-restore, and staged file-operation corrections.
4. **Compatibility gate — 7.1A:** passing non-destructive migration/backup/encrypted-file checks and correct legacy/stale entry-point resolution.
5. **Vertical-slice gate — 7.1B:** passing single-stay and two-stay scenarios across offline retrieval, readiness, tasks, reminders, widgets/shortcuts, and handoff; include the ongoing-base return flow only when 7.0A validates that need, while retaining its persistence fixture in either case.

Document completion of each gate only after its own required work and verification. This roadmap update does not claim any gate has passed or authorise migration implementation.

#### Research informing this direction

The sources below informed the 3 November 2026 proposal. Vendor capabilities establish comparison patterns, not proof of traveller demand or a requirement to copy their scope.

| Source | Relevant evidence | Limit |
|---|---|---|
| [TripIt: How it works](https://www.tripit.com/web/free/how-it-works) | One trip can group several kinds of reservations. | Email forwarding and cloud services are not adopted here. |
| [Wanderlog](https://wanderlog.com/) | Places, accommodation reservations, and travel between stops have different roles. | Its daily planning, discovery, collaboration, and budgeting scope is not adopted. |
| [Schema.org: Trip](https://schema.org/Trip) | A journey may visit several places in an ordered itinerary. | Conceptual support, not a prescribed database schema. |
| [Schema.org: LodgingReservation](https://schema.org/LodgingReservation) | A stay has its own reservation identity and check-in/check-out information. | Optional dates and drafts remain necessary in this product. |
| [Matador: Banana Pancake Trail](https://matadornetwork.com/read/undiscovered-banana-pancake-trail/) | The term describes a flexible route rather than one fixed itinerary. | Published in 2018; used for the term's meaning, not current travel advice or entry rules. |
| [RFC 5545](https://www.rfc-editor.org/rfc/rfc5545.html) | Distinguishes dates, date-times, timezone context, and non-inclusive event ends. | Calendar semantics inform interval handling; they do not dictate physical hotel check-out time. |
| [IANA Time Zone Database](https://www.iana.org/time-zones) | Location-based timezone rules support event-local timing. | No timezone is guessed when the place or event lacks enough information. |
| [Android Room migrations](https://developer.android.com/training/data-storage/room/migrating-db-versions) | Supports explicit migrations and migration testing; destructive fallback can lose data. | Database tests alone do not verify encrypted files or manual backup conversion. |
| [Android PendingIntent](https://developer.android.com/reference/android/app/PendingIntent) | Intent extras alone do not distinguish pending intents; distinct matching identity or request codes are needed for separate operations. | Persistent Arrival IDs still need correct Android alarm/notification mapping and receiver-side validation. Reviewed on 4 November 2026. |

### Phase 8 — Import an existing itinerary

Status: planned on 3 November 2026; refined on 4 November 2026; implementation has not started. Independently gated product/reliability releases, not a promised fast follow or a prerequisite for Phase 7 delivery. Advanced country context in 7.3 is not a dependency.

Goal: reduce manual entry by converting traveller-supplied itinerary text or a selected PDF into a reviewable trip draft using the Phase 7 model.

This is an input shortcut for information the traveller already has. It does not generate a holiday, recommend destinations, book travel, scan an inbox, or require a full activity calendar. “Translate into our trip format” means structural conversion; language translation is a separate, uncommitted capability.

#### Import flow and scope

1. The traveller explicitly pastes text or chooses an itinerary PDF.
2. The app extracts suggestions into an `ImportCandidate`, separate from persisted trip entities.
3. A review shows proposed visits, stays, arrivals, associations, transfer information, missing fields, and unresolved field/relationship ambiguities alongside relevant source excerpts.
4. The traveller corrects, excludes, or confirms the proposed information.
5. A deliberate save creates the journey. Imported arrivals remain “Not checked” until their normal offline readiness checks run.

Supported inputs are pasted textual itineraries and PDFs with extractable text. Arbitrary layouts, unsupported languages, password-protected files, and scanned/image-only PDFs need explicit detection or honest failure handling; recognition of every document is not promised. OCR is a later candidate if traveller evidence and processing evaluation justify it.

The first release creates a new trip. Importing into an existing trip, merging bookings, and updating an earlier import are deferred until identity and conflict handling are defined. Text import ships first through 8.1; PDF import is a separate 8.2 release with its own quality gate.

#### ImportCandidate and uncertainty contract

`ImportCandidate` is an extraction/review model, not a partially saved `Trip`. It has candidate-local IDs, source excerpts/page references, proposed fields and relationships, unresolved alternatives, and traveller edits. Candidate IDs never become operational notification or navigation targets.

- Track uncertainty and confirmation separately for each field and relationship, including dates, address ownership, visit–stay base links, and proposed arrivals.
- Distinguish directly extracted, tentative, missing, conflicting, and traveller-confirmed information. Do not use one overall confidence score to hide uncertain associations.
- A place mentioned in source text is not automatically a meaningful visit or arrival. Apply the Phase 7 offline-pack inclusion rule.
- Keep an ongoing-base return distinct from a new accommodation period, even when the proposed arrival reference tuples match.
- Resolve candidate relationships and validate invariants before commit. Only deliberate save creates persisted trip/visit/stay/arrival IDs and their associations.
- No candidate schedules notifications, publishes widget/shortcut targets, or records operational readiness. Saving confirmed content creates planned arrivals with unchecked readiness and opt-in reminders.
- If the review draft is retained across app restarts, define candidate-specific storage, protection, expiry/cleanup, and provenance handling in 8.0; retained candidates remain outside operational trip repositories.

#### Mapping and review contract

- Extract only arrival-relevant content: trip title, place visits/order, accommodation periods, addresses, booking references, incoming travel instructions, and relevant notes where present.
- Support all four place/stay combinations, including one uninterrupted base stay referenced by several visits. Do not create a new hotel booking for every destination mentioned.
- Preserve distinct return visits and separate accommodation periods at the same property. A repeated name alone is insufficient to merge records.
- Keep a stay's actual accommodation location distinct from the places visited from that base.
- Preserve date-only information. Missing years, ambiguous numeric dates, relative dates, overnight transfers, and timezones require review; suggestions remain visibly tentative until confirmed.
- Never fabricate a hotel, address, check-out date, country, timezone, booking reference, or confirmation of booking validity.
- Distinguish booked accommodation from alternatives, cancelled reservations, and recommendations. Unresolved alternatives remain review items rather than active stays.
- Identify each proposed field and relationship as source-derived or needing confirmation. Keep the source excerpt/page reference available during review; uncertainty cannot be replaced with a single confidence percentage.
- Preserve provided address wording and local-language text. Any future language translation must remain visibly separate from the original, especially for driver handoff.
- Activities, restaurant suggestions, marketing copy, and other information outside the arrival model do not create new product features. Show an excluded/unmapped-content summary; offer explicit source retention rather than silently claiming the entire itinerary was imported.
- Treat document content as data, not instructions to the app or processing engine. It cannot authorise uploads, external actions, changes to existing trips, or bypassing review.
- Keep draft saving available when nonessential information is missing. Unresolved conflicts must not be presented as confirmed facts.

#### Privacy, evidence, and failure handling

- Prefer device-local processing. Select the extraction approach only after evaluating supported formats, accuracy, device cost, and offline behaviour in 8.0; neither AI nor a particular provider is assumed.
- No automatic clipboard reading, background import, or upload of text/PDF content. Paste and file selection are explicit user actions.
- OCR, language translation, and remote AI are outside 8.2 unless separately approved through a recorded scope/privacy decision. If local processing cannot meet the agreed scope, narrow it or propose that separate decision before implementation: transmitted fields, provider handling/retention, cost, consent, and network failure behaviour. Existing offline arrival features must remain independent of it.
- Keep travel content out of telemetry, crash reports, and diagnostic logs. Limit temporary copies and define their cleanup and draft-retention behaviour.
- Reuse existing file limits and safe document intake controls. Source PDF retention is explicit and uses the protected attachment pipeline, not an unprotected parallel store.
- Parsing a PDF does not attach it to every arrival. The traveller reviews evidence associations; one retained source may support several contexts without duplication.
- A parsed reference or attached PDF does not establish offline readiness. Evidence must pass the existing scoped readability checks.
- Cancellation or extraction failure leaves saved trips unchanged. Commit the confirmed structure and associations consistently; roll back partial creation and clean up only import-owned temporary data.
- Flag likely repeated imports for review, without silently merging records or blocking intentional copies.

#### 8.0 — Extraction and review feasibility

Fixture evaluation can begin once the 7.0 domain contract stabilises, before import UI implementation. It can expose model gaps without blocking Phase 7 or authorising an import release. Persisted-trip integration depends on the relevant verified Phase 7 capabilities.

- [ ] Define representative, non-sensitive fixtures for all four place/stay combinations, repeated places, ongoing-base returns, ordinary excursions, unbooked sections, alternatives, and ambiguous dates.
- [ ] Define `ImportCandidate`, candidate-local identity, field/relationship uncertainty, provenance, review edits, and commit mapping to persisted Arrival IDs.
- [ ] Evaluate text extraction and mapping approaches against those fixtures without assuming an external service.
- [ ] Define supported input formats/languages, limits, source provenance, temporary-data handling, and unsupported-file recovery.
- [ ] Specify editable preview behaviour and the boundary between tentative suggestions and confirmed trip fields.
- [ ] Predeclare measurable release thresholds, fixture/trial sample sizes, and denominators for wrong-address rate, wrong stay/visit association rate, date interpretation errors, correction time versus manual entry, and import abandonment. Measure extraction proposals and final saved errors separately.
- [ ] Compare total import/review/correction effort with manual entry using the same supported journeys. Include unsupported-input and cancellation outcomes rather than measuring successful imports alone.
- [ ] Define separate text and PDF benchmarks; confirm text-first delivery and the independently gated PDF release.
- [ ] Define the processing/privacy decision before choosing libraries or services. Keep OCR, translation, and remote AI outside the initial text/PDF commitments.

Exit condition: a bounded import scope, `ImportCandidate` contract, processing approach, and measurable release thresholds are agreed. Fixture/trial evidence must show lower entry effort without hiding uncertainty before either release proceeds. Thresholds remain to be set in 8.0; no accuracy result or fast-follow schedule is claimed.

#### 8.1 — Paste text into a reviewed trip draft

- [ ] Add explicit paste entry and cancellable processing.
- [ ] Map supported itinerary text into candidate visits/stays/arrivals and their associations, with optional saved transfers.
- [ ] Show editable source-backed review, missing information, uncertain dates, and unmapped content.
- [ ] Commit only the traveller-confirmed candidate as a new journey with stable Arrival IDs; keep lifecycle planned, readiness unchecked, and notifications opt-in.
- [ ] Verify persistence, backup round trip, duplicate-import review, and recovery from cancellation/failure.

Exit condition: the text-specific quality thresholds defined in 8.0 pass, and a traveller can paste a supported itinerary, correct its proposed structure, and save a journey with correct arrival associations and less effort than manual entry. The confirmed trip remains usable offline. No undetected critical address/association error is accepted in the release fixture results.

#### 8.2 — PDF itinerary intake

- [ ] Add explicit PDF selection and bounded extraction into the same `ImportCandidate` review flow as a separate release.
- [ ] Preserve page/source references for proposed details and handle multi-page layout ambiguity.
- [ ] Detect or clearly report protected, scanned, malformed, oversized, and unsupported PDFs. Offer text/manual entry when extraction cannot proceed.
- [ ] Offer explicit source-PDF retention and reviewed evidence associations through the existing encrypted attachment pipeline.
- [ ] Verify cancellation, temporary-file cleanup, unreadable input recovery, and retained-source readability.
- [ ] Meet the independently measured PDF thresholds; do not use text-import results as proof of PDF accuracy. OCR, language translation, and remote AI are not part of this release without a separate recorded approval.

Exit condition: the PDF-specific quality thresholds defined in 8.0 pass, and a supported PDF produces a reviewable candidate with correct visit/stay/arrival associations. Unsupported files fail honestly without losing existing data or implying successful conversion. No undetected critical address/association error is accepted in the release fixture results.

#### Acceptance and follow-up gate

| Scenario | Required result |
|---|---|
| All four place/stay combinations | Correct independent visits/stays and associations; no assumed one-to-one mapping |
| Several places using one base | One accommodation period, booking, and check-in; base location stays accurate |
| Return visit / repeat property | Preserve distinct visits and new accommodation periods where explicitly present |
| Ongoing-base return | Distinct candidate/persisted arrivals can share the same references; no duplicate stay/check-in or copied readiness |
| Ordinary excursion | Excluded unless the traveller deliberately needs an offline arrival pack |
| Uncertain relationship | Visit/stay/arrival association flagged and reviewed, even where extracted names and dates appear clear |
| Missing address, year, or end date | Clearly incomplete draft or confirmation request; no invented factual field |
| Cancelled booking or suggested alternative | Not silently promoted into an active stay |
| Unsupported or scanned PDF | Honest recovery; OCR availability is not assumed |
| Mixed itinerary and sightseeing text | Arrival fields mapped; excluded content acknowledged |
| User correction and source retention | Confirmed edits persisted; retained evidence associated only with reviewed contexts |
| Cancel, failure, or repeat import | Existing data unchanged; no orphaned partial trip or silent merge |
| Saved import in airplane mode | Arrival content works offline; parsed content is not automatically marked ready |

OCR, language translation, broader input-language support, remote AI, and updates to existing journeys remain separately evaluated candidates. Promote them only when measured import failures or traveller demand justify their complexity and the required scope/privacy decision is recorded. Review errors, correction burden, and abandonment are release criteria, not just extraction completion.

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

### Phase 1 — Trip lifecycle

| Considered item | Status | Why it was parked | Revisit trigger or destination |
|---|---|---|---|
| Product name: “Travel with Thikana” | Confirmed on 3 November 2026 | The app now uses Travel with Thikana across launcher branding, screens, widgets, shared text and exports. Saved travel records are called trips. | Use this name in store listings and public product copy. The existing application ID and storage identifiers are retained for compatibility. |
| Arrival date and time | Date-only remains the default; optional timing deferred | A required time added form friction and implied precision the core arrival flow did not need. Phase 7 preserves date-only creation. | Phase 7.3 may introduce optional endpoint timing where traveller validation proves a reminder or transfer need; required precise arrival times remain outside scope. |
| Creating trips for historical arrival dates | Current restriction; targeted change planned | Shipped creation/editing limits dates to today or later. An ongoing multi-stay journey may need a stay whose check-in has already occurred. | Phase 7.1B permits that ongoing-journey case. Arbitrary historical reconstruction is not a prerequisite. |

### Phase 2 — Arrival evidence

| Considered item | Status | Why it was parked | Revisit trigger or destination |
|---|---|---|---|
| Passport scans and similarly high-risk identity documents | Future candidate | The first attachment release was designed for tickets, confirmations, images, PDFs, and QR codes. Identity documents materially increase disclosure impact and recovery requirements. | Require a dedicated threat model, security review, recovery policy, and direct evidence that travellers want Travel with Thikana to store them. |
| Dedicated Privacy & Security settings | Future candidate | Attachment protection was retained as an optional inline control so Phase 2 could ship, but it is too prominent and repetitive as a per-trip surface. | Add when settings architecture or another privacy control is introduced; move the existing protection preference without weakening discoverability. |
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
| Duplicate an arrival trip | Future candidate | Reuse may help frequent travellers, but curated and personal checklist templates already cover part of the need. | Validate whether users need whole-trip duplication before adding it to Phase 5. |
| Several arrivals in one journey | Promoted to planned roadmap | One hotel per trip cannot represent hotel changes or repeated city visits while preserving a coherent journey. | Phase 7.1A compatibility, 7.1B multi-stay delivery, then 7.2/7.3. Persist operational Arrival IDs while retaining the first-90-minutes boundary; implementation has not started. |
| Traveller-supplied itinerary conversion | Promoted to independently gated releases | Re-entering existing itinerary text/PDFs adds preparation effort, but conversion needs its own accuracy and correction-effort evidence. | Phase 8 uses `ImportCandidate`; 8.0 fixtures can follow the stabilised 7.0 contract. Text ships first; PDF needs a separate release gate. No fast-follow schedule, itinerary generation, or automatic inbox scanning is committed. |
| OCR and updates to an existing imported journey | Future candidates | Image-only inputs and merging changes add recognition, identity, and conflict-handling requirements beyond the first import slice. | Revisit after Phase 8 measures unsupported-input frequency and correction burden; retain the explicit review/privacy gates. |

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

These areas are crowded, frequently require connectivity or backend infrastructure, and dilute Travel with Thikana's arrival-focused value.

Phase 8 converts an existing, explicitly supplied itinerary into arrival records. It does not change the non-goals of itinerary generation, automatic inbox scanning, or full day-by-day planning.

## Backend posture

The planned product remains achievable without a proprietary backend:

- Room stores structured trip data.
- App-private encrypted storage holds attachments.
- Android Keystore protects encryption keys.
- DataStore holds lightweight preferences.
- WorkManager or alarm APIs provide local reminders.
- Android Sharesheet and Storage Access Framework handle user-controlled exchange.

Firebase Analytics is used only for product telemetry and is not a storage, synchronisation, account, or delivery backend for arrival trips. The app's travel features must not depend on Firebase availability, and trip content must never be added to analytics parameters or user properties.

A backend should only be reconsidered if validated demand emerges for real-time cross-device sync, collaborative editing, or automated safety check-ins. Those capabilities require a separate privacy, security, cost, and operational decision.

Phase 8 starts with a device-local processing preference and commits to no remote provider. If its feasibility review proposes remote extraction, that requires its own explicit privacy, security, retention, consent, cost, and offline-fallback decision before implementation. It does not authorise a backend or automatic travel-content upload.

## Product success measures

Early validation should focus on behaviour rather than download volume:

- A user creates a real trip for an upcoming trip.
- A user returns to update or complete preparation tasks.
- Critical information can be retrieved offline within two taps.
- A traveller uses the local-language address or booking evidence during arrival.
- Users report lower arrival anxiety compared with Notes, screenshots, or email.
- Users understand the product without interpreting it as a general itinerary planner.
- Users trust the local-only privacy model and understand its backup trade-off.
- A traveller with several stays selects the correct current or next arrival and retrieves its address/evidence offline without opening unrelated bookings.
- A traveller with one stay experiences no unnecessary setup or navigation from the journey grouping.
- A traveller imports an existing itinerary with less total entry/correction effort than manual creation, understands uncertainty, and saves the correct arrival associations without exposing travel content unexpectedly.

## Decision log

### 7 August 2026

- Confirmed that Travel with Thikana remains arrival-focused rather than expanding into a general travel planner.
- Changed arrival information from date and time to arrival date only.
- Reordered the roadmap so edit, delete, and custom checklist functionality precede sensitive attachments.
- Chose encrypted ticket, PDF, screenshot, and QR attachments as the next major product capability after lifecycle essentials.
- Deferred passport and identity-document storage pending a dedicated threat model and security review.
- Kept the no-backend, no-account, and no-network-permission posture.
- Added this document as the durable product source of truth.
- Completed the first lifecycle slice: existing trips can be edited and deleted without losing checklist identity or completion state.
- Completed custom checklist management with inline editing, completion, ordering, addition, removal, and transactional local persistence.
- Added additive curated templates plus device-local personal templates with explicit deletion and a non-destructive Room 1-to-2 migration.
- Completed the trip lifecycle with explicit required-field validation, honest missing-trip recovery, calm empty states, and retained past-arrival references ordered behind upcoming travel.
- Limited arrival-date selection to today or later; existing trips naturally become past references over time and remain editable without allowing accidental historical dates.
- Began Phase 2 with a verified attachment vertical slice: SAF image/PDF intake, Room metadata migration `2 → 3`, 25 MB limits, Android Keystore-backed AES-GCM storage, two-tap offline viewing through a narrow `FileProvider`, and explicit deletion that leaves the source document unchanged.
- Kept Android Sharesheet intake, biometric/device-credential gating, and a long-term retention policy as open Phase 2 work rather than weakening the first secure slice.
- Replaced the small attachment action with a tappable evidence tray and compact preview gallery; image thumbnails remain encrypted at rest, PDF entries use an honest document treatment, and deletion stays available directly on each preview.

### 10 August 2026

- Completed Android Sharesheet intake for single and multiple PDF/image streams, including cold-start and already-open app delivery.
- Added an arrival-trip chooser before import, then routed shared content through the existing size validation and Android Keystore-backed encrypted attachment pipeline.
- Kept attachment intake honest when no trip exists: users are asked to create a trip first rather than silently creating incomplete trip data.
- Preserved the no-backend, no-account, and no-network-permission posture; biometric/device-credential policy and long-term retention remained the next Phase 2 decisions.
- Added optional biometric or device-credential confirmation before decrypting an attachment for viewing. A successful confirmation unlocks evidence for five minutes while Travel with Thikana remains foregrounded; leaving the app locks it immediately.
- Required confirmation when enabling protection and when disabling it from a locked session, preventing the preference control from becoming a bypass.
- Kept the security claim precise: attachment files remain encrypted at rest, while authentication is an app-level viewing gate rather than a biometric-bound encryption-key migration.
- Completed Phase 2 with a conservative retention policy: evidence never expires silently, remains until explicit deletion, and past arrivals offer a confirmed bulk-cleanup action.
- Bulk cleanup removes app-private encrypted files, thumbnails, previews, and Room metadata while leaving every original source document unchanged.
- Chose user-controlled cleanup over scheduled auto-deletion because travel receipts and booking evidence may remain useful after arrival, and silent deletion would undermine an offline reliability product.
- Began Phase 3 with a dedicated Arrival Mode route entered from trip detail. The first verified slice prioritises the local-language address, stay, booking reference, transfer guidance, and emergency contact in a one-handed scrollable layout.
- Reused the protected attachment-opening coordinator in Detail and Arrival Mode, preserving encryption and biometric/device-credential behaviour while providing fast access to the newest saved evidence.
- Kept attachment priority explicit: newest-first is only an interim deterministic default until users can pin the file they consider most important.
- Verified Arrival Mode on the emulator, including scrolling, encrypted attachment opening in an external viewer, returning to the mode, and system Back navigation to trip detail.
- Replaced traveller-facing implementation language with plain travel language across Home, the editor, saved lists, tickets and confirmations, protection prompts, sharing, cleanup, and trip removal. Internal security and storage terminology remains available in engineering documentation where precision matters.
- Let travellers choose the ticket or confirmation that opens first in Arrival Mode. The choice is stored locally, can be cleared from the preview, and safely falls back to the newest saved file if no choice remains.
- Added a non-destructive Room `3 → 4` migration for the per-arrival quick-access choice and verified the migration on the emulator without losing the existing Kyoto trip or its saved details.
- Added an opt-in “Keep this handy” shortcut for upcoming arrivals. Android schedules it locally from 8:00 AM on the arrival date, requests notification permission only when enabled, and opens the correct trip directly in Arrival Mode from both warm and cold app states.
- Stored the choice per trip with a non-destructive Room `4 → 5` migration. Edits reschedule it, disabling or deleting the trip cancels it, and reboot, app replacement, or timezone changes restore eligible reminders without a backend.
- Kept notification content private on the lock screen and verified permission timing, Room migration, alarm scheduling/cancellation, persisted state, and direct Arrival Mode routing on the emulator.
- Verified a real posted notification end to end: it displayed the intended destination-specific copy, opened the correct Arrival Mode when tapped, cleared itself afterward, and left no active alarm after the reminder was disabled.
- Replaced the parent trip's destructive SQLite `REPLACE` write with Room `Upsert`; this prevents trip edits from cascading into deletion of saved attachment rows. Verified on the emulator by saving an existing trip and confirming its original booking PDF remained available.
- Completed Phase 3 accessibility validation on the emulator at 200% text in both light and dark themes. The long Japanese address, transfer guidance, ticket action, emergency details, and footer remain readable without clipping or overlap.
- Added an on-device Compose accessibility regression test for the Arrival Mode pane title, heading structure, labelled actions, lazy-list sections, and 48dp Back target. The test uncovered and fixed a Back-button semantic action that TalkBack could focus but could not activate.
- Corrected theme-controlled Home headings and custom-card content colors so they remain legible when the system uses dark mode.
- Completed app-wide dark mode using the device appearance. Added a deliberate semantic dark palette, dark launch and system-bar resources, theme-safe custom surfaces across Home, editor, detail, Arrival Mode, attachments, and sharing, plus an on-device contrast regression test for every Material text/container pair.
- Added Phase 3.5 as a non-blocking, evidence-led arrival-resilience phase. It focuses on offline rehearsal, explicit fallback plans, privacy-safe human handoff, faster Android entry points, reliable ticket presentation, and user-initiated contact actions without broadening Travel with Thikana into a live itinerary or tracking service.
- Structured Phase 3.5 into six independently shippable sub-milestones: verified offline arrival, operational Plan B, private handoff, honest ticket presentation, travel-day actions, and a measured faster-entry experiment. Connected their readiness rules and validation explicitly to Phases 4, 5, and 6.
- Connected the Android app to Firebase using the Google Services plugin `4.5.0`, Firebase BoM `34.17.0`, and the main Firebase Analytics module through the central version catalog. This intentionally replaces the earlier no-analytics/no-network posture while keeping all trip content device-local and every arrival feature independent of Firebase availability. Disabled Advertising ID collection and ad-personalization signals and removed advertising-related SDK permissions.

### 21 August 2026

- Added the considered, deferred, and superseded register after reconstructing Phase 1–3 decisions from the Codex session and Claude handoff history. Future candidates now have explicit revisit triggers, promoted ideas point to their owning milestone, and rejected decisions are separated from backlog.

### 3 November 2026 — Journey grouping proposal

- Recorded the multiple-place/multiple-stay proposal and subsequent scope clarification as planned Phase 7; no app implementation is claimed.
- Kept the original offline first-90-minutes purpose and extended it to each arrival, including hotel changes within the same city.
- Chose an overall trip with distinct visits, stays, and optional saved transfers as the recommended domain direction. Concrete persistence and navigation types are to be settled in 7.0.
- Expanded the acceptance model to all four place/stay combinations, including multiple places with one base stay. Visits and stays are independently trip-owned with explicit associations; this supersedes exclusive stay ownership under a stop.
- Distinguished an accommodation's actual location from its role as a base for another place. Returning to an ongoing stay does not repeat check-in; a separate accommodation period at the same property is a new stay.
- Prioritised a simple single-stay flow and complete same-place accommodation-change support before multiple destinations and advanced country context. Destination names supplied in the discussion were illustrative examples, not product requirements.
- Required scoped documents, checklists, readiness, current/next selection, and safe migration in the first working slice.
- Preserved date-only creation, inexact opt-in reminders, existing visual/scroll treatment, and device-local travel content.
- Planned a narrow relaxation of past-date validation for stays already underway; broader historical reconstruction remains outside the first slice.
- Made version 7 database upgrades, version 1 backup conversion, stable legacy IDs, and readable encrypted evidence release gates.
- Kept country-entry preparation and optional precise transfer timing behind later validation; daily itinerary planning, booking, tracking, discovery, and backend services remain non-goals.
- Added planned Phase 8 for explicit text/PDF itinerary conversion, with editable source-backed review before saving. Initially framed as a candidate fast follow after 7.2; the 4 November decision below replaces that sequencing assumption. It does not block journey grouping.
- Kept extraction approach open pending feasibility work; no AI service, remote upload, or universal PDF/OCR support is assumed. Saved imports retain normal scoped offline readiness checks.

### 4 November 2026 — Arrival contract and independent import gates

- Required a persisted Arrival as the operational target for navigation, reminders, widgets/shortcuts, quick access, readiness, and arrival tasks. The earlier uncommitted arrival-persistence choice is superseded.
- Separated arrival lifecycle (`planned`, `cancelled`, `completed`) from offline and Plan B readiness. Removal history/tombstones support honest old-target recovery without retaining deleted travel content or reusing IDs.
- Prohibited uniqueness on `(visitId, stayId, incomingTransferId)`: meaningful returns can share those references and still have distinct arrival IDs.
- Separated stay-owned check-in completion from arrival-owned transport/return preparation. Added ongoing-base return scenarios with no duplicate booking, check-in tasks, or check-in reminder, and no copied readiness.
- Defined place visits by deliberate offline arrival-pack need; ordinary excursions and imported place mentions do not automatically create arrivals.
- Split implementation into 7.0 contracts/prototypes, 7.1A compatibility foundation, 7.1B multi-stay vertical delivery, 7.2 multiple places, and discovery-gated 7.3 extensions.
- Replaced informal percentage-readiness assessments with explicit contract, compatibility, and vertical-slice gates. No gate is claimed complete by this documentation update.
- Recast Phase 8 as an independent reliability/product decision using `ImportCandidate`, with uncertainty per field and relationship. Its fixtures can evaluate the stabilised 7.0 contract early.
- Required predeclared, measured release thresholds for address/association/date errors, correction effort, and abandonment. Text import ships first; PDF is a separate release, with OCR, language translation, and remote AI outside its scope unless separately approved.
- Updated roadmap and compatibility requirements only; app implementation remains pending.
- Audited remaining Phase 7 edges before committing the plan: arrival-owned dates, purpose-specific readiness, overlapping-trip selection, stale-check rejection, distinct Android entry identities, interrupted-operation recovery, consistent backups, and reviewed restoration of removed targets. Added these to 7.0 contracts and delivery acceptance; no extra itinerary capability was introduced.
- Separated evidence of traveller need from architectural soundness. Phase 7.0 now begins with real hotel-change/current-next/return-to-base validation; all four structures remain design compatibility scenarios, not immediate implementation commitments.
- Defined explicit creation semantics and conservative legacy checklist migration: simple saves create the first arrival, drafts may have none, only deliberate actions create additional arrivals, edits preserve identity, and ambiguous existing tasks remain intact on the migrated arrival.
- Added a reliability-baseline gate before migration for Delete/Undo evidence loss, stale ticket-route loading, viewer-safe plaintext-preview cleanup, interrupted restore, and staged filesystem/database recovery. These are code-inspection findings pending reproduction and verification.
- Limited 7.1 implementation to the smallest relationships required by validated accommodation changes. Multi-place UI requires evidence after the vertical slice; migration is not authorised merely because this proposed roadmap is committed.
- Clarified that reliability repairs can proceed independently of traveller validation, while all three 7.0 gates remain prerequisites for migration. Restricted 7.1A to the minimum validated relationships and made traveller-facing return-to-base delivery conditional on evidence while retaining its architectural fixture.

## Maintaining this plan

- Update the decision log whenever product scope or ordering changes.
- Move roadmap items to the completed section only after implementation and verification.
- Add research links as evidence, not as substitutes for direct user validation.
- Record any decision that would introduce a backend, account, network permission, or sensitive identity data before implementation.
