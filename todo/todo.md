# Project Planning - PV Transport

## 1. Fuel Request & Reimbursement (Current Priority)
Workflow: 
- **Normal:** Fuel Request -> Approved -> Wallet Balance (+) -> Fuel Log -> Wallet (-)
- **Emergency (Due):** Fuel Log (own money) -> Creates due amount -> Due Request (reimbursement) -> Approved.

### Phases:
- **Phase 1: Data Models & API Update** ✅
    - Updated `FuelRequest` model with `request_category`.
    - Updated `FuelApi` to use Multipart for `saveFundRequest` to support proof files.
- **Phase 2: UI Development (Conditional Fields)** ✅
    - Implement Category Selector (Fuel Request / Due Request) in `AddFuelRequestScreen`.
    - Fuel Request: Show `fuel_type_id`, `request_type`.
    - Due Request: Show `files` (Camera only) upload UI.
    - **Smoothness:** Use `AnimatedVisibility` for transitions.
    - **Validation:** Block `due_request` if `due_amount` is 0.
- **Phase 3: Multiple Files Selection & Optimization** ✅
    - Implement multi-file picker (Camera only - reference from Other Expenses).
    - **Performance:** Add image compression (1024px & JPEG compression) before submission.
- **Phase 4: Submission Logic & Validation** ✅
    - Handle Multipart construction based on selected category.
    - Added Amount Validation (cannot exceed due amount).
    - Implemented Submission Confirmation Dialog.
    - Defaulted `request_type` to "cash".
- **Phase 5: Final Testing & UI Polish**
    - Verification of the full reimbursement workflow.
    - Minor UI adjustments for consistent user experience.

## 2. Approval System Modification
Upgrade QR verification with PIN and Digital Signature.

- **PIN UI:** 4-digit PIN with auto-focus and auto-submit.
- **E-Signature:** 2:1 fixed aspect ratio canvas with image compression.
- **API Integration:** Update verification API with PIN and signature.

## 3. Offline Mode Fixes
*Note: Only for Daily Log, Checkout, Add Fuel Log, and Add Expense.*
- Fix sync issues and add sync status indicators.
