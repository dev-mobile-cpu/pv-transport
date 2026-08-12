# PV Transport — To Do

## Task 1 — Logsheet tab + Create button
- [x] Disable **Logsheet** tab for now (`ENABLE_LOG_SHEET_TAB = false` in `LogTabScreen.kt`).
- [x] Remove FAB create buttons from `LogScreen` / `LogSheetScreen`.
- [x] Move **Create** button to Daily Log main header (right of title).
- [x] Create button navigates by current tab (Log → checkin, Logsheet → add_log_sheet).
- [ ] Verify in Android Studio (sync/run).

**Re-enable Logsheet later:** set `ENABLE_LOG_SHEET_TAB = true` in `LogTabScreen.kt`.

## Task 2 — Full Offline (Add Log local-first)
- [x] Always save Add Log to local first (online / offline / poor network).
- [x] Auto sync when network is Available (WorkManager + connectivity observer).
- [x] Status flow: **OFFLINE** → **SYNCING** → API status (e.g. PENDING).
- [x] On upload success: remove from local; on fail: keep local + retry.
- [x] After sync success: refresh list so card shows real API status.
- [x] Same local-first path for Checkout (don't lose checkout data on poor network).
- [ ] Verify in Android Studio (poor network + offline + online).

## Task 3 — UI/UX standardization (Add / Create buttons)
### Shared style (source of truth)
- File: `ui/theme/AddActionButton.kt`
- Corner: **12.dp**
- Border + text + icon: **`#169A5A`**
- Soft background: **`#169A5A1A`**
- Visual: `+` icon + label (e.g. Add Log)

### Apply now
- [x] Daily Log header create button → shared `AddActionButton` (`+ Add Log`)

### Apply later (same component)
- [ ] Logsheet create
- [ ] Fuel add Log
- [ ] Fuel request add
- [ ] Other expenses add

---

## Offline decisions (agreed 2026-08-12)
1. Sync: **auto** when network Available.
2. Badge: local = Offline; uploading = Syncing; after upload = API status.
3. Upload only when connected; success → delete local; fail → keep + retry.
4. After success → show API data status on list.
