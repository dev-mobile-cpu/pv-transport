# Releasing and production

Status: **as-built**. No HTML.  
Parked: [better-to-do](better-to-do.md#releasing-and-production).

Do not rebuild / bump `versionCode` until you say local test passed.

---

## Identifiers

| | Value |
|---|---|
| applicationId | `com.pv.transport` |
| UAT | `com.pv.transport.uat` |
| versionName | `1.6` |
| versionCode | `7` |
| minSdk | 26 |
| compile/target | 36 |

---

## Flavors

Dimension `environment`: `uat`, `production`. Each sets `BASE_URL`, `WS_URL`, `IS_UAT`, `ENV_LABEL`.

Typical outputs (local): `app/build/outputs/apk/uat/release/`, `.../production/release/`. Historical copies may sit under `builds/`.

---

## Signing and minify

Release uses a signing config in `app/build.gradle.kts` and `app/release-key.jks`. **Passwords must not be copied into docs or chat logs.** Better-to-do: move secrets to `local.properties` / CI.

`isMinifyEnabled = false`, `isShrinkResources = false`. Proguard files are present but unused until minify is on.

---

## In-app update

Not Play In-App Updates. Custom check + bottom sheet. Skip stored per version code unless force.

---

## Production checklist (when you order a release)

- [ ] Confirmed UI/UX in README
- [ ] Offline rows do not duplicate/drop on UAT
- [ ] Flavor URLs correct
- [ ] versionName / versionCode bumped **only when you say**
- [ ] Install UAT APK and production APK on a device
- [ ] Camera, QR, login, language MY/EN

Crashlytics, package rename, R8: out of scope until confirmed.
