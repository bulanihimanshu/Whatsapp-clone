# Contacts and Settings implementation verification

This records verification before the later build-tool alignment. For the current toolchain and its verification status, see [BUILD_SETUP.md](BUILD_SETUP.md).

## Scope

Reviewed all existing Kotlin application classes, Firebase models and paths, adapters, navigation, the manifest, build scripts, and relevant layouts/styles. The app uses UI-owned Firebase callbacks, View Binding, and a single module; the new screens follow those conventions.

- `ContactsActivity`: listens to `users`, excludes the authenticated UID, uses database keys for recipient IDs, sorts users by name, and passes selection through the existing `ContactAdapter` to `ChatActivity` using `userId` and `userName`.
- `SettingsActivity`: listens to `users/{currentUid}`, displays name, phone, profile image, and authenticated email, and confirms sign-out before clearing the task and opening Login. It does not change profile records or authentication details.
- Both screens check authentication on start, provide toolbar Back navigation and loading/error/retry states, and remove database listeners on stop. Contacts has an empty state; Settings reports a missing profile.
- The existing main-screen chat button now opens Contacts, as does the existing New chat menu entry.
- Contact clicks ignore `RecyclerView.NO_POSITION` to avoid using a stale row position during list updates.
- Corrected an existing duplicate closing tag in `bg_message_sent.xml` discovered during verification.
- Build versions, Firebase credentials, authentication flows, and messaging schema were not changed. No Git initialization, commit, or publication was performed.

## Checks completed

- All 26 application XML files parse successfully after the drawable correction.
- All views referenced by the new Activities exist in their matching layouts.
- Every Activity declared in the manifest now has a Kotlin source file.
- New layouts reference existing string resources; string names contain no duplicates.
- Reviewed new navigation against the existing chat intent contract and model fields.

These structural checks do not replace Kotlin compilation or runtime testing.

## Build and runtime results

Verification used the existing Gradle 8.10 installation, JDK 17, offline dependency caches, and workspace-local Gradle/Android preferences. No SDK installation or build-version changes were made.

- **PASS:** `:app:compileDebugKotlin` compiled all application Kotlin, including both new Activities. Compiled `ContactsActivity.class` and `SettingsActivity.class` were produced. Existing deprecation warnings remain for `onBackPressed` and adapter positions.
- **PASS:** debug resource merging, View Binding generation, resource processing, manifest processing, dependency duplicate checks, and debug signing validation completed.
- **BLOCKED:** `assembleDebug lintDebug` stopped at `:app:compileDebugJavaWithJavac` with `AccessDeniedException` for `C:/Users/user/AppData/Local/Android/Sdk/build-tools/30.0.3/core-lambda-stubs.jar`. This happened after successful Kotlin compilation. Neither a complete APK build nor a completed lint run is claimed.
- **BLOCKED:** `adb devices` could not start the ADB server. No device/emulator UI test, Firebase read/write test, or logout test was performed.
- An existing `app/build/outputs/apk/debug/app-debug.apk` predates these changes (2025-04-29). It is not verification of this implementation and should not be used to test the new screens.

Early verification attempts encountered workspace-external cache/preferences writes and a conflicting pair of Android preference environment overrides. Those were resolved for the final attempt. Kotlin's daemon also could not write its external marker file; its fallback compiler completed successfully. New layout files were saved as UTF-8 without a BOM after the resource merger rejected their initial encoding.

Next, use Android Studio with JDK 17 and an accessible Android SDK installation to run `assembleDebug lintDebug`, then perform the manual checks below with test accounts. The present result establishes successful Kotlin compilation and resource integration, not complete runtime correctness.

## Manual acceptance checks

Use test accounts and your own local Firebase configuration. Do not make database rules public to bypass permission errors.

1. Sign in as account A. Open Contacts through both the floating chat button and the New chat menu. A should not appear; other registered users should be sorted by name.
2. Select account B. Confirm the toolbar shows B's name, send a text message, and check receipt and replies using B on a second device/emulator. Confirm both chat previews update.
3. With no other registered users in a test database, confirm the empty message appears and the screen remains usable.
4. In a controlled test backend, deny directory access. Confirm Contacts shows an error and Retry. Restore intended access and retry. Contacts reads the whole user directory, so directory permissions and profile-data privacy must be reviewed together.
5. Open Settings. Check name, authenticated email, phone, and image against A's account. Check the default image when no image URL exists.
6. Verify a missing profile and denied profile read show the appropriate message without preventing logout.
7. Navigate Back from both screens, rotate the device, and reopen them. Check for duplicate entries or crashes.
8. Cancel logout and confirm the session remains active. Then confirm logout; pressing Back must not restore the signed-in task.
9. While signed out, opening either screen should lead to Login. Sign in again and repeat navigation.

The database listener must receive a server response to establish live backend behavior. Merely compiling or opening a screen does not verify database permissions or message delivery.
