# App performance

Status: **as-built**. What the driver feels. Verify on a **phone**, not the HTML preview.  
Parked: [better-to-do](better-to-do.md#app-performance).

---

## Lists and images

- Daily Log cards decode **thumbs** (`thumbDecode`, sized to slot ~104 dp).
- Prefer local file after sync so the list does not refetch (`preferredListPhoto`).
- Shimmer overlay until first decode.
- Keys on log items reduce wrong-item flash on update.
- Filter + card padding 16 means about **1.5 cards** on a typical phone — density is a UI/UX issue that also affects how much work Compose does per frame.

---

## Scroll

Collapsible title/bottom bar should not steal fling. Connection returns `Offset.Zero` consumption. Jank remaining is usually image decode, unbounded recomposition, or nested pager + list.

Tab: pager keeps scroll position per page. Requested 180 ms fade is not how pager works today.

---

## Network

Refetch when `ConnectivityObserver` becomes `Available` is implemented on Driver Log, Fuel, Other Expense ViewModels (pattern). Confirm Wallet / Approval / Fuel Request on device when changing this. Avoid refetch storms (once per Available, not every blink).

---

## Startup and binary

- Splash then login or home from `AuthPrefs`.
- Release minify **false** — APK larger; R8 is parked until the design pass is stable.
- Three dex files in current release outputs (multidex implicit).

---

## Stop condition

If a performance or UI pass makes offline rows **duplicate or disappear**, stop and report. Do not “fix sync” unless asked.
