# WhatsApp Clone for Android

A native Android learning project exploring account authentication and one-to-one text messaging with Firebase. The interface uses XML layouts, Material components, and a tabbed chat screen inspired by WhatsApp.

This is an independent portfolio project, unaffiliated with WhatsApp or Meta. Feature descriptions below are verified from the supplied source. Kotlin compilation and resource processing passed during the Contacts/Settings implementation; runtime behavior remains unverified. See [verification results](IMPLEMENTATION_VERIFICATION.md).

## Implemented in source

- Email/password registration and sign-in using Firebase Authentication, with sign-out and authentication-based navigation.
- User records containing name, email, phone number, and a profile-image URL field in Firebase Realtime Database. Phone numbers are collected as profile data; phone authentication is not implemented.
- One-to-one text message sending and live message listeners, with separate sent/received rows, timestamps, and scrolling to the latest message.
- Conversation previews updated for both participants, sorted by the latest timestamp, with an empty-state view.
- Emoji input through Vanniktech's Google emoji provider.
- Profile-image URL rendering in chat previews using Glide, with a default image fallback.
- Chats, Status, and Calls navigation using ViewPager2 and Material tabs.
- A registered-user contact directory that excludes the signed-in user and opens a conversation on selection, accessible from the menu and chat button.
- A Settings screen showing the signed-in account's name, email, phone number, and profile image, with confirmed logout. Profile editing is not implemented.
- Loading, empty/missing-profile, error, and retry states for Contacts and Settings, with lifecycle-managed Firebase listeners.

## Current limitations

`ContactsActivity` and `SettingsActivity` are implemented using the existing Firebase user schema. Contacts reads the registered-user directory rather than the phone's address book. Database rules must allow the intended directory reads; do not expose private profile data indiscriminately.

Status and Calls are placeholder screens. Search has a menu entry without search logic. Media uploads, voice/video calls, push notifications, read receipts, group chat, and end-to-end encryption are not implemented. Firebase Storage is a declared dependency without storage API usage in the supplied Kotlin source.

The build configuration is aligned to **Gradle 7.6.6**, **Android Gradle Plugin 7.4.2**, **Kotlin 1.9.10**, and **JDK 17**, with SDK Build Tools **33.0.1**. Full verification of this configuration is pending: this environment cannot download the uncached Gradle distribution because outbound build network access is denied. Earlier source compilation passed with the previous toolchain, but does not certify the current configuration. See [build setup and verification](BUILD_SETUP.md). No unit or instrumentation test source files are present.

## Tech stack

| Area | Technology in this snapshot |
| --- | --- |
| Application language | Kotlin 1.9.10; Java/JVM target 1.8 (no Java application source found) |
| Android SDK | Minimum 21, compile/target 33 |
| UI | XML layouts, View Binding, AndroidX AppCompat, RecyclerView, ConstraintLayout, ViewPager2, Material Components 1.8.0 |
| Backend | Firebase Authentication and Realtime Database; Firebase BoM 31.2.3 |
| Images and emoji | Glide 4.15.0, CircleImageView 3.1.0, emoji-google 0.15.0 |
| Build | Gradle 7.6.6, Android Gradle Plugin 7.4.2, Google Services plugin 4.3.15, JDK 17 toolchain, SDK Build Tools 33.0.1 |

## Architecture

A single `app` module uses Activities and Fragments for screens, RecyclerView adapters for lists, and Kotlin data classes for Firebase serialization. Firebase operations are called directly from UI classes through callbacks. There is no separate repository layer, dependency-injection setup, or ViewModel layer in the supplied source.

The database paths used by the client are:

```text
users/{uid}                                  User profile
chats/{chatId}/messages/{messageId}           Text messages
chatList/{uid}/{otherUid}                    Conversation previews
```

`chatId` concatenates the two user IDs in lexicographic order with a hyphen. Message records contain `senderId`, `text`, `timestamp`, and `id`. Chat previews contain `userId`, `userName`, `profileImage`, `lastMessage`, and `timestamp`.

## Local installation

1. Clone your copy of this repository and open its root directory in Android Studio.
2. Install Android SDK Platform 33 and SDK Build Tools 33.0.1 and configure the local SDK through Android Studio. Keep `local.properties` private.
3. Install JDK 17. In Android Studio's Gradle settings, select it as the Gradle JDK; for command-line builds set `JAVA_HOME` to that JDK. The build pins the compilation toolchain to Java 17 while retaining Java/Kotlin bytecode target 1.8. See [BUILD_SETUP.md](BUILD_SETUP.md).
4. Configure your own Firebase backend as described below.
5. Synchronize Gradle with network access available for the first download of Gradle and dependencies.
6. Select an emulator or device running Android 5.0/API 21 or later, and run the `app` configuration.

Build commands (with JDK 17 selected and local Firebase configuration present):

```powershell
# Windows PowerShell
.\gradlew.bat assembleDebug lintDebug
```

```sh
# macOS / Linux
sh ./gradlew assembleDebug lintDebug
```

The debug APK is normally generated under `app/build/outputs/apk/debug/`, which is ignored by Git.

## Firebase setup

1. Create your own Firebase project in the Firebase console and register an Android app with package name `com.example.whatsappclone` (the current `applicationId`).
2. Download that app's `google-services.json` into `app/google-services.json`. This file is intentionally ignored; every developer supplies their own local copy. See the official [Android setup guide](https://firebase.google.com/docs/android/setup).
3. Enable **Email/Password** as an Authentication sign-in provider.
4. Create a **Realtime Database**. The code calls `FirebaseDatabase.getInstance()` without an explicit URL; ensure the downloaded configuration identifies your intended default database.
5. Configure and test Firebase Security Rules before using real data. No database rules or backend deployment configuration are included in this snapshot. Do not enable public read/write access as a shortcut.
6. Account for the current access pattern when designing rules: registration writes the authenticated user's profile, chat sends read the peer profile, messages are shared by two participants, and preview updates write to both participants' `chatList` entries. Simply allowing every authenticated user to access every record is insufficient. The current schema has no explicit chat-membership records; authorization may require a separate schema/client change or trusted backend. Test unauthorized access and impersonation cases.
7. Firebase Storage provisioning is unnecessary for the implemented text-only flows; no upload implementation exists.

Firebase client configuration contains project identifiers and an API key rather than an administrative credential. Keeping it out of Git is a project policy, not backend access control. Follow Firebase's [API key guidance](https://firebase.google.com/docs/projects/api-keys) and enforce authorization with Security Rules. Never place Admin SDK service-account keys in this Android app.

Use disposable test accounts. Registration stores email addresses and phone numbers, so do not include database exports, personal data, or unredacted screenshots in a public repository.

## Project structure

```text
app/
  build.gradle
  proguard-rules.pro
  src/main/
    AndroidManifest.xml
    java/com/example/whatsappclone/
      ChatActivity.kt
      ContactsActivity.kt
      LoginActivity.kt
      MainActivity.kt
      RegisterActivity.kt
      SettingsActivity.kt
      adapters/         # Tabs, contacts, conversation previews, messages
      fragments/        # Chats plus Status and Calls placeholders
      models/           # User, Message, ChatListItem
    res/                # XML layouts, menus, strings, styles, images
gradle/wrapper/          # Wrapper properties and JAR (retain in Git)
build.gradle            # Project plugins and repositories
settings.gradle
gradle.properties       # Shared AndroidX settings
gradlew / gradlew.bat
.gitignore
PUBLICATION_REVIEW.md
```

## Publication status

The [Firebase security review](FIREBASE_SECURITY_REVIEW.md) records current access patterns, privacy and authorization concerns, and the tests needed before deploying stricter rules. Deployed rules remain unverified because their contents are not available locally.

See [PUBLICATION_REVIEW.md](PUBLICATION_REVIEW.md) for the credential scan, exclusions, build limitations, and review steps before the first commit. No license has been selected; choose one and confirm the rights to included images before advertising reuse permissions.
