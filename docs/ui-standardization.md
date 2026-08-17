# UI standardization

Status: **as-built** (not one confirmed kit).  
See it: [docs/preview/index.html](preview/index.html).  
Parked: [better-to-do](better-to-do.md#ui-standardization).

This file is the Bootstrap-style spec: **category → specs → variants → usage in this app**. Values are from Compose objects today. `1 dp` in preview CSS = `1 px`.

Font everywhere below: **Noto Sans Myanmar**.

---

## Color (tokens in code, not unified)

Source: `ui/theme/Color.kt` plus hard-coded hex on widgets.

| Name in code | Hex | Used for |
|---|---|---|
| `colorPrimary` | `#169A5A` | Add/Save outline, selected tab text, some icons |
| `green_primary` | `#1B8E5F` | leftover |
| `iconColor` | `#1B8E50` | leftover |
| `green` | `#1FA15B` | leftover |
| `colorSecondary` | `#F1F2F6` | list page background |
| `white` | `#FFFFFF` | cards, tab chip, toast |
| `textPrimary` / `textColorPrimary` | `#212529` / `#1A1A1A` | two “primary text” greys |
| `textSecondary` / `textColorSecondary` | `#6C757D` / `#707070` | two secondary greys |
| Form label | `#495057` | `FormFieldLabel` |
| Placeholder (select) | `#BDBDBD` | `FormSelect` |
| `checkColor` | `#E53935` | **list Checkout** fill |
| Danger button | `#D32F2F` | `FormPrimaryButton` danger (logout) |
| Login field fill | `#176B43` | `CustomTextField` |
| Login submit | `#D9D9D9` / text `#1E7D4E` | Login `Button` |
| Material theme | purple 40/80 | `PVTransportTheme` — screens mostly ignore it |

Soft primary fill: `#169A5A` at 10% (`#1A169A5A`).

---

## Typography

`Type.kt` only sets Material `bodyLarge` 16 / 24. Screens pick sizes ad hoc.

| Role | Size | Weight | Where |
|---|---|---|---|
| Page title | 18–20 sp (NetworkAwarePageTitle) | SemiBold | Log / Fuel / Approval headers |
| Card title | 16 sp | SemiBold | Daily Log type line |
| Card subtitle | 13 sp | Normal | reason / trip type |
| KM value | 18 sp | Bold | start/end km |
| Meta (date/time) | 13 sp | Normal | card row |
| Field label | 14 sp / line 18 | Normal | `FormFieldLabel` |
| Input value | 16 sp | Normal | `FormSelect`, fuel field |
| Tab (segmented) | 13 sp | SemiBold selected / Medium idle | `SegmentedTabs` |
| Bottom nav label | 10 sp | default | `MainBottomBar` |
| Add button | 13 sp | SemiBold | `AddActionButton` |
| Save button | 15 sp | SemiBold | `FormPrimaryButton` |
| Badge | 11 sp | SemiBold | `StatusBadge` |
| Toast | 14 sp | — | `AppToast` |
| Detail section title | 12 sp | SemiBold | `DetailSectionCard` (green) |
| Detail item | 14 sp | label normal / value SemiBold | `DetailItem` |

---

## Spacing and radius (as-built)

| Token | Value |
|---|---|
| List content padding / card gap | 16 dp |
| Card inner padding | 16 dp (log card top 18) |
| Filter inner | 16 dp, 20 dp before date row |
| Filter/card radius | 16 dp |
| Button radius Add/Save | 16 dp |
| List Checkout radius | **12 dp** |
| Select / photo slot | 12 dp |
| Fuel text field | 8 dp |
| Badge | 8 dp |
| Toast | 12 dp |
| Space scale (requested, not in code) | 4 / 8 / 12 / 16 — see better-to-do |

---

## Buttons

### Compact add — `AddActionButton`

| Spec | Value |
|---|---|
| Height | 40 dp |
| Radius | 16 dp |
| Border | 1 dp `#169A5A` |
| Background | `#169A5A` 10% |
| Content | `#169A5A` SemiBold 13 sp, icon 18 dp, gap 6, padding 14×8 |

**Usage:** Add Log, Add Log Sheet (hidden), Add Fuel / Add Log on Fuel tab, Add Request, Add on Expenses.

**Also same language:** Generate QR on Approval (OutlinedButton copying Add colors; disabled grey fill `#F1F2F6`).

### Block primary — `FormPrimaryButton`

| Spec | Value |
|---|---|
| Height | 50 dp |
| Radius | 16 dp |
| Border | 1 dp |
| Background / content | primary 10% / `#169A5A` or danger `#D32F2F` |
| Text | 15 sp SemiBold, icon 20, gap 8 |
| Loading | `DotsLoading` 7 dp dots (not a circle) |
| Bottom spacer | 16 dp |

**Usage:** Save on Check-in, Check-out **form**, Add Fuel Log, Add Request, Add/Update Expense, Logsheet save. Logout = danger tone. Approval dialog **Approve** and **Continue** copy these specs. Check-out **form** Save matches this. **List** Checkout does not.

### List Checkout — one-off filled

| Spec | Value |
|---|---|
| Height | 52 dp |
| Radius | 12 dp |
| Fill | `#E53935` (`checkColor`) |
| Text | white 15 sp |

**Usage:** Daily Log card when trip still open (`isCheckout == "true"`).

### Login submit — one-off filled

Height 56, radius 16, fill `#D9D9D9`, text `#1E7D4E`, elevation 6. Loading: dots.

### Modal Close / Cancel

Height 50, radius 16, white, text `textPrimary`, border `#E0E0E0`. **Usage:** Generate QR dialog Close; user-picker Cancel.

### Profile logout

`FormPrimaryButton` danger.

There is **not** one button component for all of the above. That is the UI job in better-to-do.

---

## Inputs

### Login — `CustomTextField`

Height 60, radius 16, fill `#176B43`, white text/placeholder, no indicator. **Only login.**

### Form select — `FormSelect` / `FormSearchSelect`

Height 50, radius 12, white, border `#E0E0E0` 1 dp, padding H 16, value 16 sp black, placeholder `#BDBDBD`, chevron `#757575` 24 dp. Selected row tint `#14169A5A`. **Usage:** trip type, reason, cost type, corporate user search, etc.

### Fuel-style field — `CustomFuelTextField`

Height 56, radius 8, white, padding H 12, 16 sp black. Digit filter when numeric. **Usage:** fuel amounts / km on fuel forms. Not the same as select.

### Labels — `FormFieldLabel`

Icon 18, gap 6, 14/18 sp, `#495057`. **Usage:** form fields including photo captions.

Field errors: some under the field, some Toast-only (login blank uses system Toast).

---

## Tabs

### Segmented — `SegmentedTabs`

Track `#E9EAEF`, radius 16, padding 4. Chip radius 12, vertical padding 10. Selected: white + `#169A5A` SemiBold 13. Idle: `#8A8F98` Medium 13.

**Usage:** Daily Log (Log only while logsheet off), Fuel (Request / Log / Wallet or Log / Wallet).

### Bottom — `MainBottomBar`

White, 1 dp `#EEEEEE` top rule, icon + 10 sp label, selected `#169A5A`, idle grey. Not Material `NavigationBar` items (custom Row).

Approval inner tabs (QR vs PIN) are local to the dialog, not `SegmentedTabs`.

---

## Cards

White, radius 16, elevation 0. List gap 16. Daily Log card: type + subtitle, badge top-end, date/time row, start/end km, two photo slots 104 dp, optional Checkout.

Filter card: same card chrome, title “Filters” + icon, Start/End date pickers on `colorSecondary` fields.

Detail: `DetailSectionCard` + `DetailItem` + `DetailPhotoThumbnail` 110 dp radius 12.

---

## Badges — `StatusBadge`

Radius 8, padding 10×5, 11 sp SemiBold.

| Status | Background | Text |
|---|---|---|
| OFFLINE | `#F5F5F5` | `#757575` |
| SYNCING | `#E3F2FD` | `#1976D2` + spinning icon |
| PENDING | `#FEF7E0` | `#B06000` |
| APPROVED | `#E6F4EA` | `#137333` |
| REJECTED | `#FFEBEE` | `#C62828` |

---

## Messages — `AppToast`

System Toast + custom white view: text `#212529` 14, padding 16×12, radius 12, border `#E8E8E8`, gravity bottom + 72 dp. Same look for success and error (message string only). Login still uses `Toast.makeText` in places. **Does not use brand green.**

---

## Loading

| Where | Widget |
|---|---|
| Daily Log / Fuel lists, Save button, Approval list (many paths) | `DotsLoading` (Telegram-style 3 dots) |
| Login button | dots |
| Several forms / Wallet / some screens | `CircularProgressIndicator` |

Not one loader.

---

## Photo

| Piece | Spec | Usage |
|---|---|---|
| `LogKmPhotoSlot` | grey `#E6E7EC`, radius 12, height 104, thumb decode | list start/end km |
| `DetailPhotoThumbnail` | 110 dp, radius 12, `#EEEEEE` empty | details |
| Pickers | `CustomImagePicker`, `CustomImagePickerBox`, `CustomMultipleImagePicker`, `EditMultipleImagePicker` + bottom sheet | forms — four implementations |

Odometer vs proof layout is per screen, not one `AppPhotoField`.

---

## Offline banner

`OfflineBanner`: fill `#E57373`, radius 12, white 13 sp. Defined on LogScreen; not the same as `AppToast`.
