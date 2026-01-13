# SessionLedger

Watch-first, billable-aware time tracking for Android and Wear OS.

SessionLedger is designed for professionals who want a fast "start/stop" workflow from a Wear OS tile, with billing rules that match real client work: categories/jobs, hourly rates, 6-minute rounding, and minimum billable time/charge.

## Key Features (v1)
### Wear OS (Pixel Watch)
- Watch tile: Start / Pause / Resume / End
- Big elapsed timer (tile-first workflow)
- Optional job selection: Uncategorized or pick from existing phone-defined categories
- Watch app screens:
  - Session control screen
  - Pick Job screen (read-only list synced from phone)

### Phone (Android)
- Start / Pause / Resume / End (fully functional without a watch)
- Sessions list + session editor
- Categories (jobs/clients/projects): create/edit/archive
- Hourly rate: global default + category default + per-session override
- Rounding: Exact or 6-minute increments (Up/Nearest/Down), global/category/session overrides
- Minimums: minimum billable time and/or minimum charge, global/category/session overrides
- Reports: totals overall + by category (tracked vs billable vs amount)
- Export to CSV

## Billing Model (high level)
- Store raw tracked time exactly.
- Compute billable time from: rounding, then minimum time floor, then minimum charge floor.
- Precedence for rate/rounding/minimums: Session override > Category > Global.

See docs:
- `docs/spec_v1.md`
- `docs/billing_rules_v1.md`
- `docs/csv_schema_v1.md`
- `docs/ux_wireflow.md`
- `docs/chat_outputs_step2.md` (staging code blocks from this chat)

## Repository Layout
- `docs/` Planning specifications and UX flows
- `android/` Android Studio project (mobile + wear modules) (to be created)

## License
Intended license: Apache License, Version 2.0. See `LICENSE_INSTRUCTIONS.md` to add the canonical license text.

## Publisher / Author
PellDomPress

## Status
Planning complete. Implementation begins with data layer (Room + settings + billing engine + CSV export), then UI, then Wear tile.
