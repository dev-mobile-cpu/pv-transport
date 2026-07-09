# Offline Mode Enhancement Tasks

## Daily Logs (Completed)
- [x] Implement a real-time Network Connectivity Monitor using Flow.
- [x] Add an "Offline Mode" warning banner/indicator in `LogScreen.kt`.
- [x] Refactor `DriverLogViewModel` to merge cached server logs, offline check-ins, and offline check-outs into a single unified list.
- [x] Update Log Card UI to match the online design for offline entries.
- [x] Implement a status badge (e.g., "Offline" or "Syncing") at the top right of the log card.
- [x] Enable the "Checkout" button for offline logs within the unified card UI.
- [x] Implement background sync logic and completion notifications.

## Phase 4: Fuel Logs Offline Enhancement
- [ ] Implement Unified Fuel List (Merged online/offline logs) in `FuelViewModel`.
- [ ] Add "Offline Mode" warning banner in `FuelLogScreen.kt`.
- [ ] Update Fuel Log Card UI to match online design with "OFFLINE" and "SYNCING" status badges.
- [ ] Integrate Fuel Sync logic with `SyncWorker` and provide visual feedback.

## Phase 5: Other Expenses Offline Enhancement
- [ ] Implement Unified Expense List in `OtherExpenseViewModel`.
- [ ] Add "Offline Mode" warning banner in `ExpenseScreen.kt`.
- [ ] Update Expense Card UI with consistent status badges and sync indicators.
- [ ] Enable "Add/Update Expense" offline functionality with background sync.
- [ ] Provide nice visual design for uploading progress when back online.
