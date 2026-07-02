Act as a Senior Android Developer. I need to implement an Offline-First architecture and UI/UX state in this Java/Kotlin project using Room DB and WorkManager.

### Critical Rule:
Do NOT ruin or overwrite our existing Retrofit/API binding interfaces. Build a clean repository layer on top of them to manage data between local storage and remote networks.

### Specific API Mappings & Dependency Logic:

1. **Sequential Offline Sync for Driver Logs (Check-in -> Checkout):**
    - Both `save_driver_log` (Check-in) and `edit_driver_log` (Checkout) must now support Offline Mode.
    - If a user performs both Check-in and Checkout while offline, they must be saved locally in Room DB with an explicit relation or timestamp ordering.
    - **WorkManager Sync Order:** When the network becomes available, the `SyncWorker` MUST send the `save_driver_log` payload to the server first. It must wait for a successful `200 OK` response from the server before executing the `edit_driver_log` payload. This prevents dependency errors on the server.

2. **Other Offline Data Creation & Queue:**
    - Create Room tables for `save_fuel_log` and `save_other_expense`.
    - Add `isSynced: Boolean` (default false), `clientTimestamp: Long`, and a unique `uuid: String` (Idempotency key) to all offline-supported tables.
    - Show a "pending clock" icon next to these items in their respective list views if `isSynced == false`.

3. **Local Caching for Dynamic Form Data (Read-Only):**
    - For `driver/reasons`, `driver/trip_types`, `driver/get_type_of_costs`, and `driver/get_fuel_types`:
    - Fetch from the network if online and update the Room DB. If offline, load the last cached data from Room to populate form dropdowns.

4. **Online-Only Enforcement & UI/UX:**
    - Fuel Request (wallet increase) and Approvals remain strictly Online-Only.
    - Monitor network connectivity. If offline, disable these specific buttons and display a Snackbar/Toast: "This action requires an active internet connection."

Please scan the repository, database, and UI files, and implement this robust sequential sync architecture step-by-step.


OFFLINE SYNC — MOBILE API NOTES

Base path: /api/v1/driver Auth: Bearer token (same as today)

WRITE ENDPOINTS (offline sync)

These 4 POST routes accept 2 new optional request fields. Response shape is unchanged.

POST /save_driver_log POST /edit_driver_log POST /save_fuel_log POST /save_other_expense

NEW OPTIONAL REQUEST FIELDS

uuid Client-generated unique ID (max 36 chars). One per offline action.

client_timestamp Epoch time when the action happened on device. Seconds OR milliseconds both work.

Send with existing fields (multipart/form-data where applicable). Both are optional — omit them and the API behaves exactly as before.

RESPONSE (unchanged)

Success (including duplicate sync):

{"message":"success"}

HTTP 200 — same body whether the row was created or already synced.

Errors unchanged: 401, 422, etc.

MOBILE BEHAVIOR

Generate uuid locally for each offline action before queueing.

On sync, send the same uuid + client_timestamp with the payload.

On retry (network timeout, app restart), resend the same uuid. If server already has it → 200 + "success" (no duplicate row).

client_timestamp = when the user performed the action offline. Check-in / fuel / expense → stored as created_at. Checkout (edit_driver_log) → stored as updated_at.

Use separate UUIDs for check-in vs checkout on the same trip segment.

CACHE ENDPOINTS (read-only, unchanged)

No new fields. Safe to cache locally.

GET /reasons Response: {"data":[{"id","value","is_calculate_on_ot"}]} No auth required.

GET /trip_types Response: {"data":[{"id","value"}]} No auth required.

GET /get_fuel_types Response: same as before (fuel type collection) Auth required.

GET /get_type_of_costs Response: {"data":[{"id","name"}]} Auth required.

SUMMARY FOR ANDROID

Request: add optional uuid + client_timestamp to the 4 write endpoints. Response: no new fields — still {"message":"success"} on 200. Idempotency: duplicate uuid = success, not error. Backward compatible: old app builds without these fields keep working.