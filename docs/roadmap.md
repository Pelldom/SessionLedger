# SessionLedger Product Roadmap

This roadmap is the authoritative product direction for SessionLedger. It is organized into phases to clarify what exists today, what a Pro unlock includes, and what future work may include.

## Phase 1 — Ad-Supported Free Version (current)

### Core functionality
- Time tracking: Start / Pause / Resume / End (phone and watch control surfaces)
- Sessions:
  - Session list (historical sessions)
  - Session detail (timing edits, with safe navigation prompts)
- Categories:
  - Category list and management
  - Category-level billing defaults (rate, rounding, minimums)
- Global billing defaults:
  - Global rate, rounding, and minimum defaults
- Per-session billing overrides:
  - Session-level overrides for rate, rounding, and minimums
- Watch app controls:
  - Lightweight session controls
  - Category selection sourced from the phone
- Transparent billing calculations:
  - Clear “effective value” display and source labeling (Session / Category / Default)
  - Read-only billing summary visibility on sessions
- CSV-compatible billing engine:
  - Billing and export rules follow the versioned docs and remain consistent with CSV expectations
- Project hygiene:
  - Versioned `CHANGELOG.md` entries per release
  - Documented UI navigation standards (authoritative) in `docs/ui_navigation_standards.md`

### Advertising model (Free)
- Banner ads only
- No interstitials
- No rewarded ads
- No ads on Watch
- No ads during active sessions
- Ads placed only on list-style screens (e.g., Sessions list, Categories list)

### App settings (Free version)
- Use system defaults by default
- Dark Mode:
  - System default
  - Force light
  - Force dark
- Language:
  - System language default
  - Future localization support (non-breaking foundation)
- App version and build info (always visible and consistent)
- “Upgrade to Pro” entry point

## Phase 2 — Pro Version (feature unlock)

Pro is a feature unlock (not just ad removal) and focuses on portability, ownership, and professional outputs.

### Ads
- Removal of all ads

### Import / export capabilities
- Import/export custom timesheets
- Import/export billing data
- Import/export categories

### Branding features
- Business name
- Optional logo
- Branding included in exports (e.g., CSV headers/metadata where appropriate)

### Advanced data portability
- Import categories (from file)
- Export categories (to file)
- Backup / restore workflows (designed for long-term retention and device migration)

## Phase 3 — Polish & Expansion (future-looking, non-committed)

This phase contains candidates for improvement and expansion. Inclusion here does not guarantee delivery order or scope.

### Watch UI polish
- Scrollable category picker
- Haptic feedback for start/pause/resume/end actions
- Improved paused-state indicators
- Further battery optimization (update frequency, wake-up policies, and UI recomposition minimization)

### Reporting
- Per-category totals
- Date-range summaries
- Monthly / weekly views

### Localization
- Language packs
- Region-aware formatting (dates/times, number formats, currency presentation while keeping billing currency rules explicit)

## Architectural principles (authoritative)

- Billing rules are defined in `docs/` and are never duplicated in multiple calculation implementations.
- The watch app remains lightweight and billing-read-only; the phone is the source of truth for billing and persisted data.
- UI navigation standards are authoritative and must be followed (`docs/ui_navigation_standards.md`).
- Versioning follows `v<release>.<feature>.<iteration>` (e.g., `v0.1.8`) and is reflected consistently in-app.
- `CHANGELOG.md` is mandatory for every release; each release must include an entry describing user-visible changes.

