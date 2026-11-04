# Phase 7 validation and acceptance fixtures

Started 4 November 2026. This is a working research protocol and fixture register.
It does not authorise migration or declare any Phase 7.0 gate passed.

## Evidence register

| ID | Source | Evidence type | Interpretation |
| --- | --- | --- | --- |
| TOKYO-CHANGEOVER | Synthetic scenario generated during planning on 4 November 2026 at the product owner’s request. | Engineering fixture; no participant interview, actual artefacts, or observed retrieval | Defines a testable same-place accommodation-change hypothesis. It is not traveller evidence and does not establish frequency or prove grouping is better. |

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
- Viewing-preview lifetime is repaired and verified in the second delivery below.
- Interrupted restore is repaired and verified in the third delivery below.
- Current single-owner import/deletion recovery and coordinated backup snapshots
  are repaired and verified in the fourth delivery below. Shared-association
  unlinking, the policy for older unclaimed files, and final combined verification
  remain outstanding; 7.0C stays open.
- The reminder-cancellation review below closes the durable deletion-to-cancellation
  gap without changing those remaining gates.

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

## Viewing-preview reliability delivery — 4 November 2026

- Decrypted copies now have unique session filenames. Thumbnail preparation cannot
  replace or remove a copy being presented. Leaving the in-app presenter,
  backgrounding, authentication expiry, and session lock release internal copies.
  A preparation completing after its session ended is discarded.
- Startup and foreground recovery remove abandoned plaintext copies and interrupted
  temporary writes. Original encrypted attachments and encrypted thumbnails are
  preserved.
- Before opening an external viewer, an atomic journal records its preview URI and
  eligible recipient UIDs, including the Android resolver. Cleanup checks Android's
  actual read grants and preserves copies while their granted activity remains
  active, including across Thikana process death. After the grant ends, cleanup
  runs on foreground entry or a five-second retry while Thikana is running.
  If its process is absent, recovery runs at the next startup.
- PDF renderer and descriptor disposal now coordinate with rendering and close
  resources when preparation is cancelled, the screen exits, or it backgrounds.
- Automated verification on the Pixel 10 Pro API 37 emulator: seven preview-session
  tests and two encrypted-store tests passed; five presenter lifecycle tests,
  four route tests, and one accessibility/window test passed (19 total).
  Session tests cover unique copies, lock, interrupted preparation, restart,
  simulated active grants, failed launches, and malformed journal recovery.
  Presenter tests include a generated PDF and background/foreground recreation.
- All 53 existing model/feature unit tests passed. The debug build and app lint
  passed; app lint reported zero errors and 16 dependency/version warnings.
- Manual rehearsal used a separate `Preview-Verification` trip and synthetic PNG.
  Android's Photos app rendered it; returning to Thikana removed its plaintext
  copy and journal. The resolver chooser also retained the copy while open.
- A test-only viewer in a separate app UID reopened the URI successfully after
  `am kill` terminated Thikana's background process. The FileProvider restarted
  Thikana (PID 9744 became 16803), and the viewer read all 511 bytes again.
  Finishing that viewer removed both the copy and journal.
- Arrival Mode's in-app presenter opened the same image using one internal copy
  without an external journal; Back navigation left the preview directory empty.
- Removed the temporary trip, its encrypted files, its synthetic Downloads file,
  and the test-only viewer APK. The existing Kyoto trip remained present.
  The viewer fixture exists only in instrumentation sources, outside the shipped app.
- This closes the preview-lifetime item, not the reliability gate. Interrupted
  backup restore and staged filesystem/database failure recovery are still pending.

## Interrupted-restore reliability delivery — 4 November 2026

- Replaced in-memory-only rollback with a durable atomic restore journal outside
  cache. It records only newly owned trip/template IDs, the protection preference,
  and commit state; it contains no addresses, booking references, or password.
- Validate the complete decrypted backup before any restore mutation. Refuse
  occupied document directories and existing document IDs. Insert trips and
  checklist items with conflict-abort semantics so a colliding item ID cannot
  replace an existing traveller's task.
- Write and sync encrypted documents before publishing all new trips, checklist
  items, templates, and attachment metadata in one Room transaction. Hold the
  database writer during preparation to keep the ownership check and publication
  consistent; existing readers continue to see the previously committed state.
- Before the durable journal commit, restart rolls back only this restore's new
  records and document directories. This includes termination after the Room
  transaction committed but before the journal commit. After the durable journal
  commit, recovery retains the restored content and finishes preference/cleanup.
  Retain the journal until cleanup succeeds; repeated recovery is idempotent.
- Ordinary exceptions and cancellation use the same recovery protocol, with
  non-cancellable rollback. A process death leaves the journal for startup.
  Startup recovery runs before ordinary repositories expose restored content.
  Recovery and retry run in an application-owned IO coroutine; Application startup
  does not wait for recursive cleanup. The UI distinguishes loading from failure,
  and consumes a pending shortcut/share only after readiness becomes true.
  Failed recovery blocks their reads/mutations and shows a retry screen. An
  unreadable journal is preserved rather than guessed away; retry cannot repair
  genuine journal corruption without repairing the underlying state.
- Startup reconciles reminders from persisted trips, covering the gap between a
  committed restore and its ViewModel scheduling alarms. Restored reminder flags
  are checked by the fixture; physical alarm delivery was not rehearsed here.
- Backup plaintext work now lives in a dedicated temporary directory and is
  removed on startup/retry. Cleanup also handles abandoned payload files from the
  previous implementation.
- Fixed an additional multi-document defect: closing one limited document stream
  previously closed the whole payload. The payload reader now retains stream
  ownership so subsequent documents can be restored.
- Constrain record IDs to 1–64 ASCII letters, numbers, underscores, hyphens, or
  periods, excluding `.` and `..`. Backup parsing bounds ID byte lengths before
  allocation; journal creation validates all IDs before writing. Journal reads
  validate every ID before recovery mutates any target. Control characters, NUL,
  Unicode, path separators, and oversized IDs are rejected.
- Room remains version 7, backup format remains version 1, and the encrypted
  attachment format is unchanged. No Phase 7 journey-model migration is introduced.
- Automated verification after review fixes: all 26 core-data instrumentation tests
  passed on the Pixel 10 Pro API 37 emulator. This includes twelve recovery tests,
  three backup
  round-trip/password/tampering tests, and the existing preview/store/migration
  tests. All 53 model/feature unit tests passed; debug build and app lint passed
  with zero errors and 16 dependency/version warnings.
- The on-disk recovery fixture contains an existing journey/document, a new
  journey with two documents, and a template. It checks all seven restore
  checkpoints, restart/retry, atomic-journal fallback, cancellation, conflicting
  task IDs, occupied file paths, interrupted filesystem cleanup, malformed state,
  abandoned plaintext, and blocked access until recovery succeeds.
- Real process-death rehearsal used a test-only APK with isolated synthetic
  databases and files. Its process terminated at each of: journal creation,
  first encrypted document, trip insertion, all metadata written, Room commit,
  durable journal commit, and preference application. Restart produced the
  expected rollback/retention in all seven cases; retry was safe, both restored
  documents decrypted correctly, and the existing journey/document remained intact.
  These are actual process terminations, separate from the automated fault hooks.
- Installed the updated production debug app while preserving existing data.
  Seeded only a synthetic uncommitted journal and owned test files; application
  startup removed the journal, document directory, and plaintext work directory.
  This checks startup integration; partial metadata rollback is exercised by the
  separate process-death fixture.
- A synthetic unreadable journal displayed the recovery screen. Inspected its
  layout and tapped retry: access remained blocked while the fault persisted.
  Removed only that synthetic journal, then tapped retry again; Home returned
  with the existing Kyoto trip intact.
- Rehearsed asynchronous cold startup with 60,000 empty synthetic document files.
  The activity displayed in 828 ms while cleanup was still pending; a repeat
  displayed in 657 ms and allowed visual inspection of the loading screen.
  These are single-emulator observations, not a performance guarantee or a
  large-payload benchmark. After readiness, the waiting shortcut opened its
  expected missing-trip screen without replaying when returning Home.
- Installed the final review build and repeated failed recovery with a pending
  shortcut. After removing only the verified synthetic corrupt journal, the
  application-owned retry completed, consumed the shortcut, and returned to Home
  with Kyoto intact. Confirmed synthetic journals and work directories were gone.
- Removed all synthetic production recovery state, the UI dump, and the test-only
  probe APK. This rehearsal did not perform a full file-picker-to-restore journey
  in the production UI or emulate device power loss/storage hardware failure.
- The interrupted-restore item is complete. General attachment import/unlink/
  deletion recovery, shared-file ownership, snapshot consistency, and final
  combined verification still belong to the remaining 7.0C work.

## Staged-document operation delivery — 4 November 2026

- Added an atomic document-operation journal outside cache, coordinated with the
  existing backup recovery lock and startup access gate. It contains an operation
  kind and bounded trip/document IDs; no addresses, file contents, or credentials.
  All repositories require the shared coordinator, so alternate construction
  cannot silently fall back to the old file-first deletion path.
- Before import writes an encrypted original or thumbnail, verify the parent trip
  exists, the document ID is unused, and the destination files are unoccupied;
  then record durable import intent. Attachment metadata is the publication
  boundary. Restart removes an unpublished import's owned original, thumbnail,
  and temporary files; a published import is retained. Never delete another
  owner's metadata or files when a recovery record has conflicting ownership.
- Confirmed document/trip deletion records durable intent before mutation.
  Remove metadata first, using a Room transaction and foreign-key cascades for
  trip removal; then remove encrypted files and finish the journal. This gives
  restart/retry a deterministic completion path. Once intent is durable,
  cancellation or interruption completes the confirmed removal rather than
  promising Undo.
- File removal now checks failures rather than ignoring `File.delete()` results.
  A failed cleanup retains the journal, blocks repository access/ID reuse, and
  offers recovery retry. Ordinary failure and cancellation attempt cleanup in a
  non-cancellable coroutine. Unreadable or oversized journals remain preserved
  and block access. Trip-removal errors are handled in the ViewModel instead of
  escaping its coroutine.
- All repository mutations and document file access share the backup-operation
  lock. Export reads trips/tasks, templates, and attachment metadata in one Room
  transaction and retains the lock until the encrypted backup is written. A
  concurrent edit or deletion therefore cannot invalidate the captured document
  set. This serialises writes/file access during export; it is a consistency
  tradeoff, not a background-export performance claim.
- Room remains version 7, backup format version 1, and encrypted-document format
  unchanged. Current metadata has one trip owner per document ID and derives
  its file path from that owner. No shared association schema or Phase 7 journey
  migration is introduced.
- Automated verification: the full 37-case core-data emulator run passed, followed
  by all 12 staged-operation tests after adding explicit ownership-conflict
  coverage (38 distinct core-data cases across the runs). This exercised on-disk
  reopen/idempotent recovery at all import and deletion boundaries, cancellation
  before/after publication, permission-denied file removal and retry, interrupted
  atomic-journal fallback, missing-parent/publication failures, owned temporary
  cleanup, malformed/oversized records, cross-owner refusal, and backup export
  while concurrent edit/deletion waited. The resulting backup restored the
  captured tasks and decrypted documents correctly.
- All 53 model/feature unit tests passed. The final debug build and app lint
  passed with zero errors and 16 dependency/version warnings. `git diff --check`
  passed.
- Actual process-termination rehearsal used the isolated test APK. Import passed
  five boundaries: journal, encrypted document, metadata publication, settled
  files, and journal cleanup. Individual document removal, clearing all trip
  documents, and trip deletion each passed four boundaries: journal, metadata
  removal, settled files, and journal cleanup. All 17 restarts/retries preserved
  the other synthetic trip/document; document removal also preserved the second
  document and its quick-access flag. Trip-deletion cases successfully restored
  both documents from the pre-deletion backup.
- Installed the final production debug app without clearing its data. Through the
  UI, created a temporary “Recovery rehearsal” trip, imported a valid synthetic
  PDF via Android's file picker, removed that document through confirmation, and
  imported it again. Scoped a filesystem permission failure to that trip's new
  encrypted directory, then confirmed permanent trip deletion. The app displayed
  recovery UI without crashing, and the retained journal referenced only the
  synthetic trip. Restoring its permissions and tapping retry completed deletion
  and returned Home with Kyoto intact. Inspected the confirmation/recovery/Home
  states; no unrelated traveller content was edited.
- Verified the temporary trip directory, operation/restore journals, and backup
  work were gone and the pre-existing encrypted directories were unchanged.
  Removed the downloaded synthetic PDF, UI dump, and isolated test APK.
- Coverage limits: these tests simulate process termination and permission
  failures, not device power loss or failing storage hardware. Orphan cleanup is
  limited to files owned by a durable journal; older unclaimed directories remain
  preserved, and restore still refuses to overwrite them. A policy for those
  files remains open. Cross-owner refusal and single-owner isolation do not prove
  shared-file unlinking; its association/last-reference rules and fixtures still
  need agreement before shared persistence is introduced. Final combined
  reliability verification is also pending. Phase 7.0C remains open.

## Durable reminder cancellation review — 4 November 2026

- Confirmed the gap in the fourth delivery: deleting the trip and then calling
  the reminder scheduler from the ViewModel was not durable. Startup's live-trip
  scan could not identify the deleted ID, so its alarm/notification could remain.
- Added an atomic cancellation queue independent of Room trip rows. Trip deletion
  first commits metadata removal, then durably queues the ID before completing
  file cleanup and clearing the deletion journal. Termination between metadata
  removal and queue persistence leaves that deletion journal to replay enqueue.
  The queue retains only the opaque trip ID and a request token, excludes travel
  content, and is not exported in travel backups.
- Cancellation performs both the alarm/PendingIntent cancellation and notification
  cancellation before durable acknowledgement. Partial failure or termination
  leaves requests to replay idempotently. A batch uses one acknowledgement write;
  token matching preserves requests added during processing and prevents a stale
  acknowledgement from erasing a later deletion of the same restored ID. The
  durable empty queue removes completed IDs instead of retaining removal history.
- Application startup/recovery and boot/package/timezone reconciliation drain the
  queue before reading live trips to schedule. Scheduler operations are serialised
  on IO, and ordinary sync/show re-read persisted trip state so stale callers
  cannot re-arm or show a deleted/disabled trip. Restoring the same ID drains its
  old cancellation before scheduling the restored trip's enabled reminder.
- Coverage applies to deletions performed or recovered by this workflow. A
  reminder scheduled by older code is cancelled by its existing identity when
  that trip is deleted through the new workflow; a registry of all scheduled
  alarms is not required. This does not claim to discover already-deleted,
  untracked identities from before the fix.
- Automated verification: the full core-data run passed 43 tests and the feature
  run passed 34 tests. After batching acknowledgement and adding its concurrent
  enqueue case, the final five queue tests and five real-reminder recovery tests
  passed separately (78 distinct emulator cases across runs). All 53 unit tests,
  the final debug build, and app lint passed, with zero errors and 16
  dependency/version warnings. `git diff --check` passed.
- Tests used isolated disk databases, the real scheduler, Android AlarmManager
  PendingIntents, and posted synthetic notifications. They covered reopen after
  completed deletion, the database-to-queue gap, interruption after alarm or
  notification cancellation and acknowledgement, notification-service failure,
  stale sync/show, and restoring the same ID. An unrelated trip's alarm and
  notification remained available throughout recovery.
- Actual process termination/restart passed five checkpoints: database deletion
  before enqueue, completed deletion before scheduler cancellation, alarm
  cancelled, notification cancelled, and queue acknowledged. The deleted-before-
  cancellation rehearsal independently inspected Android's alarm/notification
  state after termination: both synthetic trip reminders remained. Startup
  recovery then removed the deleted trip's alarm and notification, retained the
  other reminder, and emptied the queue. Test effects were cleaned after their
  preservation had been verified.
- These are cancellation/recovery checks, not a timed notification-delivery test
  or a simulation of device power loss. Room stays version 7 and travel backup
  stays version 1. The remaining 7.0C work and its gate status are unchanged.
- Removed the isolated test APK and its synthetic OS effects. Installed the final
  production debug build without clearing data and confirmed Home with Kyoto
  intact. Removed the verification UI dumps.

## Verification practice for subsequent deliveries

Run relevant emulator tests for each Phase 7 repair slice. For changed user flows,
also install the updated app and rehearse the flow directly on the emulator.
Use separate synthetic trips, preserve existing traveller data, inspect the
resulting layout, and record automated and manual coverage separately.
