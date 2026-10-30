# Build toolchain

## Configuration

| Component | Selected version |
| --- | --- |
| Gradle distribution | 7.6.6, pinned by the wrapper with the official SHA-256 checksum |
| Android Gradle Plugin (AGP) | 7.4.2 |
| Kotlin Gradle Plugin | 1.9.10 |
| Build JVM and compilation toolchain | JDK 17 |
| Java and Kotlin bytecode target | 1.8 |
| SDK Platform / compile SDK | 33 |
| SDK Build Tools | 33.0.1 |
| Minimum / target Android API | 21 / 33 |

The previous Gradle 8.10/Kotlin 1.7.10 combination was outside Kotlin's documented tested range. This change keeps AGP in the existing 7.4 release family and selects the compatible Gradle 7.6/Kotlin 1.9.10 families, including the Gradle 7.6.6 patch fixes. It does not attempt an AGP 8/9 migration or change Firebase dependencies or application behavior.

AGP 7.4 requires Gradle 7.5 or later and JDK 11 or later. JDK 17 is the project-selected runtime and compiler. Kotlin's table lists 1.9.0–1.9.10 with Gradle through the 7.6 family and AGP through the 7.4 family; the selected Gradle/AGP versions include maintenance patches. The Kotlin plugin now supplies its matching standard library, avoiding the old explicit 1.7.10 dependency.

Sources: [AGP 7.4 compatibility](https://developer.android.com/build/releases/agp-7-4-0-release-notes), [Kotlin plugin compatibility](https://kotlinlang.org/docs/gradle-configure-project.html), [Gradle 7.6.6 release notes](https://docs.gradle.org/7.6.6/release-notes.html), [official distribution checksums](https://gradle.org/release-checksums/).

SDK Build Tools 33.0.1 is explicitly selected instead of AGP's older default 30.0.3, whose lambda-stub JAR failed with an access error during earlier verification. The newer selection still needs a successful build to establish whether that environment issue is resolved.

The namespace moved from the manifest to `app/build.gradle` with the same value. The application ID is unchanged. The referenced `app/proguard-rules.pro` now exists; minification remains disabled. Repositories use Google Maven and Maven Central.

## Local setup

1. Install JDK 17 and Android SDK Platform 33 plus SDK Build Tools 33.0.1 using Android Studio's SDK Manager.
2. In Android Studio, open Settings > Build, Execution, Deployment > Build Tools > Gradle and select JDK 17 for the Gradle JDK. Do not select the installed JDK 23 for this Gradle release.
3. For terminal builds, set `JAVA_HOME` to your JDK 17 installation. `java -version` can differ if PATH points at an old JRE; the wrapper prioritizes `JAVA_HOME`.
4. Keep the SDK path in ignored `local.properties`, and put your Firebase client configuration in ignored `app/google-services.json`.
5. Allow the first Gradle/dependency download. Offline mode works only after the selected distribution and all dependencies are cached.

Example for the existing Windows installation (adjust the JDK path on another machine):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
.\gradlew.bat --version
.\gradlew.bat assembleDebug lintDebug
```

The existing Android Studio project uses `#GRADLE_LOCAL_JAVA_HOME`. Its ignored `.gradle/config.properties` was updated to the installed JDK 17. That absolute path is local configuration and must remain out of Git. Other developers should select their own installed JDK 17. No machine-specific JDK path was added to shared Gradle properties.

## Verification result

- Confirmed the installed JDK reports Java 17.0.12 and Build Tools 33.0.1 is present.
- Checked that the wrapper URL and official binary distribution checksum both refer to 7.6.6, Kotlin is pinned once, and Java/Kotlin bytecode targets both remain 1.8.
- Attempted the actual project wrapper command `assembleDebug lintDebug` using JDK 17 and workspace-local caches/preferences.
- **Blocked before project configuration:** downloading `gradle-7.6.6-bin.zip` failed with `java.net.SocketException: Permission denied`. Only Gradle 8.10 is currently cached, so it cannot verify the final configuration offline.
- No complete build, lint result, or runtime success is claimed for this toolchain. Earlier Kotlin/resource checks documented in `IMPLEMENTATION_VERIFICATION.md` used the previous toolchain.

Run the commands above outside the network-restricted execution environment to complete verification. If the SDK access error recurs with Build Tools 33.0.1, repair/reinstall that SDK component through SDK Manager and verify its local permissions. No SDK files or permissions were modified during this change.
