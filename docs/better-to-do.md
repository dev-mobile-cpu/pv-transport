# Better to do

Nothing here is confirmed. You pick a category, we discuss an ID, you say yes → it moves into the category MD and README. Visual IDs get an HTML variant first.

Sources: `todo/continue.md` (2026-08-17), later product notes, and current as-built gaps.

---

## How to confirm

Reply like: `UI-05 yes` or `UX-01 keep current Android slide, no`.  
Then: HTML if needed → code → update detail MD + README.

**Do not implement from this file until you confirm.**

---

## App flow

| ID | Idea | Notes |
|---|---|---|
| FLOW-01 | Keep logsheet tab off until you ask | `ENABLE_LOG_SHEET_TAB = false` |
| FLOW-02 | Document office vs driver as the only role split | Already as-built |

---

## UI standardization

| ID | Idea | From |
|---|---|---|
| UI-01 | One primary green; drop `#1B8E50` / `#169A5A` / `#1B8E5F` mix | continue tokens |
| UI-02 | MaterialTheme uses brand (remove unused purple scheme) | code |
| UI-03 | Type scale: title, body, label, caption, button | continue + Type.kt empty |
| UI-04 | Space 4/8/12/16; card padding 10–12 | continue |
| UI-05 | One outline-primary family: Add, Save, Checkout, Generate QR, QR/modal next-back | continue |
| UI-06 | Same shape; color/action change (primary / danger / disabled / loading) | you |
| UI-07 | Thinner border, slightly more round, tighter padding (keep the Add look) | continue |
| UI-08 | List Checkout is filled red 52/12 — fold into kit or keep as danger? | as-built vs continue |
| UI-09 | Login field/button stay special or join kit | as-built |
| UI-10 | Compact Daily Log cards; ≥2 full cards; date+status one row; km inline; no dash empty plate | continue |
| UI-11 | Restyle tabs (still one component); bottom nav + segmented | continue |
| UI-12 | Filter/sort **icons** + dialog; remove tall Filters card | continue |
| UI-13 | `AppMessage` snackbar/banner; tones success/info/error/offline; EN+MY; no system Toast | continue |
| UI-14 | One circle loader for page empty/loading (Approval included) | continue |
| UI-15 | Button loading: **one** of circle vs dots | continue; Save uses dots today |
| UI-16 | One `AppPhotoField` (odometer dashed full width; proof thumbs aligned) | continue |
| UI-17 | Inputs after remodel: one text + one select + label + error under field | you |
| UI-18 | Semantic badge colors from tokens only | StatusBadge hex |

**Recommend (not confirmed):** UI-15 = circle everywhere, including Save. Say no if you want dots in buttons.

---

## UX standardization

| ID | Idea | From |
|---|---|---|
| UX-01 | Form enter **from left**, exit **to right** | continue — **conflicts with current** right-enter 320 ms |
| UX-02 | Same transition on all form-like routes | continue |
| UX-03 | Tabs: fade 180 ms; keep list scroll | continue — today is pager |
| UX-04 | iOS-like smooth scroll (keys, no nested fight, no full bitmap) | continue |
| UX-05 | Reconnect refetch only on `Available` for Request / Approval / Wallet | continue |
| UX-06 | Save: loading in button → message → pop (fields clear because screen closes) | as-built; keep |
| UX-07 | Field errors under field; toasts not for validation | continue |
| UX-08 | Do not change which screens work offline | continue out of scope |

---

## Third party

| ID | Idea | From |
|---|---|---|
| TP-01 | Keep Socket.IO only for `qr-verify-approve` until a real push product exists | as-built |
| TP-02 | Crashlytics | continue out of scope |
| TP-03 | FCM / general realtime noti | future plugin — you asked to reserve the category |
| TP-04 | Drop or gate `okhttp-profiler` in production builds | code smell |

---

## Code structure and performance

| ID | Idea | From |
|---|---|---|
| CODE-01 | Extract kit **after** UI confirm (`AppButton`, `AppMessage`, `AppCard`, `AppTabs`, `AppLoader`, `AppPhotoField`, `AppTextField`) | continue “then extract” |
| CODE-02 | Thin components only; no skip-busting wrappers | continue |
| CODE-03 | Stable params; fewer inline lambdas on list rows | continue |
| CODE-04 | One photo compress/permission path | continue |
| CODE-05 | `ui/components/` folder once kit is real | note |

---

## App performance

| ID | Idea | From |
|---|---|---|
| PERF-01 | Audit all lists for `key` + thumb decode | continue |
| PERF-02 | Minify/R8 after design pass stable | continue |
| PERF-03 | Stop if offline duplicate/drop | continue |

---

## Testing

| ID | Idea | From |
|---|---|---|
| TEST-01 | Real unit tests (KM, mapping, date/sort, Available refetch) | you + continue path |
| TEST-02 | Remove or ignore `2+2` example | as-built |
| TEST-03 | Paparazzi/Roborazzi kit PNGs for cloud | earlier note |
| TEST-04 | Black-box stays on your device | honest cloud limit |
| TEST-05 | Cloud `environment.json` + SDK so `./gradlew test` runs on VM | earlier note |

---

## Releasing and production

| ID | Idea | From |
|---|---|---|
| REL-01 | No versionCode bump until you pass local test | continue |
| REL-02 | Move signing passwords out of Gradle into local/CI secrets | security |
| REL-03 | Package rename | continue out of scope |
| REL-04 | New features freeze during this design pass | continue |

---

## Extra (from earlier design notes)

| ID | Idea | Goes to if yes |
|---|---|---|
| B1 | HTML/Compose preview catalog of kit | UI |
| B5 | Bottom nav outline vs filled icon | UI |
| B6 | Empty-state layout (one line + action) | UI / UX |
| B7 | Pull-to-refresh on online lists | UX |
| B9 | Dots in Save vs one circle — duplicate of UI-15 | UI |
| B12 | Cloud Android SDK snapshot | Testing |
| B13 | Extract pure merge **without behavior change** + unit test | Code / Testing |
| B16 | One date/time picker component | UI |
| B17 | contentDescription on icon-only filter | UX |

---

## Confirmed promotions

| ID | Date | Moved to |
|---|---|---|
| — | — | — |
