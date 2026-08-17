# Testing

Status: **as-built = almost none**. No HTML.  
Parked plan: [better-to-do](better-to-do.md#testing).

Unit tests are a **feature** for this repo (especially cloud agents). They do not replace device QA.

---

## What exists

| Type | File | What it does |
|---|---|---|
| JVM unit | `app/src/test/.../ExampleUnitTest.kt` | `2+2=4` |
| Instrumented | `app/src/androidTest/.../ExampleInstrumentedTest.kt` | template |

`./gradlew test` is green because it tests nothing about the app.

---

## Definitions we will use

| Kind | Means here | Runs where |
|---|---|---|
| **Unit** (white-box-ish) | Pure Kotlin: validation, mapping, date/sort, reconnect policy, debounce | JVM / cloud VM |
| **White box** | Tests that know internals (ViewModel, merge function) | JVM if extracted; otherwise instrumented |
| **Black box** | Driver-like: tap, camera, airplane, slide feel, card density | Your emulator/phone only |
| **Screenshot** (optional) | Paparazzi/Roborazzi of kit composables | JVM, no emulator — parked |

Cloud agents can own **unit** tests. They cannot honestly own black-box UI.

---

## First tests worth adding (not confirmed)

- KM / numeric / end ≥ start
- Empty plate must not display `"—"` if that becomes a rule
- Date range + sort comparators (when filter dialog exists)
- Refetch only on `Available`
- `safeNavigate` debounce if extracted

Do **not** rewrite offline merge only to test it. If a pure merge function is extracted later without behavior change, then unit-test that.

---

## Agent rule (when you confirm testing)

1. Change a rule → add/extend a test.
2. `./gradlew test`
3. Fix code, not the test (unless the test was wrong).
4. No device, network, or camera in unit tests.
