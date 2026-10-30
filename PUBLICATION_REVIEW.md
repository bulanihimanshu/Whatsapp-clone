# GitHub publication review

Reviewed on 2026-10-30. This is a local preparation review, not a guarantee that no secrets exist.

## Changes and Git state

- The original publication pass added `README.md`, `.gitignore`, and this review without changing application logic or build configuration. The later Contacts/Settings implementation is described below and in `IMPLEMENTATION_VERIFICATION.md`. No existing files were deleted.
- Git 2.51.0.windows.1 is installed.
- The project repository is initialized on `main` with no commits. The first-commit staging review resumed on 2026-10-30; intended source, resources, build files, wrapper, and documentation are staged for local review. No commit or push was performed.
- The earlier candidate-file review used a disposable bare Git directory. The resumed review uses the actual project index. Because Git reports an ownership mismatch, review commands use a command-scoped `safe.directory` exception for this exact project path; global Git configuration is unchanged. The sandbox still reports "setup refresh had errors", so the resumed shell inspection required approved execution outside the sandbox.

## Credential review

A pattern scan inspected 246 text/configuration files, including local and generated files, for common Google/AWS/GitHub key formats, private-key headers, and literal secret/password/token assignments. Application Kotlin, XML, and Gradle source was also read to assess behavior. No hardcoded credential matches were found in application source or the files eligible for publication after applying `.gitignore`.

Matches were found in these local files; no values are reproduced here:

- `app/google-services.json`: Firebase client API key.
- `app/build/generated/res/google-services/debug/values/values.xml`: generated Firebase key resources.
- `app/build/intermediates/incremental/debug/mergeDebugResources/merger.xml`: generated resource metadata containing the Firebase key.

All three paths are ignored. `local.properties` contains a machine-specific SDK path and is ignored. No environment files, signing keystores, or service-account key files were found by the filename inventory. Ignore rules cover common variants of those credentials, IDE state, build outputs, and caches. Gradle wrapper files remain eligible for publication.

Firebase client API keys are not administrative secrets, and exclusion alone does not protect backend data; see [Firebase API key guidance](https://firebase.google.com/docs/projects/api-keys). If a genuine private credential has been shared elsewhere, revoke or rotate it; ignoring a file does not remove an earlier exposure.

The scan is heuristic. It cannot prove the absence of obfuscated credentials, inspect private backend settings, or establish image ownership. Image assets and the Gradle wrapper JAR remain publication candidates and were not treated as text credentials. Do not use force-add for ignored local configuration.

## Remaining concerns

- Firebase rules are absent locally, so deployed database permissions cannot be verified. The schema contains private profile details and message text. The follow-up [Firebase security review](FIREBASE_SECURITY_REVIEW.md) documents the access contract, privacy/authorization findings, coordinated changes needed, and security test cases.
- Messages are ordinary text records; no end-to-end encryption is implemented. Use test data for a portfolio demonstration.
- The client updates both participants' chat previews and reads peer profiles. Review authorization deliberately; owner-only preview writes would reject the current send flow, while unrestricted authenticated writes would allow tampering.
- `keepSynced(true)` is called on the whole `chats` path. Review that broad subscription when implementing backend authorization; disk persistence is not explicitly enabled in the supplied code.
- `android:allowBackup` is enabled. Review backup policy before handling real personal data. The manifest also declares external-storage read permission; its necessity and modern Android behavior have not been validated.
- Follow-up implementation added `ContactsActivity` and `SettingsActivity`, their layouts and strings, a guard for stale contact-row positions, and the main chat button's navigation. An extra closing tag in the existing sent-message drawable was also corrected after XML validation found it.
- The follow-up build change aligns Gradle 7.6.6, AGP 7.4.2, Kotlin 1.9.10, JDK 17, and SDK Build Tools 33.0.1. It removes `jcenter()`, adds the referenced ProGuard file, and explicitly declares the namespace. The final wrapper build is blocked by network permissions while downloading Gradle; see `BUILD_SETUP.md`. A successful debug build remains unverified.
- The original publication pass did not run a build or device session. Follow-up implementation verification is recorded in `IMPLEMENTATION_VERIFICATION.md`; source inspection alone does not establish working runtime behavior.
- Choose a license and verify rights to images and branding before public distribution. No license was invented or added.

## Before the first commit and upload

1. Review the README and these limitations. Prefer resolving build blockers before presenting this as a runnable portfolio application.
2. Git initialization is complete. Intended files are staged; review the actual index before creating the first commit.
3. Check the actual index with `git diff --cached --name-only`, `git diff --cached --stat`, and `git diff --cached`. Inspect any content diff locally; do not paste credential values into public issues or messages.
4. Confirm `git ls-files` contains no local Firebase configuration, `.env` files, `local.properties`, keystores, build directories, or IDE caches. Check ignore matches with `git check-ignore -v app/google-services.json local.properties`.
5. Repeat the credential scan against the staged snapshot, especially after any configuration or code changes. Ignore rules do not protect files that were already tracked or added with `--force`.
6. Obtain owner confirmation before committing. Create an empty GitHub repository, configure its remote, and obtain confirmation before pushing/publishing. No remote or GitHub account has been configured during this review.

The resumed candidate-file scan found no matches for common Google/AWS/GitHub keys, private-key headers, service-account private keys, or quoted literal password/secret/token assignments. Local Firebase configuration, SDK paths, build outputs, and IDE state were confirmed ignored. The staged snapshot is checked separately using Git; this heuristic review does not certify the absence of every possible secret. Build/runtime and asset-rights limitations above remain unresolved.
