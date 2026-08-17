# Code structure and performance

Status: **as-built**. Not visual.  
Parked refactors: [better-to-do](better-to-do.md#code-structure-and-performance).

This is how Kotlin is laid out, and what already helps (or hurts) Compose performance. User-felt speed is [app-performance.md](app-performance.md).

---

## Packages

| Package | Job |
|---|---|
| `presentation` | Screens (31 files): lists, forms, details, login, splash |
| `viewmodels` | UI state, paging, reconnect |
| `repository` | API + Room merge (offline) |
| `api` | Retrofit interfaces |
| `data` | DTOs, socket models |
| `local` | Room DB, entities, DAOs |
| `worker` | `SyncWorker` |
| `network` | Connectivity, interceptors, Socket.IO |
| `auth` | `AuthPrefs` |
| `di` | Hilt modules |
| `ui/theme` | Color, type, shared widgets |
| `extension` | Nav hosts, pickers, bottom bar, transitions, `safeNavigate` |

There is no `ui/components` package yet. Shared UI sits in `ui/theme` and `extension`.

---

## Shared vs copied (current)

**Shared:** `AddActionButton`, `FormPrimaryButton`, `SegmentedTabs`, `FormFieldLabel`, `FormSelect`, `StatusBadge`, `AppToast`, `DotsLoading`, `DetailSectionCard`, `LogKmPhotoSlot`, `CachedAppImage`, `NavTransitions`, collapsible chrome, `NetworkAwarePageTitle`.

**Still copied / parallel:** four image pickers; list Checkout filled button; Approval/QR OutlinedButtons that only *copy* Add/Save specs; `CustomFuelTextField` vs select; login `CustomTextField`; Material purple theme unused; two text-grey pairs.

Extract **after** UI confirm. Extra wrappers that recompose parents make lists slower.

---

## Compose performance (as-built)

| Practice | State |
|---|---|
| LazyColumn `key` | Daily Log uses `stableKey` |
| List images | `LogKmPhotoSlot` + thumb pixels, not full bitmap |
| `contentType` | `"driver_log"` on log items |
| Click debounce | `safeNavigate`; Add Log 1 s guard |
| Nested scroll | chrome observes, does not consume |
| Stable button params | defaults objects exist; many screens still pass inline lambdas |
| Theme skip | hardcoded colors vs `MaterialTheme` purple |

---

## Offline architecture (do not casually rewrite)

Local-first write → Room row OFFLINE/SYNCING → `WorkManager` upload → merge with server list in repository/ViewModel. Duplicate/drop bugs are stop-the-line. Performance work must not “simplify” merge.

---

## Build performance (not this pass)

R8 / minify off. Compose compiler reports not in CI. Baseline profiles appear in release APK output but are not a documented product step yet.
