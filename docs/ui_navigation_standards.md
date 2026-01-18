# UI Navigation Standards (Mobile)

This document defines a single, consistent navigation and edit-flow pattern for all mobile Compose screens.

## Core vs non-core screens

Core screens (NO `TopAppBar`):
- Active Session
- Sessions List
- Categories List

Non-core screens (MUST include a `TopAppBar` with a Back arrow):
- Session Details
- Session Billing Overrides
- Category Defaults (Category Detail)
- Global Billing Defaults (Settings)
- Any future screens that are not one of the three core screens

## Top app bar usage

Non-core screens:
- Use Material 3 `TopAppBar`
- Include a Back arrow (`Icons.AutoMirrored.Filled.ArrowBack`)
- Title should describe the screen (e.g. "Session Details")

## Back arrow behavior (standard)

On editable screens, the Back arrow is the ONLY way the user navigates away.

- If there are **no unsaved changes**:
  - Navigate back immediately.

- If there **are unsaved changes**:
  - Show a confirmation dialog:
    - Title: "Save changes?"
    - Buttons:
      - Save
      - Discard
      - Cancel

Button actions:
- **Save**: persist changes, then navigate back.
- **Discard**: discard staged changes, then navigate back.
- **Cancel**: dismiss dialog; remain on screen.

### Save availability in the dialog

- The **Save** button should be enabled only when the staged edits are valid (e.g., no validation error).
- **Discard** must always be available (it never writes).

## Save / Cancel semantics (standard)

Editable screens should use Save/Cancel buttons with these meanings:
- **Save**: commits staged changes but **does not navigate away**.
- **Cancel**: discards staged changes but **does not navigate away**.

Navigation away occurs **only** via the TopAppBar Back arrow.

## Parent/child dirty state rule

Saving in a child screen must not cause the parent screen to appear dirty.

Example:
- Editing billing overrides in "Session Billing Overrides" must not affect whether "Session Details" shows an unsaved-timing prompt.

