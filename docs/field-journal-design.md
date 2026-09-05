# Traveler's Field Journal

The November 2026 redesign covers the entire app. The visual system uses warm
paper, forest ink, amber, and sage, with the supplied travel illustrations as
illustrated covers and quiet background accents.

## Shared system

- Fraunces headings and Manrope body text. Smaller display sizes give practical
  travel details more space; body text uses medium weight.
- Semantic Material colors cover light and dark modes, including dialogs,
  sheets, date pickers, inputs, and their surface containers.
- Card and button shapes come from `MaterialTheme.shapes`. Journal cards use
  restrained corners and elevation. The detail panel retains its rounded top,
  4 dp elevation, centered handle, and layered scrolling.
- The app launcher uses the extracted ticket doodle in forest ink on warm
  paper, sized inside the adaptive icon safe area. Android 13+ themed icons use
  the same outline. `scripts/prepare_launcher_icon.py` regenerates its vector.
- A ruled header with a mountain-and-sun mark connects the secondary screens.
  Header badges and page footers are removed throughout; list items and spacing
  formerly occupied by footer copy are removed as well. The print preview and
  generated PDF also omit their footer copy.
- Individual travel doodles are scattered as faint forest-ink watermarks, two
  on shorter pages and three on longer pages. Stable page/section seeds vary
  assets, size, tilt, and side without reshuffling during recomposition.
- Artwork is draw-only behind content, including inside opaque paper cards.
  It adds no lazy items, spacers, touch targets, or accessibility announcements.
  Essential information uses text. The original cover and header art remains.
- Scattered list backgrounds read measured item geometry during drawing; card
  backgrounds travel with their existing containers. Dragging, flinging,
  animated scrolling, and entrance transitions retain their existing behavior.
  Rotated motifs fit inside their anchors so lazy culling cannot cut them short.
- Primary actions can grow vertically with text scaling.

## Screen coverage

| Screen | Treatment |
| --- | --- |
| Home | Illustrated sunrise cover, mountain brand mark, forest arrival cards and amber readiness labels |
| Trip detail | Paper destination poster with mountain illustration; raised details panel; suitcase accent for travel tools |
| Create/edit | Three scattered section backgrounds, numbered paper sections, ruled inputs and semantic primary save action |
| Offline check | Three scroll-linked doodles behind preparation and readiness checks; joined status and checklist pages |
| Arrival Mode | Quiet ruled header, forest address card, three scroll-linked background doodles |
| Handoff setup / active | Two scattered background doodles in setup; forest presentation with warm accents in active mode |
| Share summary / attachment sheet | Scattered card backgrounds, selectable journal cards and themed sheet controls |
| Print preview / PDF | Three doodles behind the paper preview, paper document, forest destination block, amber accents; PDF uses the same pigments |
| Backup | Three scroll-linked doodles, paper panels, sage accents and themed password fields |
| Ticket presentation | Forest controls and dark journal canvas; document rendering, zoom and brightness behavior preserved |
| Loading / missing / empty states | Mountain or traveler artwork, themed text and controls |

## Artwork provenance and preparation

All artwork comes from the SVGs supplied by the user. Originals remain untouched.
`scripts/prepare_journal_art.py` uses local `rsvg-convert` and ImageMagick to
create compact lossless WebP resources.
`scripts/extract_journal_doodles.py` additionally isolates the 12 pictorial groups
from 64372227 alongside the existing suitcase: glasses, plane, globe, passport,
camera, pin, map, ticket, photos, signpost, compass, and lifebuoy. Lettering and
texture groups are excluded. All 13 editable SVG elements, with tight viewboxes,
are retained in `assets/journal-doodles/`. The 12 extra Android resources total
approximately 194 KiB. It removes backing rectangles, selects
individual illustrations, omits stock captions, and adapts silhouette colors.
Mountains and hikers have light-colored silhouettes for dark surfaces.

| Resource | Supplied VectorStock ID |
| --- | --- |
| `journal_mountains` | 64195475 |
| `journal_sunrise` | 100858 |
| `journal_travelers` | 22384057 |
| `journal_hikers` | 37134008 |
| `journal_vehicle` | 53476033 |
| `journal_rest` | 63707063 |
| `journal_footsteps` | 50228935 |
| `journal_doodles` (suitcase crop) | 64372227 |

The preexisting staged VectorDrawable for 64372227 is preserved separately.
No network assets or runtime SVG dependencies are introduced.

## Verification

- Debug APK builds successfully.
- The ticket launcher was reviewed at its actual size in the emulator app drawer.
- The doodle motion regression check verifies rendered movement during animated
  scrolling, identical placement after recomposition, and unchanged item geometry.
- 37 feature unit tests pass.
- 18 emulator tests pass, including semantic contrast, arrival actions,
  handoff privacy, attachment controls, ticket presentation, and PDF output.
- Doodle visual review checks Home, editing before/after a fling, and the
  detail panel in dark mode. Existing layout spacing and panel behavior remain.
- Visual review covers Home, detail, editing, Arrival Mode, offline checks,
  sharing, print preview, backup, and dark mode.
- The detail poster and scrolling panel were checked with 200% text in portrait
  and landscape; the arrival action remains reachable.
- Device tests run directly through `adb shell am instrument` because Gradle's
  connected-test runner needs a dependency unavailable in the offline cache.
- Local review screenshots are saved in the ignored `.focus/journal-review/`
  folder. The emulator is restored to normal text size, light mode, and unlocked
  portrait rotation.
