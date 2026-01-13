# CSV Schema v1

## Defaults
- Export ENDED sessions only
- Currency default: CAD
- Money values: 2 decimals
- Date/time: local ISO "YYYY-MM-DD HH:MM:SS"
- UTF-8, comma delimited

## Columns (in order)

Identity and classification
1) session_id
2) category_id
3) category_name
4) notes

Timing
5) start_local
6) end_local
7) state
8) paused_seconds
9) tracked_seconds

Rate + rounding + minimums used
10) rate_used_per_hour
11) rate_source (session|category|global)

12) rounding_mode_used (exact|six_minute)
13) rounding_direction_used (up|nearest|down|none)
14) rounding_source (session|category|global|none)

15) min_time_seconds_used
16) min_time_source (session|category|global|none)
17) min_charge_used
18) min_charge_source (session|category|global|none)

Billable results
19) rounded_seconds
20) billable_seconds
21) cost_pre_min_charge
22) final_cost
23) currency

Audit / debug
24) created_on_device (phone|watch)
25) updated_at_local
