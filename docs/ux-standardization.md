# UX standardization

Status: **as-built**. Cannot be “shown” as a kit the way buttons can. Preview has a short motion note only.  
Parked requested UX: [better-to-do](better-to-do.md#ux-standardization).

---

## Navigation motion

Source: `extension/NavTransitions.kt`.

| Event | Current |
|---|---|
| Form / detail **enter** | slide into container **Left** (new screen comes from the **right**), 320 ms FastOutSlowIn |
| Form **exit** | slide out Left |
| **Pop** enter / exit | Right |
| Login → Home | slide **up** + fade (`AppNavigation`) |
| Tab pager | `HorizontalPager` (Log, Fuel) — not the 180 ms fade written in continue.md |
| Duration | `NAV_TRANSITION_MS = 320` |

Hosts using the shared slides: `LogNavHost`, `LogSheetNavHost`, `FuelTabScreen` nested NavHost, `ExpenseNavHost`. Approval list→detail uses its own host.

**Requested (not confirmed):** enter from the **left**, exit to the **right**. That is the opposite of current Android-style push. Confirm in better-to-do before changing.

---

## Scroll and chrome

- Lists: `LazyColumn` + `collapsibleChromeScroll`.
- Scroll down hides page title and bottom bar; scroll up shows them (thresholds 40 / 24 px). Nested connection **observes only** (does not consume deltas).
- Tab switch: pager keeps page; `rememberSaveableStateHolder` keeps tab state in `HomeScreen`.
- Daily Log pagination: load more near last 5 items.
- “iOS-like fling / no jank” is a goal, not a separate animation. It depends on keys, images, nested scroll (see app performance).

---

## Filters

Tall **Filters** card on Daily Log, Logsheet, Expense, Approval: Start date + End date side by side. Offline: date pickers read-only on Daily Log.

**Requested:** replace with filter + sort **icons** in the title row; dialog for range + sort.

---

## Save, loading, clearing

Typical form:

1. Tap Save (`FormPrimaryButton`).
2. Guard: `isSaving` / `isButtonClicked` so double tap does not fire twice.
3. Button shows **dots** (not circle).
4. Success: `AppToast` + navigate back to list (log saved, checkout complete, etc.). Fields are not left filled because the form is popped.
5. Failure: `AppToast` with `save_failed`; stay on form.
6. Offline check-in: still toast “saved”; row shows OFFLINE / SYNCING; `SyncWorker` uploads later.

Login: blank fields → **system** Toast, not `AppToast`. Loading dots on the grey button.

Approval Approve: dots in the outline button; Close disabled while saving.

---

## Network UX

- Header can swap to waiting/connecting (debounced 450 ms) so tab switches do not flash offline.
- `ConnectivityObserver` statuses: Available, Unavailable, Losing, Lost.
- Several ViewModels refetch when status becomes **Available** (Fuel, Expense, DriverLog). Wallet / Request / Approval reconnect was an agreed item — verify each screen when touching UX.
- Offline banner composable exists (red) for “changes will sync”.

---

## Dialogs and sheets

- Exit app: `HandleBackPressWithDialog` on Daily Log.
- Update: `UpdateVersionBottomSheet` (force vs skip).
- Approval: Generate QR dialog (user search + Continue/Cancel), then QR or PIN+signature; Close / Approve.
- Image source: camera / gallery bottom sheet.
- `safeNavigate` debounce (~duplicate route taps).

---

## Photos

Camera permission + compress live in the picker files. List thumbs prefer local file after sync (`preferredListPhoto`). Missing image = grey slot, no “Image Uploaded” text.

---

## Copy / language

EN + MY string resources. `AppToast` itself is not bilingual-structured (callers pass `stringResource`). Message chrome is one grey toast for every tone.
