# PV Transport — Production Hardening Plan (CONFIRMED)

> Confirmed via chat 2026-08-12. This file is the working checklist.
> Process: do ALL phases, then one big test pass at the end (user choice Q23=B).

## Locked decisions

| # | Decision |
|---|----------|
| D1 | Camera/photo: never crash — sample decode (inSampleSize), null-safe, try/catch, background thread, error toast |
| D2 | Gallery: keep as current — only Add Fuel Log proof/slip has gallery; odometer camera-only; nowhere else |
| D3 | Permission deny: rationale dialog + re-request; permanent deny → "Open Settings" + inline error on photo slot |
| D4 | Permissions: CAMERA (settings link), POST_NOTIFICATIONS (13+), remove unused storage perms, keep install perm solid |
| D5 | Forms: all screens — disabled until valid, disabled while saving, locked after success (isSaved) |
| D6 | Check-in reason: FOLLOW BACKEND FORMAT (inspect online API path; make offline sync identical) |
| D7 | Numeric fields: digits only + max length; end KM ≥ start KM warning |
| D8 | Dropdown auto-select once only; never overwrite user pick |
| D9 | Offline scope unchanged: check-in/out, fuel log, expense = offline; fuel request/approval/wallet = online-only |
| D10 | Fuel tab Add Request stays hidden when offline (current behavior is intended) |
| D11 | Sync fail: NO retry button — re-trigger sync on page visit / refresh / return-to-list; pending stays stored |
| D12 | One shared parseApiError(): server message/error body first, else network text; never crash |
| D13 | Errors shown via Toast everywhere |
| D14 | Login: show real reason (no internet vs invalid credentials vs master-data fail) |
| D15 | Update: keep custom APK; add download-fail retry, install-fail toast, fix Later semantics + label |
| D16 | Performance: keys everywhere, sort in VM, Wallet list fix, Coil limits + any extra finds |
| D17 | Crash reporting: SKIP (no Crashlytics, no handler) — for now |
| D18 | Keyboard: imePadding on scroll forms |
| D19 | Strings: externalize to strings.xml EN + values-my MY |
| D20 | Password in prefs: KEEP (needed for flow) |
| D21 | Minify: keep OFF this phase |
| D22 | Master data: login > check local > none → full fetch+store; has → ?since call → update/overwrite; API error → toast only. Verify implemented. Remove 5 unused endpoints |
| D23 | All phases first, then one big test pass |
| D24 | Test on low + high spec (few emulators at a time) |

## Phases / steps

### Phase 1 — Camera & permission crash-safety (P0) — DONE
- [x] 1.1 Harden `uriToFile` (CustomImagePicker.kt): bounds decode + inSampleSize, null-check, try/catch, recycle, IO thread safe; caller-visible failure (null return + toast at call sites)
- [x] 1.2 Harden `multipleUriToFile` (CustomMultipleImagePicker.kt) same way
- [x] 1.3 try/catch around FileProvider/createImageUri + camera launch in all 4 pickers
- [x] 1.4 Shared camera permission handler: rationale dialog, permanent-deny → Settings deep-link, inline error text on photo slot (all pickers)
- [x] 1.5 Fix `EditMultipleImagePicker` broken bottom-sheet/permission path (camera-only per D2)
- [x] 1.6 Remove dead gallery code in `CustomImagePickerBox`

### Phase 2 — Forms & flow (P0/P1) — DONE
- [x] 2.1 Verify backend reason format from online API path; make Daily/Trip check-in + offline sync send identical format (D6)
- [x] 2.2 Unify submit guards: `isSaved`/`isSaving` on Daily, CheckOut, FuelLog (match Trip/Expense/Request)
- [x] 2.3 `canSave` includes dropdown readiness (fuel type/company) on AddFuelLog
- [x] 2.4 Digit-only filter + max length on KM/amount/liter fields; checkout end KM ≥ start KM warning
- [x] 2.5 Auto-select once-only: move TripCheckIn composition-time mutation to LaunchedEffect; guard all auto-selects against overwriting user pick

### Phase 3 — API errors & login (P1) — DONE
- [x] 3.1 `ErrorHandler.fromResponse()` (errorBody JSON message/error/detail/Laravel errors → fallback); used across all ViewModels
- [x] 3.2 Login: distinguish no-internet / invalid credentials / master-data fail; show real message (LoginScreen + AuthViewModel)
- [x] 3.3 QR generate error surface (Toast)
- [x] 3.4 Master data flow per D22: local-first + ?since + error toast verified; empty+fail now throws → toast
- [x] 3.5 Remove 5 unused endpoints from AuthApi/FuelApi

### Phase 4 — Offline sync UX (P1) — DONE
- [x] 4.1 Re-trigger sync when list pages open/refresh (scheduleSyncIfPending in DriverLog/Fuel/Expense VMs)
- [x] 4.2 SyncWorker: missing photo → reset isSyncing, log, "Sync Incomplete" notification if records remain
- [x] 4.3 POST_NOTIFICATIONS: manifest + runtime request (Android 13+) before sync notifications

### Phase 5 — Update flow (P2) — DONE
- [x] 5.1 Download fail → toast + isDownloading reset so "Update Now" acts as retry
- [x] 5.2 Install fail → Toast (ApkInstaller, all failure paths)
- [x] 5.3 Later now stores skipped version code (per-version skip, not forever); label fixed to update_message

### Phase 6 — Performance (P1) — DONE
- [x] 6.1 FuelLogScreen: key (uuid ?: id) + sort moved into FuelViewModel combine
- [x] 6.2 FuelRequestScreen: items with keys
- [x] 6.3 WalletScreen: itemsIndexed with keys instead of one-item forEach (rounded first/last rows)
- [x] 6.4 Coil: app-wide ImageLoader with bounded memory (15%) + disk (50MB) cache in TransportApp
- [x] 6.5 Removed all debug println noise (screens, VMs, repos, SyncWorker, ConstantModule)
- [x] 6.6 Extra: WalletBalanceItem due!! → null-safe

### Phase 7 — Keyboard (P1) — DONE
- [x] 7.1 windowSoftInputMode=adjustResize + imePadding() on CheckIn/CheckOut/AddFuelLog/AddFuelRequest/AddLogSheet/AddOtherExpense/UpdateOtherExpense/Login (Login also got verticalScroll)

### Phase 8 — Strings & manifest (P3) — DONE
- [x] 8.1 Externalized hardcoded UI text → strings.xml EN + values-my MY (Clear, Confirm Submission, Wallet labels, detail labels, NoInternet, image source sheet, etc.)
- [x] 8.2 Manifest: removed WRITE/READ_EXTERNAL_STORAGE, READ_MEDIA_IMAGES; added POST_NOTIFICATIONS

### Phase 9 — Build & verify — DONE
- [x] 9.1 Gradle :app:assembleDebug BUILD SUCCESSFUL (2026-08-12); fixed duplicate findActivity overload in CameraAccess.kt
- [x] 9.2 Compiler warnings reviewed — remaining are pre-existing deprecations (TabRow, icons, etc.), no new issues

### Phase 10 — Testing (one big pass at end) — DONE VIA EMULATOR (2026-08-12 evening)
Windowed emulator (`qemu-system-x86_64`, not headless), `hw.keyboard=yes`, login session preserved.
- [x] T2 camera deny + capture ✓ (earlier)
- [x] T3 end KM warning + digit filter; End KM accepts `adb input text` ✓
- [x] T4 Add Fuel Log offline: dropdowns from cache, Save gated until photos, OFFLINE card, sync on reconnect (200) ✓
  - D2 fix: Current KM Image → `CustomImagePickerBox` (camera-only); voucher/slip remains `CustomMultipleImagePicker(enableGallery = true)`. No edit-fuel-log KM picker elsewhere.
- [x] T5/D10 Add Request hidden offline ✓
- [x] T6 Add Expense offline save+sync (15k+25k uploaded); double-submit guarded by `isButtonClicked` ✓
  - Bug fixed: offline Error without Room *cache* hid pending expenses/fuel/logs — now show pending even when cache is null (`OtherExpenseViewModel` / `FuelViewModel` / `DriverLogViewModel`). Also ExpenseScreen internet-error string match is case-insensitive.
- [x] T7 / T8 lists + wallet + offline errors ✓ (earlier)
- [x] T9 Approval offline: list shows real "No internet connection"; Generate QR Toast on Error wired; offline Generate QR button toasts "Active internet connection required." (no pending approval items to exercise PIN path this session)
- [x] T10 Update flow — code review PASS: download fail resets `isDownloading` + toast/retry; ApkInstaller toasts on all fail paths; Later uses `saveSkippedVersionCode`
- [x] T11 keyboard/`imePadding` ✓ (earlier); PC keyboard enabled again (`hw.keyboard=yes`)
- [x] T12 SyncWorker posted "Sync Completed" notification on reconnect after offline fuel+expense ✓
- [x] Phase 11 resume refresh: HOME → relaunch while offline — no crash, no error toast (silent)
- [x] T1 login variants — code review only (did not log out): network exceptions → `AuthState.Error(ErrorHandler.getMessage)` ("No internet connection"), not InvalidCredentials; 401/422 → InvalidCredentials with server message
- [ ] Low-spec small-RAM AVD pass (optional / not run)

Note: briefly hit headless emulator earlier; restarted with normal windowed emulator. Do not wipe userdata / do not kill Android Studio.

## Test checklist (run after all phases)
| # | Flow | What to verify |
|---|------|----------------|
| T1 | Login | online ok / wrong pwd real msg / offline real msg / master fail toast |
| T2 | Daily+Trip check-in | save online+offline, reason format on server, camera deny → dialog/Settings, no crash on big photo |
| T3 | Check-out | endKM ≥ startKM warning; offline save+sync |
| T4 | Add Fuel Log | odometer camera-only, slip camera/gallery sheet, dropdown ready gate, offline pending+sync |
| T5 | Add Fuel Request | server error message toast; hidden Add btn offline |
| T6 | Add Expense | offline save+sync; double-submit locked |
| T7 | Lists | keys/scroll smooth, sort correct, pending on top, revisit triggers sync |
| T8 | Wallet | long list scroll, error toast |
| T9 | Approval QR/PIN | QR error toast; PIN server error |
| T10 | Update | download fail → retry; install fail → toast; force update locked |
| T11 | Keyboard | Save visible with IME open on all forms |
| T12 | Notifications | sync notification appears on Android 13+ after permission |

---

# Phase 11 — Deferred "nice to fix" follow-ups (2026-08-12)

Separate pass after phases 1–10. Nothing above was changed.

## 11.1 Debug logging hygiene — DONE

New `util/DebugLog.kt` (`DebugLog.d` / `DebugLog.w`) wraps `android.util.Log` in a
`BuildConfig.DEBUG` check, so release builds print nothing from these call sites.

Converted to `DebugLog.d` (developer-only tracing, several of them leaked values):

| File | Was logging |
|------|-------------|
| `di/AuthenticationInterceptor.kt` | the full bearer token |
| `di/RefreshTokenInterceptor.kt` | the full bearer token + response code |
| `network/WebSocketManager.kt` | socket connect/disconnect + full socket payload |
| `network/NetworkConnectivityObserver.kt` | onAvailable / onLost |
| `network/ApkDownloader.kt` | apk absolute path + size |
| `network/ApkInstaller.kt` | install-flow progress breadcrumbs |
| `extension/CustomImagePicker.kt` | compressed photo size |
| `extension/CustomMultipleImagePicker.kt` | compressed photo size |
| `repository/AuthRepository.kt` | cached expense row count |
| `repository/FuelRepository.kt` | cached fuel row count |
| `repository/MasterDataRepository.kt` | sync result + `since`/`update` values |
| `worker/SyncWorker.kt` | pending counts, per-stage "finished", cache refresh counts |

Kept as real `Log.w` / `Log.e` (useful in the field, no data values): `SyncWorker` missing-photo
warnings, sync-failed / record-still-pending warnings and all its `Log.e`; `ApkInstaller` install
failures; `ApkDownloader` invalid-file error; `MasterDataRepository` API failure warn/error. The
`SyncWorker` response-body dumps were already inside `if (BuildConfig.DEBUG)` and were left alone.

## 11.2 Master data refresh timing (12 h) — DONE

Cold start and login were the only sync points, so an app left open for days never refreshed.

- `MasterDataRepository.REFRESH_INTERVAL_MS = TimeUnit.HOURS.toMillis(12)`.
- New `MasterDataRepository.refreshIfStale()`: downloads only when the last successful download
  is older than the interval, otherwise returns immediately without touching the network.
- `MainActivity.onResume()` calls it when logged in. Chosen over `SyncWorker` because there is no
  periodic worker in this app — `SyncWorker` is enqueued one-shot only when offline records are
  pending, so it would never run for a driver with nothing queued.
- The sync body was extracted into a private `downloadInitialData()`; `syncInitialData()` and
  `refreshIfStale()` both take the existing `syncMutex` around it, so the cold-start sync and the
  first `onResume` cannot fire two requests — the second one re-reads the freshness marker inside
  the lock and skips.
- Offline safe: `downloadInitialData()` still returns the cached-data flag when there is no
  network or the call throws; the return value is ignored at the call site, nothing blocks the UI.

**Deviation to note:** the elapsed-time check uses a new local `initial_data_synced_at`
(device clock) rather than the existing `initial_data_update`. `initial_data_update` is the
server's own cursor echoed back as `?since=`; its unit is server-defined (it is never compared to
a local clock anywhere), so subtracting it from `System.currentTimeMillis()` would be wrong if it
is anything other than epoch millis. `initial_data_update` remains the one and only delta cursor
and "have we ever synced" marker — the new key is its local-clock companion, written in the same
`AuthPrefs.saveInitialDataSync(...)` call so the two cannot drift.

## 11.3 Master-data all-empty response — DONE

If the server legitimately returns all five lists empty, every table ends up empty, the old
`hasCachedLists()` reported "no cache", and the app did a full (non-delta) fetch on every form
open forever.

- `AuthPrefs.saveInitialDataSync(update, rowCount)` now records the cursor, the row count the
  sync left cached, and the device timestamp in one write.
- `hasCachedLists()` = `cachedRowCount() > 0 || authPrefs.getInitialDataRowCount() == 0`, so a
  recorded all-empty sync counts as a valid cache and `?since` is used from then on.
- The wiped-database guard is preserved: last sync recorded N > 0 rows but the tables are now
  empty ⇒ still treated as no cache ⇒ full re-fetch.
- `getInitialDataRowCount()` defaults to `-1` (unknown) so an existing install that upgrades into
  this build keeps the previous "tables non-empty" behaviour until its next successful sync.
- `failIfUnavailable()` is untouched: empty local cache **and** a failed API still throws and
  surfaces the error toast (deliberate behaviour from phase 3.4).
- `AuthPrefs.clear()` also clears the two new keys on logout.

## 11.4 Reactive master data (Room `Flow`) — NOT IMPLEMENTED, plan only

**Why not.** The five lists are read through a four-layer one-shot chain, and each layer is
shaped around a *snapshot*, not a stream:

`XxxCacheDao.getAll(): suspend List<T>` → `MasterDataRepository.getXxx()` (calls
`ensureInitialData()` and **throws** via `failIfUnavailable`) → `AuthRepository`/`FuelRepository`
wraps the list in a fake Retrofit `Response.success(...)` → ViewModel `UiState.Loading/Success/
Error` → screen `LaunchedEffect(state) { list.clear(); list.addAll(...) }`.

Making the DAOs return `Flow` forces a rewrite of all four layers, because:

1. `failIfUnavailable()` throwing is the mechanism that produces the error toast when there is no
   cache and the network failed. A `Flow` that emits an empty list cannot throw at the right
   moment without a separate error channel — phase 3.4 behaviour would have to be rebuilt.
2. The `Response.success(...)` wrappers in `AuthRepository`/`FuelRepository` exist only so the
   ViewModels can keep their `response.isSuccessful` + `ErrorHandler.fromResponse(response)`
   shape. A `Flow` has no `Response`, so all five wrappers and every ViewModel branch change.
3. **Behavioural risk, the real blocker:** the screens hold the user's pick as an id
   (`dailySelectedIndex`, fuel type / company / cost type ids). Today the form works against a
   stable snapshot for its whole lifetime. With a live `Flow`, a sync that lands mid-form can
   replace the list under the user, leaving a selected id that no longer exists — the D8 guard
   ("auto-select once, never overwrite the user's pick") keeps the *label* but would happily
   submit a stale id. Fixing that properly means adding re-validation of the current selection to
   every dropdown screen, which is a much larger change than the staleness it cures.

Given 11.2 now refreshes at most every 12 h in the background, the stale-form window is small and
the risk/benefit does not justify it.

**File-by-file plan if it is ever wanted:**

1. `local/dao/ReasonCacheDao.kt`, `TripTypeCacheDao.kt`, `CostTypeCacheDao.kt`,
   `FuelTypeCacheDao.kt`, `FuelCompanyCacheDao.kt` — add `fun observeAll(): Flow<List<Entity>>`
   next to the existing `suspend fun getAll()`. Add, do not replace: `getAll()` is still needed by
   `cachedRowCount()`/`hasCachedLists()` and by `DriverLogViewModel`'s id→label maps.
2. `repository/MasterDataRepository.kt` — add `fun observeReasons(): Flow<List<ReasonListResponse>>`
   (and four siblings) mapping the entity flows. Keep the existing suspend getters for the
   error-surfacing path. Trigger `ensureInitialData()` once via `onStart { }` on the flow rather
   than inside the mapping, so collection never throws.
3. `repository/AuthRepository.kt`, `repository/FuelRepository.kt` — expose the flows straight
   through (`fun observeReasons() = masterDataRepository.observeReasons()`); do **not** wrap them
   in `Response`. Leave the existing `getReason()/getTripTypes()/getCostTypes()/getFuelTypes()/
   getFuelCompanies()` in place.
4. `viewmodels/ReasonViewModel.kt`, `TripTypeViewModel.kt`, `OtherExpenseViewModel.kt`
   (`getCostTypes`), `FuelViewModel.kt` (`getFuelType`, `getFuelCompanies`) — add a
   `StateFlow<List<T>>` built with `repository.observeX().stateIn(viewModelScope,
   SharingStarted.WhileSubscribed(5_000), emptyList())` alongside the existing `UiState`. Keep
   `UiState` as the loading/error channel so error toasts do not regress.
5. `presentation/DailyCheckInScreen.kt`, `TripCheckInScreen.kt`, `AddOtherExpenseScreen.kt`,
   `UpdateOtherExpenseScreen.kt`, `AddFuelLogScreen.kt` — collect the new list flow instead of
   copying into a `mutableStateListOf` inside `LaunchedEffect(state)`. Each screen then needs one
   extra guard: if the current selected id is absent from the new list, clear the selection (or
   re-run the once-only auto-select). This guard is the part that must be tested per screen.
6. `viewmodels/DriverLogViewModel.kt` — the `tripTypeMap` / reason map built in `init` can stay on
   the suspend getters; it is a display-label lookup, not a form input.

Estimated blast radius: 5 DAOs + 3 repositories + 4 ViewModels + 5 screens = 17 files, and every
dropdown form needs a manual re-test. Recommend doing it only together with a form-state refactor.

## 11.5 Persisting `is_calculate_on_ot` — NOT IMPLEMENTED, plan only

The field is not modelled anywhere in the app today (no `is_calculate_on_ot` / `isCalculateOnOt`
in the source). Storing it means a Room schema change, and `AppDatabase` currently builds with
`.fallbackToDestructiveMigration(true)` at version 11 — **any** schema bump today wipes
`app_database`, which holds the pending offline check-ins, check-outs, fuel logs and expenses plus
their photo references. Losing those loses a driver's unsynced work. Hence: plan only.

Safe path, in this order:

0. **Start exporting the schema — this is missing today.** `AppDatabase` sets
   `exportSchema = true` but `room.schemaLocation` is never configured, so kapt only warns
   ("Schema export directory was not provided…") and `app/schemas/` does not exist. Nothing
   describes version 11 on disk, so there is no baseline to write a migration against and no way
   to run `MigrationTestHelper`. Add the Room Gradle plugin (`id("androidx.room")`) plus
   `room { schemaDirectory("$projectDir/schemas") }` in `app/build.gradle.kts`, build once, and
   commit the generated `app/schemas/com.pv.transport.local.database.AppDatabase/11.json`.
1. **Stop the bleeding, in its own release.** Remove `.fallbackToDestructiveMigration(true)`
   from `local/database/AppDatabase.kt` *without* changing the schema version. Ship this alone and
   verify upgrade-in-place on a device that has pending offline rows.
2. **Model the field.** Add `@SerializedName("is_calculate_on_ot")` to the relevant
   `data/master/InitialDataResponse.kt` model (`MasterIdValue` for reasons/trip types, or a new
   model if it belongs to only one list). Type it nullable (`Boolean?` or `String?`, matching what
   the API actually sends — verify against a real response before choosing).
3. **Add the column.** Add the property to the matching entity in `local/data/` (e.g.
   `TripTypeCacheEntity`), with a Kotlin default so existing constructor calls still compile, and
   a matching SQL default so the migration can be `NOT NULL`.
4. **Write the migration.** In `AppDatabase`:
   ```kotlin
   val MIGRATION_11_12 = object : Migration(11, 12) {
       override fun migrate(db: SupportSQLiteDatabase) {
           db.execSQL("ALTER TABLE trip_type_cache ADD COLUMN is_calculate_on_ot INTEGER NOT NULL DEFAULT 0")
       }
   }
   ```
   `ALTER TABLE ... ADD COLUMN` is the cheap, safe form; no table copy, no data loss.
5. **Bump `version = 12`** and register `.addMigrations(MIGRATION_11_12)` on the builder.
6. **Populate it** in `MasterDataRepository.replaceCachedLists()` and read it wherever the OT rule
   is applied.
7. **Test the upgrade explicitly:** install the previous build, create a pending offline record,
   install the new build over it, confirm the record is still pending and syncs.

Steps 0 and 1 must ship before any schema change, not with it: until the destructive fallback is
gone and a version-11 schema is committed, a bumped version silently wipes every driver's pending
offline work on upgrade.

## 11.6 Build

`.\gradlew.bat :app:compileDebugKotlin --console=plain` → **BUILD SUCCESSFUL** (3m 35s), run once.
Only pre-existing deprecation warnings (Locale ctor, TabRow, auto-mirrored icons, `@param` target)
— nothing from this phase. No other gradle task, no adb, no emulator, no install (emulator in use
by another session).

## Files changed in phase 11

| File | Change |
|------|--------|
| `util/DebugLog.kt` | **new** — `BuildConfig.DEBUG`-gated `d`/`w` wrappers |
| `auth/AuthPrefs.kt` | `saveInitialDataSync()` replaces `saveInitialDataUpdate()`; new `initial_data_synced_at` + `initial_data_row_count` getters; both cleared on logout |
| `repository/MasterDataRepository.kt` | extracted `downloadInitialData()`; new `refreshIfStale()` + 12 h `REFRESH_INTERVAL_MS`; row-count-aware `hasCachedLists()`; gated debug log |
| `MainActivity.kt` | `onResume()` → `refreshIfStale()` when logged in |
| `worker/SyncWorker.kt` | 8 `Log.d` → `DebugLog.d`; warn/error logs untouched |
| `di/AuthenticationInterceptor.kt` | token log gated |
| `di/RefreshTokenInterceptor.kt` | token + response-code logs gated |
| `network/WebSocketManager.kt` | socket logs gated |
| `network/NetworkConnectivityObserver.kt` | connectivity callback logs gated |
| `network/ApkDownloader.kt` | apk path/size logs gated; error log kept |
| `network/ApkInstaller.kt` | progress logs gated; error logs kept |
| `extension/CustomImagePicker.kt` | file-size log gated |
| `extension/CustomMultipleImagePicker.kt` | file-size log gated |
| `repository/AuthRepository.kt` | cache-count log gated |
| `repository/FuelRepository.kt` | cache-count log gated |

No new user-visible strings, so `values/strings.xml` and `values-my/strings.xml` were not touched.

## Left for the user to decide

- 11.4 reactive master data: implement only alongside per-screen selection re-validation.
- 11.5 migration: needs the real API shape of `is_calculate_on_ot`, and steps 0–1 (export the
  schema, drop the destructive fallback) should ship as their own release before any schema bump.
- Whether the 12 h interval is right; it is one constant in `MasterDataRepository`.
