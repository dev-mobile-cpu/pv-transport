# PV Transport

Android driver app (Jetpack Compose) for daily logs, fuel, approvals, expenses, and wallet.

This README is the **map**. It lists the same categories as `docs/`.  
**Confirmed** items land here after you say yes. Until then, treat detail files as **as-built** (what the code does today).

| Need | Open |
|---|---|
| Map of confirmed work | this file |
| Cloud / next-agent handoff | [docs/workingflow.md](docs/workingflow.md) |
| As-built specs and jobs | [docs/](docs/) |
| See current UI / confirm a change | [docs/preview/index.html](docs/preview/index.html) (open in a browser) |
| Ideas not confirmed | [docs/better-to-do.md](docs/better-to-do.md) |

Preview HTML is cloned from Compose defaults (`1 dp = 1 px` in the catalog). Emulator was not available when this was first written, so pixels come from code, not screenshots.

---

## How we work

1. Pick a **category** below.
2. Read its detail MD (as-built).
3. Open [better-to-do](docs/better-to-do.md) for that category — discuss, you confirm.
4. If it is visual → update [HTML preview](docs/preview/index.html) first; you say yes/no.
5. Then **code** in the app.
6. Update the detail MD **and** this README (confirmed only).

Do not implement parked items. Do not rewrite working offline sync unless a row still duplicates or drops.

---

## Categories

Same set as the docs folder. Confirmed column starts empty.

| # | Category | Detail | HTML | Confirmed |
|---|---|---|---|---|
| 1 | App flow | [docs/app-flow.md](docs/app-flow.md) | list mock in preview | — |
| 2 | UI standardization | [docs/ui-standardization.md](docs/ui-standardization.md) | **yes** — kit | — |
| 3 | UX standardization | [docs/ux-standardization.md](docs/ux-standardization.md) | motion notes only | — |
| 4 | Third party | [docs/third-party.md](docs/third-party.md) | no | — |
| 5 | Code structure and performance | [docs/code-structure-and-performance.md](docs/code-structure-and-performance.md) | no | — |
| 6 | App performance | [docs/app-performance.md](docs/app-performance.md) | no | — |
| 7 | Testing | [docs/testing.md](docs/testing.md) | no | — |
| 8 | Releasing and production | [docs/releasing-and-production.md](docs/releasing-and-production.md) | no | — |

---

## 1. App flow

**As-built:** Splash → Login → Home tabs. Driver: Log, Fuel, Approval, Expense, Profile. Office: no Approval tab. Forms hide the bottom bar. Logsheet tab is compiled off.

**Confirmed:** none yet.

---

## 2. UI standardization

**As-built (not one kit yet):**

- Add / Generate QR — compact outline green (`AddActionButton`, 40 dp)
- Save / Checkout form / Approve / Continue — block outline green (`FormPrimaryButton`, 50 dp)
- List Checkout — **filled red**, different radius
- Login — filled grey on green screen
- Close / Cancel — grey outline, same 50 dp height

See variants and usage in [ui-standardization.md](docs/ui-standardization.md) and the [preview](docs/preview/index.html).

**Confirmed:** none yet.

---

## 3. UX standardization

**As-built:** form enter from the **right** (320 ms). Title + bottom bar hide on list scroll. Save uses in-button dots + `AppToast`. Daily Log uses a tall Start/End filter card.

**Confirmed:** none yet. Requested changes live in [better-to-do](docs/better-to-do.md).

---

## 4. Third party

**As-built:** Retrofit/OkHttp, Hilt, Coil, Room, WorkManager, Socket.IO (`qr-verify-approve`), version check API.

**Confirmed:** none yet. Crashlytics and extra plugins are parked.

---

## 5. Code structure and performance

**As-built:** `presentation` / `viewmodels` / `repository` / `local` / `ui/theme`. Some shared widgets; four image pickers still separate.

**Confirmed:** none yet.

---

## 6. App performance

**As-built:** list photo thumbs + `key` on Daily Log. Reconnect refetch exists on several ViewModels. R8 off.

**Confirmed:** none yet.

---

## 7. Testing

**As-built:** placeholder `ExampleUnitTest` / instrumented example only.

**Confirmed:** none yet.

---

## 8. Releasing and production

**As-built:** flavors `uat` / `production`, `versionName` 1.6, `versionCode` 7, release signing, minify off.

**Confirmed:** none yet. Do not bump APK until you say local test passed.

---

## Stack (quick)

- minSdk 26, compile/target 36, Compose + Hilt
- Package `com.pv.transport` (UAT suffix `.uat`)
- Font: Noto Sans Myanmar
- Light theme only (`PVTransportTheme` forces `darkTheme = false`)
