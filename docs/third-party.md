# Third party

Status: **as-built**. No HTML kit (not visual).  
Parked plugins: [better-to-do](better-to-do.md#third-party).

How this app uses libraries: **where / for what / how**. Versions: see `gradle/libs.versions.toml` and `app/build.gradle.kts`.

---

## In use

| Library | Where | For what | How |
|---|---|---|---|
| **Hilt** | `di/`, ViewModels, `SyncWorker` | DI | `@HiltViewModel`, `@Inject`, `hiltViewModel` / `activityHiltViewModel` so Fuel/Log share the activity VM |
| **Retrofit + OkHttp** | `api/`, `di/NetworkModule` | REST | `BASE_URL` per flavor. Logging interceptor. `okhttp-profiler` in dependencies |
| **Gson** | APIs, `WebSocketManager` | JSON | Socket payload → `SocketResponse` |
| **Coil** | `CachedAppImage`, pickers | Images | Thumb decode on lists (`thumbDecode = true`) |
| **Room** | `local/` | Offline cache + pending writes | DAOs for logs, fuel, expense, masters |
| **WorkManager + Hilt Work** | `worker/SyncWorker`, `AuthRepository` | Upload when back online | Unique work enqueue after local save |
| **Socket.IO** (`io.socket.client`) | `network/WebSocketManager` | Realtime QR approval | Connect `BuildConfig.WS_URL`. Event **`qr-verify-approve`**. `reconnection = true`. Used from Approval QR flow (`generateQRViewModel` / clear socket on new QR) |
| **Navigation Compose** | `AppNavigation`, *NavHost files | Routes | Nested hosts per module + custom tab shell |
| **DataStore / Shared prefs** | `AuthPrefs` | Session, language, skipped version | Login persistence |
| **Camera / file providers** | image pickers | Odometer and proof photos | URI → compress → Room path / multipart |
| **QR bitmap** | `ApprovalScreen.generateQrBitmap` | Show QR to corporate user | Generated on device, not a Maps SDK |

Realtime today is **QR verify approve only**, not a general push-notification product. No FCM / Notifee in the tree from this pass.

---

## Flavors and backends

| Flavor | API | WebSocket |
|---|---|---|
| `uat` | `https://uat.pvmyanmar.com/api/v1/` | `https://uat.pvmyanmar.com` |
| `production` | `https://pvmyanmar.com/api/v1/` | `https://pvmyanmar.com` |

Application id: `com.pv.transport` ; UAT adds `.uat`.

---

## Version check

`CheckVersionViewModel` + API. If `latestVersionCode > VERSION_CODE`, show `UpdateVersionBottomSheet`. Force update cannot dismiss. Optional skip stored in `AuthPrefs`.

---

## Not in app (parked)

Firebase Crashlytics, Play in-app updates besides the custom sheet, analytics, maps, payment SDKs. Add only via better-to-do confirm.

Do not document keystore passwords. Signing exists in Gradle; treat secrets as a release-docs problem (better-to-do).
