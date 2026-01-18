# UX Wireflow v1

## Phone Screens
1) Now Tracking
- Start / Pause / Resume / End
- Category selector
- Live elapsed
- Quick link: Sessions, Reports, Settings

Note: For edit screens and navigation behavior standards, see `docs/ui_navigation_standards.md`.

2) Sessions
- List with filters (date range, category)
- Tap -> Session Detail

3) Session Detail / Edit
- Start/end times
- Paused time (simple input)
- Category, notes
- Overrides: rate, rounding, minimums

4) Categories (Jobs)
- List + add
- Edit: name, archive
- Defaults: rate, rounding, minimums

5) Reports
- Overall totals: tracked vs billable vs amount
- Totals by category
- Date range selection

6) Settings
- Default currency (CAD)
- Global defaults: rate, rounding, minimums
- Export options

7) Export
- Date range + category filter + include notes toggle
- Generate CSV and share/save

## Watch
A) Tile (primary entry)
- STOPPED: Start (last used / uncategorized) + Job (open picker)
- RUNNING: elapsed + Pause + End
- PAUSED: elapsed + Resume + End

B) Watch App
1) Session Control screen
- Start/Pause/Resume/End
- Display job + start time + elapsed

2) Pick Job screen
- Uncategorized
- Category list (synced, non-archived)
- Selecting a job starts a session immediately (v1-simple)
