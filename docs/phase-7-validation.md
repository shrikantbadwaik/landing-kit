# Phase 7 validation and acceptance fixtures

Started 4 November 2026. This is a working research protocol and fixture register.
It does not authorise migration or declare any Phase 7.0 gate passed.

## Evidence register

| ID | Source | Evidence type | Interpretation |
| --- | --- | --- | --- |
| TOKYO-CHANGEOVER | Scenario supplied by Shrikant on 4 November 2026 | Concrete scenario; no participant interview, actual artefacts, or observed retrieval yet | Defines a testable same-place accommodation-change hypothesis. Does not establish frequency or prove grouping is better. |

No traveller interviews or comparative retrieval sessions have been recorded yet.
Keep participant observations separate from proposed requirements and simulated fixtures.

## TOKYO-CHANGEOVER acceptance fixture

A six-night Tokyo trip has one place visit and two distinct stays:

- Shinjuku: nights 1–3.
- Asakusa: nights 4–6.

Each stay has its own Japanese address, booking reference, confirmation PDF,
check-in/check-out times, and luggage instructions. The second has a late-arrival
contact. Movement instructions connect the hotels without replacing either
hotel's address. Use synthetic addresses and documents in engineering fixtures;
never infer missing travel details from this scenario.

On changeover day, luggage pickup at Shinjuku remains useful while Asakusa is the
next check-in target. Dates alone cannot prove physical location or completed
check-out. Explicit traveller selection must take precedence over suggestions.

| Task | Required outcome |
| --- | --- |
| Pick up luggage at the first hotel | Retrieve Shinjuku's address and luggage instructions without changing the next check-in target. |
| Travel to the second hotel | Retrieve Asakusa's Japanese address and movement instructions; do not substitute the current hotel's address. |
| Show the second hotel's confirmation | Open only Asakusa's associated confirmation from its arrival entry. A missing target produces recovery, never another booking. |
| Open the second check-in reminder | Resolve its stable arrival ID even after stays are edited or reordered. |
| Prepare the second check-in | Its check-in tasks start incomplete; Shinjuku's completed check-in remains completed. |
| Revisit the first hotel's information | Preserve its booking and completion history while keeping the second arrival independently accessible. |
| Remove a targeted document or arrival | Old links provide honest recovery; they do not load forever or silently retarget. |
| Retrieve without connectivity | Address and confirmation remain usable within the roadmap's two-tap goal from the relevant arrival entry. |

Distinct arrivals reference the two stays. The fixture does not require multiple
place records or a country hierarchy. Exact reminder timezone and task ownership
contracts remain to be reviewed in 7.0B.

## Research sessions for 7.0A

Start with a small discovery round of actual same-place hotel-change travellers
and single-stay travellers. Seek repeated observations across independent journeys;
sample size alone is not a gate. Expand research if behaviour or needs disagree.
Research participation and access to private bookings must be voluntary.

1. Ask participants to walk through a recent or upcoming real accommodation change.
   Record the actual information needed, storage locations, retrieval workarounds,
   and observed confusion. Do not first pitch the grouping feature.
2. With their permission, inspect redacted notes/screenshots or let them demonstrate
   locally. Do not collect booking references, addresses, or PDFs unnecessarily.
3. Compare the existing separate-card approach with a simple grouped-arrival
   prototype using equivalent information. Alternate task order between participants
   to reduce learning effects. Record prototype limitations separately.
4. Rehearse luggage pickup, next-hotel navigation, confirmation presentation, and
   reminder targeting. Include a missing-document recovery task.
5. Run equivalent creation/retrieval tasks with single-stay travellers to detect
   additional steps, terminology, or organisational burden.
6. Ask whether the benefit is preparing for arrivals or managing daily activities.
   Study return-to-base and multi-place needs separately; Tokyo does not prove them.

For every session record: participant pseudonym, actual journey type, evidence
source, task, chosen target, wrong-target incidents, taps, retrieval time,
preparation time/corrections, assistance, and qualitative explanation. Record
missing data as missing, not success. Report results per participant and task;
do not substitute a confidence percentage for evidence.

### Decision rules fixed before evaluation

- Any wrong address, confirmation, or arrival target is a failure: correct and retest.
- Correct offline address/confirmation retrieval must meet the two-tap goal from
  the relevant arrival entry.
- Single-stay preparation must introduce no additional organisational step.
- Grouped arrivals must show repeated benefit in correct selection or preparation/
  retrieval effort compared with separate cards. Retain raw paired results and
  counterexamples; a preference vote alone does not pass the gate.
- Unobserved return-to-base or multi-place needs remain hypotheses.
- If the grouped approach fails these rules, narrow or stop before migration.

## 7.0 progress

- 7.0A: scenario and protocol prepared; observations and comparative evaluation pending.
- 7.0B: Tokyo fixture prepared; behavioural contract and prototypes pending review.
- 7.0C: first repair slice removes misleading Delete/Undo and handles missing ticket
  targets. Explicit route document IDs now take precedence over pinned documents.
- Viewing-preview lifetime, interrupted restore, and staged file/database recovery
  remain outstanding. Neither this document nor the first repair slice closes 7.0C.

## First reliability delivery — 4 November 2026

- Removed the snackbar Undo and its structured-trip-only restore method. The
  permanent-delete confirmation explicitly includes saved document copies.
  This corrects the promise; it does not add trash or solve filesystem/database
  failure recovery.
- Ticket routes distinguish pending data from a completed empty result, recover
  when a trip/document is missing, and do not prepare another document for a
  missing target. Removing the routed document while viewing also shows recovery.
- An available explicit document target opens ahead of a different pinned
  document. Pinning still determines the gallery order.
- Verification: 16 model unit tests and 37 feature unit tests passed; debug app
  build passed; feature lint completed with zero errors and 13 warnings.
- Five focused instrumentation tests passed on the Pixel 10 Pro API 37 emulator:
  pending-to-empty recovery, missing target with another pinned document,
  removal while presenting, missing trip before attachment emission, and explicit
  target/gallery navigation/window restoration.
- These tests exercise the production route content and presenter. They do not
  constitute a complete notification-to-database end-to-end journey.
- Manual app rehearsal on the same emulator: installed the updated debug APK while
  retaining existing data; created a temporary `Thikana-Verification` trip; imported
  a synthetic PNG through Android's file picker; pinned it; opened it from Arrival
  Mode; verified rendering and Back navigation.
- Inspected the revised permanent-delete confirmation and deletion snackbar.
  The confirmation included document copies, and the snackbar had no Undo.
  The temporary trip's encrypted image and thumbnail existed before deletion and
  were absent afterward. The existing Kyoto trip remained present.
- Reopened the deleted temporary trip through its old Arrival Mode intent. It
  showed “This trip isn’t on this device.”; “Back to my arrivals” returned to Home.
  This rehearses stale trip entry recovery. Stale ticket-specific states and
  explicit-target precedence were covered by the instrumentation tests above.
- Removed the temporary trip and its synthetic Downloads file after the rehearsal.
  Interruption/failure testing and the remaining reliability repairs are still
  required before closing 7.0C.

## Verification practice for subsequent deliveries

Run relevant emulator tests for each Phase 7 repair slice. For changed user flows,
also install the updated app and rehearse the flow directly on the emulator.
Use separate synthetic trips, preserve existing traveller data, inspect the
resulting layout, and record automated and manual coverage separately.
