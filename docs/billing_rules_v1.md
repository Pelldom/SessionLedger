# Billing Rules v1

## Precedence
For each attribute:
- Session override > Category > Global

### Rate used
1) Session hourly rate override
2) Category default hourly rate
3) Global default hourly rate

### Rounding used
1) Session rounding override
2) Category rounding override
3) Global rounding

### Minimums used
Minimum time:
1) Session min time
2) Category min time
3) Global min time
Minimum charge:
1) Session min charge
2) Category min charge
3) Global min charge

Minimums do not stack.

## Calculation Order
1) raw = (end - start) - paused
2) rounded = apply rounding (or raw if exact)
3) billable = max(rounded, min_time) if min_time exists
4) cost_pre_min_charge = billable_hours * rate
5) final_cost = max(cost_pre_min_charge, min_charge) if min_charge exists

## Rounding
Modes:
- exact
- six_minute

Direction (six_minute only):
- up
- nearest
- down
