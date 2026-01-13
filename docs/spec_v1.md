# SessionLedger - Spec v1

## Platforms
- Android phone app (Pixel 9a)
- Wear OS watch app + tile (Pixel Watch 3)

## Design Standard
- Phone: Material 3 (Jetpack Compose Material 3)
- Watch: Wear Compose Material 3
- Tile: ProtoLayout Tiles aligned to Material 3 Expressive

## Core Principles
- Phone is the system of record (database, editing, reporting, export).
- Watch is a fast control/display surface.
- Raw time is always stored exactly; billing is derived (rounding/minimums/rates).

## Core Entities
### Category (Job/Client/Project)
- id (UUID)
- name
- archived (bool)
- defaultHourlyRate (optional)
- roundingModeOverride (optional)
- roundingDirectionOverride (optional)
- minBillableMinutes (optional)
- minChargeAmount (optional)

### Session
- id (UUID)
- startTime (Instant)
- endTime (Instant? null if active)
- state (RUNNING/PAUSED/ENDED)
- pausedTotalMs (Long)
- lastStateChangeTime (Instant)
- categoryId (nullable)
- notes (optional)
- overrides:
  - hourlyRateOverride (optional)
  - roundingModeOverride (optional)
  - roundingDirectionOverride (optional)
  - minBillableSecondsOverride (optional)
  - minChargeAmountOverride (optional)
- audit:
  - createdOnDevice (phone/watch)
  - updatedAt (Instant)

### Global Settings
- defaultCurrency (CAD)
- defaultHourlyRate
- defaultRoundingMode
- defaultRoundingDirection
- minBillableSeconds (optional)
- minChargeAmount (optional)
- lastUsedCategoryId (optional)

## Watch v1 (Simple category behavior)
- Watch can start a session as:
  - Uncategorized, OR
  - select from existing categories synced from phone
- Watch does not create/edit categories.
- v1 does not change category mid-session.

## Sync (Data Layer)
- Phone -> Watch:
  - /categories (read-only snapshot)
  - /active_session_state (authoritative session state)
- Watch -> Phone commands:
  - /session/start
  - /session/pause
  - /session/resume
  - /session/end

## Phone Features v1
- Now Tracking: Start/Pause/Resume/End, pick category, show elapsed
- Sessions list + filter (date range, category)
- Edit Session: times, category, notes, overrides
- Categories: add/edit/archive + defaults (rate/rounding/minimums)
- Reports: tracked vs billable vs amount (overall + by category)
- CSV export (see csv schema doc)
