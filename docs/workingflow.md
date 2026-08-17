# Working flow — handoff for cloud agent

Read this first. Then `README.md`, `docs/`, `docs/preview/index.html`, `docs/better-to-do.md`.

You are continuing **documentation of the current (as-built) app**, not a redesign and not an Android build.

---

## Repo and machine

- Target git repo: `https://github.com/dev-php-cell/pv-transport-mobile.git`
- Local folder after clone: `C:\pv\pv-transport-mobile` (this tree was copied from `C:\pv\pv-transport`, branch `version-1.6-cursor`, including uncommitted docs + HTML).
- **Do not** `./gradlew` assemble, test, install, or use an emulator/adb.
- **Do not** implement Kotlin/Compose UI changes in this pass.
- **Do not** mark items Confirmed in `README.md`.
- **Do not** rewrite offline sync / Room merge / `SyncWorker`.
- **Do not** bump `versionCode` or rebuild APKs.
- Visual QA stays with the human later. Cloud has no real device UI.

If you need a pixel, read Compose (`1 dp = 1 px` in the HTML catalog). Font: Noto Sans Myanmar.

---

## Product goal (later, after this docs pass)

1. Human picks a **category** in README.
2. Read that category MD (as-built).
3. Discuss IDs in `docs/better-to-do.md` → human confirms.
4. If visual → change **HTML first** → human yes/no.
5. Then code in the app.
6. Update category MD + README (confirmed only).

This cloud job stops at step **as-built HTML + MD + better-to-do complete**. No confirm. No app code.

---

## This cloud job (do this)

**Goal:** every current screen/widget the app actually uses is visible in HTML, described in MD where it belongs, and every known fix/idea is in better-to-do.

### 1. Finish HTML — `docs/preview/index.html` + `styles.css`

Already in preview (as-built clone):

- Tokens (partial)
- Type samples
- Buttons: Add, Save, loading dots, disabled, danger logout, Close+Approve, **list Checkout filled red**, Login grey button
- Inputs: FormSelect, CustomFuelTextField, login CustomTextField
- Segmented tabs + bottom nav
- Badges, AppToast, OfflineBanner, DotsLoading
- List photo slots 104 dp
- Daily Log **phone mock** (header Add, one tab, tall Filters card, one log card, red Checkout, bottom bar)

**Still missing — add these as as-built clones (not proposals):**

| Area | Clone from |
|---|---|
| Splash | `SplashScreen.kt` |
| Login **full page** (green bg, fields, login button, UAT label) | `LoginScreen.kt` |
| Check-in form (daily + trip if different) | `DailyCheckInScreen.kt`, `TripCheckInScreen.kt` |
| Check-out form | `CheckOutScreen.kt` |
| Log detail | `DriverLogDetailScreen.kt` |
| Fuel Request list + Fuel Log list + Wallet | those screens |
| Add Fuel Request / Add Fuel Log forms | those screens |
| Fuel details | detail screens |
| Approval list (select all, Generate QR) | `ApprovalScreen.kt` |
| Generate QR dialog (user search, Cancel/Continue) | same |
| QR vs PIN+signature panel, Close/Approve | same |
| Expense list (filter card) + add/update form + detail | expense screens |
| Profile + logout danger + language | `ProfileScreen.kt`, `LanguageScreen.kt` |
| Photo: odometer dashed box vs proof thumbs vs picker sheet | picker files + forms |
| Date picker field used in filters | `CustomDatePicker` |
| Confirm-exit dialog, update version sheet | those composables |
| Office vs driver bottom bar (4 vs 5 tabs) | `MainBottomBar.kt` |

Keep the banner: **AS-BUILT — not confirmed.** Do not restyle to the “better-to-do” look in HTML yet. New HTML sections should match code hex/dp.

### 2. Finish MD so they match the HTML

Same categories as README (keep this list):

| File | Job now |
|---|---|
| `README.md` | Map only. Link workingflow. Confirmed column stays empty. |
| `docs/app-flow.md` | Head-to-toe. Add any screen you discover that is missing from the diagram. |
| `docs/ui-standardization.md` | Specs / variants / usage. When you add a widget to HTML, add its table here if missing. |
| `docs/ux-standardization.md` | Motion, save/clear, scroll, filters, network — written, not pretty pictures. |
| `docs/third-party.md` | Where / for what / how. Socket.IO = `qr-verify-approve` only today. |
| `docs/code-structure-and-performance.md` | Packages, shared vs copied, Compose keys/thumbs. |
| `docs/app-performance.md` | What the driver feels. |
| `docs/testing.md` | Almost no tests today; plan stays in better-to-do. |
| `docs/releasing-and-production.md` | Flavors uat/production, version 1.6 / code 7. **Do not paste keystore passwords.** |
| `docs/better-to-do.md` | Keep IDs. Add newly noticed as-built gaps. Do not implement. |

`todo/` is **gitignored**. Do not rely on `todo/continue.md` after push. Intent is copied below and in better-to-do.

### 3. Do not start Kotlin

No component extract, no token unify, no filter-icon dialog in the app. That is after human confirm.

---

## Continue.md intent (2026-08-17) — parked, not done

Compact, clean, modern first; then one component set everywhere.

1. Forms: enter from **left**, exit to **right** (current code is Android-style enter from **right**, 320 ms — see UX-01).
2. One primary outline-green button: Add, Save, Checkout, Generate QR, modal steps.
3. Denser cards; Daily Log ≥2 full cards; no duplicate labels.
4. Restyle tabs (one component).
5. Filter + sort **icons** + dialog (kill tall Filters card).
6. Thinner border, more round, tighter padding.
7. One in-app message chrome (replace Toast).
8. One circle loader for page loading (Save today uses **dots**).
9. Smooth scroll = keys + thumbs + nested scroll, not extra animation.
10. Then extract components.

Also: one primary green, photo field alignment, reconnect on Available for Request/Approval/Wallet, **do not rewrite working offline sync**.

Out of scope until human asks: new features, Crashlytics, package rename, minify/R8, APK bump.

---

## As-built facts cloud must not “fix” in HTML

- List **Checkout** = filled `#E53935`, height 52, radius 12 — not `FormPrimaryButton`.
- **Save** = outline `#169A5A`, height 50, radius 16, loading = `DotsLoading`.
- **Add** = outline same green, height 40.
- Login field `#176B43`, login button `#D9D9D9`.
- Several greens in `Color.kt` (`#169A5A`, `#1B8E50`, `#1B8E5F`, `#1FA15B`).
- `PVTransportTheme` still Material purple; screens hardcode greens.
- Logsheet tab compiled off (`ENABLE_LOG_SHEET_TAB = false`).
- Four image pickers, not one `AppPhotoField`.

---

## How we work with the human (after this job)

Human: “do UI” → you open `docs/ui-standardization.md` + better-to-do UI-* IDs → discuss → they confirm IDs → HTML proposal variant → they yes/no → **then** Compose.

Until they confirm, HTML stays as-built plus optional clearly labeled **proposal** sections (e.g. `class="proposal"`), never overwrite as-built mocks.

---

## Done when

- [ ] Preview HTML has splash, login, log list, one form, fuel, approval+QR chrome, expense, profile, photo variants — enough that a mobile dev can see the current app without running it.
- [ ] Each of those has specs in `ui-standardization.md` or a pointer to the HTML section.
- [ ] App-flow diagram includes those routes.
- [ ] better-to-do still has continue.md + later notes; new gaps added as IDs.
- [ ] README confirmed column still empty.
- [ ] No gradle, no emulator, no Kotlin UI rewrite.

When finished, reply with: files touched, screens added to HTML, MD gaps filled, anything you could not clone from code.
