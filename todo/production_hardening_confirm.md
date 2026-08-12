# PV Transport — Production Hardening Confirm & Plan

> **Status:** Awaiting your answers (multiple choice + Other).  
> **Rule:** After you confirm, work proceeds **step by step**. No mass rewrite until answered.  
> **Rescanned:** 2026-08-12 (includes recent ImageSourceBottomSheet, MasterData, FormPrimaryButton, NetworkAwarePageTitle, offline local-first).

---

## How to answer

Reply in chat like this (example):

```
Q1: A
Q2: B, C
Q3: Other — <your text>
Q4: A
...
```

You can skip with `Skip` / answer later. Unanswered → use **Recommended** default.

---

## Part A — What I see now (current code reality)

### Already good (keep)
| Area | What exists |
|------|-------------|
| Offline local-first | Check-in / Check-out / Fuel Log / Other Expense → Room + WorkManager `SyncWorker` |
| Master data offline | `MasterDataRepository` + cold sync in `MainActivity` |
| Network title UX | `NetworkAwarePageTitle` (Waiting for network / Connecting) |
| Form submit button | Shared `FormPrimaryButton` (loading disable) |
| Image source UX | `ImageSourceBottomSheet` (camera / gallery when enabled) |
| List keys (some) | Log / Expense / Approval use LazyColumn keys |
| Force update | Download progress + unknown-sources Settings |
| Fuel Request / Approval / Wallet | Intentionally online-only (per prior todo) |

### Remaining gaps (production risks)
| Priority | Gap | Where |
|----------|-----|--------|
| **P0** | Camera/upload bitmap full-decode → **NPE / OOM crash** on low-RAM | `CustomImagePicker.uriToFile`, `CustomMultipleImagePicker.multipleUriToFile` |
| **P0** | Camera permission deny = **Toast only** (no Settings / no form error) | All image pickers |
| **P0** | Check-in still may send **reason as index ID** not string value | `DailyCheckInScreen`, `TripCheckInScreen` |
| **P0** | No Crashlytics / uncaught handler | `TransportApp` |
| **P1** | API errors inconsistent — many show HTTP phrase, not server `message` | Most ViewModels; only fuel-request / approve-PIN parse body well |
| **P1** | Keyboard covers Save — **no `imePadding` / softInputMode** | All long forms + `MainActivity` |
| **P1** | Fuel list: **no keys** + sort in composition; Wallet poor Lazy recycling | `FuelLogScreen`, `FuelRequestScreen`, `WalletScreen` |
| **P1** | Login always shows “invalid username/password” even for network/master fail | `LoginScreen` |
| **P1** | `POST_NOTIFICATIONS` missing (Android 13+ sync notify silent) | Manifest + SyncWorker |
| **P2** | App update: download fail / install fail UX weak; reopen not guaranteed | `UpdateVersionBottomSheet`, `ApkInstaller` |
| **P2** | SyncWorker silent fail / missing photo skip forever | `SyncWorker` |
| **P2** | Double-submit / `isSaved` inconsistent across forms | Daily / CheckOut / FuelLog |
| **P2** | `EditMultipleImagePicker` gallery/permission path incomplete | Edit picker |
| **P3** | Hardcoded EN/MY strings not in `strings.xml` | Pickers, toasts |
| **P3** | Unused storage permissions; release minify off; plaintext password in prefs | Manifest, AuthPrefs, build.gradle |

---

## Part B — Questionnaire (confirm before fix)

### Category 1 — Camera / Media (crash-safe)

**Q1. Camera / image compress crash hardening**  
When photo decode fails or device is low memory, what should happen?  
- [ ] **A (Recommended)** Never crash: show error on form + Toast/Snackbar; skip bad image  
- [ ] **B** Crash silently and restart (not recommended)  
- [ ] **C** Other: _______________

**Q2. Bitmap / upload strategy for low-spec phones**  
- [ ] **A (Recommended)** Sample decode (`inSampleSize`) + max ~1280px JPEG + recycle + off main thread  
- [ ] **B** Keep current full decode, only add try/catch  
- [ ] **C** Other: _______________

**Q3. Gallery vs Camera**  
- [ ] **A (Recommended)** Keep bottom sheet (Camera / Gallery) where `enableGallery=true`; odometer stays camera-only  
- [ ] **B** Always allow gallery everywhere  
- [ ] **C** Camera only everywhere (remove gallery)  
- [ ] **D** Other: _______________

**Q4. Permission denied / permanent deny**  
- [ ] **A (Recommended)** Dialog: explain why → Request again; if permanent deny → **Open App Settings** button + inline error on photo slot  
- [ ] **B** Toast only (current)  
- [ ] **C** Other: _______________

---

### Category 2 — Device permissions

**Q5. Which runtime permissions to handle properly?**  
(Select all that apply)  
- [ ] **A (Recommended)** CAMERA (with Settings deep-link)  
- [ ] **B (Recommended)** POST_NOTIFICATIONS (Android 13+) for sync status  
- [ ] **C** REQUEST_INSTALL_PACKAGES / unknown sources (already partially done — keep improve)  
- [ ] **D** Storage / READ_MEDIA_IMAGES (only if still needed; Photo Picker usually enough)  
- [ ] **E** Other: _______________

**Q6. Unused storage permissions in Manifest**  
- [ ] **A (Recommended)** Remove if Photo Picker + cache FileProvider cover all cases  
- [ ] **B** Keep for older Android compatibility  
- [ ] **C** Other: _______________

---

### Category 3 — Code flow / Form validation

**Q7. Submit button rules**  
- [ ] **A (Recommended)** Disabled until all required fields valid; disable while saving; lock after success (`isSaved`) on **all** add/check screens  
- [ ] **B** Keep each screen as-is; only fix broken ones  
- [ ] **C** Other: _______________

**Q8. Check-in `reason` value**  
Current risk: may send index ID instead of reason string.  
- [ ] **A (Recommended)** Always send **reason string value** (API-compatible)  
- [ ] **B** Send numeric ID if backend expects ID  
- [ ] **C** Other: _______________

**Q9. Numeric validation (KM, amount, liter)**  
- [ ] **A (Recommended)** Digits only + max length; optional: end KM ≥ start KM warning  
- [ ] **B** Presence-only (current style)  
- [ ] **C** Other: _______________

**Q10. Dropdown auto-select first item**  
- [ ] **A (Recommended)** Auto-select once only; never overwrite after user picked  
- [ ] **B** Keep current auto-select behavior  
- [ ] **C** Other: _______________

---

### Category 4 — Offline / Online

**Q11. Offline feature scope (confirm)**  
- [ ] **A (Recommended)** Keep: Log check-in/out + Fuel Log + Other Expense = offline OK; Fuel Request + Approval + Wallet = online-only with clear message  
- [ ] **B** Also make Fuel Request offline  
- [ ] **C** Also make Approval offline  
- [ ] **D** Other: _______________

**Q12. Sync failure UX**  
- [ ] **A (Recommended)** Show pending count / last sync fail + Retry; don’t silently drop forever  
- [ ] **B** Silent background only (current lean)  
- [ ] **C** Other: _______________

**Q13. Offline while filling a form that is online-only**  
- [ ] **A (Recommended)** Block submit + clear message (“Need internet for this”)  
- [ ] **B** Allow typing but fail on submit with Toast  
- [ ] **C** Other: _______________

---

### Category 5 — API / Server errors

**Q14. Error message policy**  
- [ ] **A (Recommended)** One shared `parseApiError()`: prefer server `message`/`error` body; else mapped network text; never crash  
- [ ] **B** Keep ErrorHandler for network only; leave each VM as-is  
- [ ] **C** Other: _______________

**Q15. How to show errors in UI**  
- [ ] **A (Recommended)** Snackbar / inline Text with retry on lists; Toast OK for quick actions  
- [ ] **B** Toast everywhere  
- [ ] **C** Dialog for all errors  
- [ ] **D** Other: _______________

**Q16. Login error**  
- [ ] **A (Recommended)** Show real reason: wrong password vs no internet vs master-data sync fail  
- [ ] **B** Keep generic “Invalid username or password” always  
- [ ] **C** Other: _______________

---

### Category 6 — App version update

**Q17. Update distribution model**  
- [ ] **A (Recommended for current)** Keep custom APK download + installer; improve fail/retry/install UX  
- [ ] **B** Migrate to Google Play In-App Updates  
- [ ] **C** Other: _______________

**Q18. After download completes**  
- [ ] **A (Recommended)** Keep sheet: Install button → system installer; on fail show error + retry; document that OS may kill app on update (normal)  
- [ ] **B** Auto-install immediately only (current lean)  
- [ ] **C** Other: _______________

**Q19. Force update**  
- [ ] **A (Recommended)** Cannot dismiss / cannot use app until updated (keep + polish)  
- [ ] **B** Allow Later always  
- [ ] **C** Other: _______________

---

### Category 7 — Performance / low-spec

**Q20. List performance**  
- [ ] **A (Recommended)** Add LazyColumn keys everywhere; sort in ViewModel; fix Wallet item recycling; Coil size limits on thumbnails  
- [ ] **B** Only fix FuelLogScreen  
- [ ] **C** Skip for now  
- [ ] **D** Other: _______________

**Q21. Release crash reporting**  
- [ ] **A (Recommended)** Add Firebase Crashlytics (or Sentry) + basic uncaught handler  
- [ ] **B** Uncaught handler + log file only (no third party)  
- [ ] **C** Skip for now  
- [ ] **D** Other: _______________

**Q22. Release minify (R8)**  
- [ ] **A** Enable minify + keep rules (more work, smaller APK)  
- [ ] **B (Recommended for this phase)** Keep minify off; harden crashes first  
- [ ] **C** Other: _______________

---

### Category 8 — UI / UX (keyboard, language, polish)

**Q23. Keyboard covering Save button**  
- [ ] **A (Recommended)** `imePadding()` on scroll forms + soft input adjustResize / insets  
- [ ] **B** Skip  
- [ ] **C** Other: _______________

**Q24. Language for errors / toasts / pickers**  
- [ ] **A (Recommended)** Move to `strings.xml` + `values-my` (EN + MY)  
- [ ] **B** Keep hardcoded bilingual in code  
- [ ] **C** English only  
- [ ] **D** Other: _______________

**Q25. Password stored in SharedPreferences**  
- [ ] **A (Recommended)** Stop storing plaintext password; use token/session only  
- [ ] **B** Keep (needed for some flow) — explain why in Other  
- [ ] **C** Other: _______________

---

### Category 9 — Scope / process / testing

**Q26. Fix order (phases)**  
- [ ] **A (Recommended)** P0 crash/data → P1 UX blockers → P2 reliability → P3 polish → full test matrix  
- [ ] **B** Your order: _______________  
- [ ] **C** Other: _______________

**Q27. After each phase**  
- [ ] **A (Recommended)** Pause for your device smoke-test, then continue  
- [ ] **B** Finish all phases, then one big test pass  
- [ ] **C** Other: _______________

**Q28. Devices / API levels to prioritize for testing**  
- [ ] **A (Recommended)** Low-RAM (2–3GB) Android 8–10 + mid Android 12–14 + one Android 13+ for notifications  
- [ ] **B** Only latest phones  
- [ ] **C** Other: _______________

**Q29. Anything else you want included before we start?**  
- [ ] **A** None — proceed with Recommended defaults for unanswered  
- [ ] **B** Other: _______________

---

## Part C — Proposed step-by-step work plan (after confirm)

### Phase 0 — Confirm answers (this file)
- [ ] You answer Q1–Q29  
- [ ] I lock decisions + update this checklist  

### Phase 1 — P0 Crash & data correctness
1. Harden `uriToFile` / `multipleUriToFile` (sample, null-safe, try/catch, recycle, background)  
2. Camera permission UX: rationale + Settings deep-link + inline error  
3. Fix check-in reason value (per Q8)  
4. Fix `EditMultipleImagePicker` path / dead gallery  
5. Crash reporting (per Q21)  

### Phase 2 — P1 Production UX blockers
6. Shared `parseApiError()` + apply across ViewModels  
7. Login / QR real error surfaces  
8. `imePadding` + soft input on forms  
9. LazyColumn keys + FuelLog sort out of composition + Wallet list fix  
10. `POST_NOTIFICATIONS` request  

### Phase 3 — P2 Reliability
11. SyncWorker fail visibility + retry  
12. Unify `isSaved` / double-submit on all forms  
13. Fuel type/company in `canSave`  
14. TripType auto-select race fix  
15. Update sheet retry / install error UX  

### Phase 4 — P3 Polish
16. Externalize strings EN+MY  
17. Manifest permission cleanup  
18. Password storage (per Q25)  
19. Coil memory policy; remove debug println  
20. Optional R8 (per Q22)  

### Phase 5 — Full testing matrix
See Part D. Mark pass/fail per cell; fix regressions before release.

---

## Part D — Testing matrix (run after fixes)

| # | Flow | Online | Offline | Permission deny | Low memory / large photo | Keyboard |
|---|------|--------|---------|-----------------|--------------------------|----------|
| T1 | Login | wrong pwd / success / server error | clear offline message | N/A | N/A | fields usable |
| T2 | Daily check-in | save + list | save pending + sync | camera deny → Settings | no crash | Save visible |
| T3 | Trip check-in | same | same + reason correct on server | same | same | same |
| T4 | Check-out | same | same | same | same | same |
| T5 | Add Fuel Log | online path | offline pending + sync | camera/gallery | multi+KM photos | Save visible |
| T6 | Add Fuel Request | success + **server** error text | blocked clearly | images | — | Save visible |
| T7 | Add Other Expense | online | offline + sync | camera | multi | Save visible |
| T8 | Lists (Log/Fuel/Expense) | scroll/paginate | cached + pending | N/A | many images no jank/OOM | N/A |
| T9 | Wallet | load | error clear | N/A | long list | N/A |
| T10 | Approval QR | generate + approve | internet required | N/A | QR ok | N/A |
| T11 | Approval PIN + sign | wrong PIN shows server error | blocked | N/A | signature | N/A |
| T12 | Force update | download → install → reopen | fail + retry | unknown sources Settings | large APK | N/A |
| T13 | SyncWorker | mixed queue uploads | N/A | notification (API 33+) | many photos | N/A |
| T14 | Form validation | button disabled until required filled; no double submit | same | N/A | N/A | N/A |

---

## Part E — Your answer sheet (copy-paste)

```
Q1:
Q2:
Q3:
Q4:
Q5:
Q6:
Q7:
Q8:
Q9:
Q10:
Q11:
Q12:
Q13:
Q14:
Q15:
Q16:
Q17:
Q18:
Q19:
Q20:
Q21:
Q22:
Q23:
Q24:
Q25:
Q26:
Q27:
Q28:
Q29:
```

---

## Notes
- I will **not** start code fixes until you reply with answers (or say “use all Recommended”).  
- After answers: update this MD checkboxes → implement Phase 1 → you smoke-test → next phase.  
- File location: `todo/production_hardening_confirm.md`
