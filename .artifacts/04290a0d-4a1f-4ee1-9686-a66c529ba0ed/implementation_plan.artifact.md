# Auto-fill and Suggestions for DVBS Registration

The user wants to add an autocomplete feature to the child's name field in the DVBS registration screen. When a name is typed, matching names from previous registrations should be suggested. Selecting a suggestion should automatically fill in the child's age, grade, parent details, and gender.

## User Review Required

> [!NOTE]
> The auto-fill logic will use the most recent registration record for a child with the matching name to populate the fields. If multiple children have the same name, the user may need to manually verify the details.

## Proposed Changes

### UI Layer

#### [MODIFY] [DVBSRegistrationEntryScreen.kt](file:///D:/Android/Sdk/RESOURCE-/app/src/main/java/com/roy/ngong/ui/dvbs/DVBSRegistrationEntryScreen.kt)
- Add state `nameSuggestionsExpanded` to control the visibility of the suggestions dropdown.
- Add logic to filter unique child names from the existing `registrations` list provided by the `DVBSViewModel`.
- Wrap the `childName` text field in an `ExposedDropdownMenuBox`.
- Update the `onValueChange` of `childName` to show suggestions as the user types.
- Implement a click listener for suggestion items that populates:
    - `ageString`
    - `gradeClass`
    - `parentGuardianName`
    - `parentGuardianPhone`
    - `selectedGender`
- Add a "Clear" button to the child name field to easily reset the form if needed.

## Verification Plan

### Automated Tests
- I will verify the build and check for any compilation errors.

### Manual Verification
1.  Open the DVBS Registration screen.
2.  Type a name that exists in previous registrations (e.g., if "John Doe" was registered on Day 1, type "Joh").
3.  Verify that a dropdown appears with "John Doe".
4.  Select "John Doe" and verify that the Age, Grade, Parent Name, Parent Phone, and Gender fields are automatically populated.
5.  Verify that you can still manually edit the fields if needed.
6.  Save the registration and verify it works for Day 2.
