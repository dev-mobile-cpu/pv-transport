# PV Transport — To Do

## Production hardening (active)
See full confirm questionnaire + fix/test plan:
→ [`todo/production_hardening_confirm.md`](production_hardening_confirm.md)

Answer Q1–Q29 in chat (or “use all Recommended”), then step-by-step fixes + testing.

---

## Task 1 — Logsheet tab + Create button
- [x] Disable Logsheet tab (`ENABLE_LOG_SHEET_TAB = false`).
- [x] Header Create button + tab-aware navigation.
- [ ] Verify in Android Studio.

## Task 2 — Full Offline (local-first)
- [x] Add Log / Checkout local-first (3 ways: full online, full offline, halfway).
- [x] Sync supports: check-in only, checkout only, check-in+checkout pair.
- [x] Fuel Log + Other Expense local-first (same pattern).
- [x] Fuel Request + Approval remain online-only.
- [ ] Verify poor/offline/online.

## Task 3 — Header Add buttons + tabs
- [x] Shared `AddActionButton` + `SegmentedTabs`.

## Task 4 — Photos / form buttons / transitions
- [x] `LogKmPhotoSlot` gray + shimmer + crossfade (fixed compiler issue).
- [x] `FormPrimaryButton` solid `#169A5A`.
- [x] Shared `NavTransitions`.

## Task 5 — Network UX
- [x] Remove red Offline banner under filters.
- [x] Telegram-style title: Waiting for network / Connecting (Log, Fuel, Approval, Expense, Profile).
- [x] Offline date filters read-only (Log, Fuel Log/Request, Expense, Approval).

---

## Offline sync decisions (confirmed)
1. Full online / full offline / halfway — supported.
2. Halfway checkout → checkout-only sync; also check-in-only and pair sync.
3. Fuel Log + Expense same local-first.
4. List: last fetch + pending on top.
5. Offline date filter readonly.
