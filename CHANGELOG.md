# Changelog

All notable changes to SessionLedger will be documented in this file.

## [0.1.0] - 2026-01-12
### Added
- Project planning spec (v1)
- Billing rules spec (v1)
- CSV export spec (v1)
- UX wireflow (v1)
- Step 2 data-layer staging outputs from chat
- Apache-2.0 license instructions

## [0.1.2] - 2026-01-18
### Added
- Category billing defaults editing screen (name + overrides) with explicit Save/Cancel
- Clearer inherited vs override display (shows effective defaults inline)

### Changed
- Category list billing summary simplified (rate + minimum; rounding removed)

### Fixed
- Category detail save/back navigation behavior (no activity finish)

## [0.1.3] - 2026-01-18
### Added
- Read-only billing visibility on Sessions list (final billed amount)
- Read-only Billing Summary section on Session Detail (resolved values + final amount)

### Notes
- No billing behavior changes (uses existing BillingEngine)

## [0.1.4] - 2026-01-18
### Changed
- Improved discoverability of editable Session Detail fields with subtle trailing Material affordances

## [0.1.5] - 2026-01-18
### Added
- Session-level billing overrides (rate, rounding, minimum) with live BillingEngine recalculation
