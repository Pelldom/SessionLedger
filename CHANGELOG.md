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

## [0.1.6] - 2026-01-18
### Fixed
- Session Detail Save button state now reflects only unsaved timing edits (billing overrides no longer affect it)

## [0.1.7] - 2026-01-18
### Changed
- Improved Session Detail navigation with a back arrow and Save/Discard prompt when timing edits are unsaved

## [0.1.8] - 2026-01-18
### Changed
- Unified navigation behavior across edit screens (Back arrow prompts to Save/Discard/Cancel when changes are unsaved)
- Standardized Save/Cancel semantics so they do not navigate (navigation occurs via Back only)

## [0.1.9] - 2026-01-18
### Changed
- Simplified Session Detail editing UX with three entry points (Timing, Category, Billing)
- Consolidated timing edits into a dedicated timing edit screen; Billing Summary is now fully read-only on Session Details

## [0.2.0] - 2026-01-18
### Added
- CSV export UI for sessions (date range + category filter)
- Exports are saved to Downloads/SessionLedger and can be shared optionally

## [0.2.1] - 2026-01-19
### Changed
- Watch UX polish: improved control button layout and prevented label wrapping
- Watch hourly haptic reminders while a session is running

## [0.2.2] - 2026-01-19
### Added
- App Settings screen (appearance, system defaults, app info, Pro placeholder)
### Changed
- Theme selection with immediate apply (System / Light / Dark)

## [0.2.3] - 2026-01-19
### Added
- Session archiving (hidden from default list; excluded from exports)
- Session restore (unarchive)
- Permanent delete with confirmation
- Sessions list filter: Active / Archived
