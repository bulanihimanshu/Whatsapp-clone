# Firebase Realtime Database security review

Reviewed on 2026-10-30 against the Android source currently in this folder.

## Result and limits

**Deployed rules are unverified.** No `database.rules.json`, other Realtime Database rules file, `firebase.json`, or `.firebaserc` was found. No connected Firebase management tool is available. `google-services.json` configures the Android client; it does not contain the deployed authorization policy.

The findings below describe verified client access patterns and their implications for rule design. They do not establish that the live database is public or that an exploit has occurred. The current Console Rules contents are needed to audit the deployed policy.

No application code, database data, deployed rules, or Firebase project settings were changed. No credentials or project identifiers are included in this report. No deployable rules file was added because the private-profile and chat-authorization design must be resolved together with the client.

## Current access contract

| Path | Existing client operation | Intended access boundary |
| --- | --- | --- |
| `users/{uid}` | Registration writes the full `User` record | The authenticated user owns their record; validate the stored ID against the path |
| `users` | Contacts subscribes to the entire user collection | Directory discovery must not disclose private profile fields |
| `users/{currentUid}` | Settings reads the signed-in user's profile | Owner read |
| `users/{peerUid}` | Message sending reads a peer's name/image for previews | Read only discoverable profile data |
| `chats/{chatId}/messages` | Chat subscribes to all messages in that conversation | Only the two authorized participants |
| `chats/{chatId}/messages/{messageId}` | Chat creates a message with a client-generated push ID | Authorized participant; stored sender must match the authenticated UID |
| `chatList/{uid}` | Chats screen subscribes to the user's conversation previews | Owner read |
| `chatList/{uid}/{peerUid}` | Sending writes both participants' preview entries | Carefully validated participant writes or trusted-backend maintenance |
| `chats` | Chat calls `keepSynced(true)` on the entire collection | No collection-wide access to private conversations |

Sources in this repository: [ContactsActivity](app/src/main/java/com/example/whatsappclone/ContactsActivity.kt), [SettingsActivity](app/src/main/java/com/example/whatsappclone/SettingsActivity.kt), [RegisterActivity](app/src/main/java/com/example/whatsappclone/RegisterActivity.kt), [ChatActivity](app/src/main/java/com/example/whatsappclone/ChatActivity.kt), [ChatsFragment](app/src/main/java/com/example/whatsappclone/fragments/ChatsFragment.kt), and [models](app/src/main/java/com/example/whatsappclone/models).

## Findings

### 1. High priority: directory reads include private profile fields

`ContactsActivity` reads `users`, whose records contain `id`, `name`, `email`, `phone`, and `profileImage`. Although the UI displays only names, the snapshot delivered to an authorized client includes the other fields. A modified client can inspect them.

A parent `.read` grant covers its descendants; a deeper deny cannot hide email/phone after collection access is granted. Conversely, owner-only permissions at `users/{uid}` cannot satisfy the current full-directory request. This follows Firebase's [rule cascading behavior](https://firebase.google.com/docs/database/security/core-syntax).

Recommended follow-up: separate discoverable data into `publicProfiles/{uid}` containing only ID, display name, and avatar URL. Keep `users/{uid}` private to its owner. Update Contacts and preview lookups to read the directory; update registration to populate both paths with validated, consistent ownership. The directory can be restricted to signed-in users without exposing the private profile collection. Existing records require an explicit migration; tightening rules alone would break current directory loading.

### 2. High priority: conversation authorization lacks explicit membership

Messages store `senderId`, `text`, `timestamp`, and `id`. There is no `members` record. A deterministic chat key formed from sorted UIDs is an identifier, not proof that the requesting user is authorized. A root rule that checks only `auth != null` would permit unrelated signed-in users to access other conversations.

Recommended follow-up: introduce explicit, validated conversation participants. Restrict reads and message creation to those participants. Membership initialization must bind the correct two users to the conversation and prevent later additions or replacement by arbitrary clients. Do not allow an attacker to grant themselves access to an existing chat. Avoid substring-based UID checks against the concatenated key; they are not a general identity proof.

Existing conversations need trusted participant metadata before membership-based rules are enabled. Strict authorization should also deny reads at the full `chats` collection.

### 3. High priority: preview writes cross account boundaries

`ChatActivity.updateChatList()` writes both the sender's and recipient's preview entries. A rule allowing writes only when `auth.uid == $uid` rejects the recipient update. Broad authenticated writes would permit forged previews or tampering with unrelated users' lists.

Recommended follow-up: maintain previews with a trusted backend triggered by an accepted message, deriving participants and profile fields from authoritative data. Backend code must validate its own inputs because Admin SDK operations bypass client rules. A client-side alternative needs strict checks tying every preview to an authorized, accepted message and its exact participant pair; the current preview payload has no message ID reference for that purpose.

Do not treat client-supplied names, avatar URLs, text, or timestamps as authoritative. Plan for duplicate/out-of-order backend events so older messages cannot replace the latest preview.

### 4. Medium priority: whole-chat synchronization conflicts with private access

`keepSynced(true)` is called on `chats`, rather than the active conversation's messages. Proper participant rules should reject this broad subscription; rules will not turn a collection-wide request into a filtered list of authorized chats. A separate authorized child listener may still work, but the broad synchronization attempt is inappropriate.

Scope synchronization to the permitted conversation path as a separate client fix. Do not grant collection-wide reads to make the current call succeed. No explicit disk-persistence enablement was found, so this review does not claim persistent on-disk caching is configured.

### 5. Medium priority: denied writes can look like successful sends

`sendMessage()` does not await or handle the result of `messageRef.setValue(message)` or preview updates. The input is cleared immediately. Tightening rules could therefore reject a write while the UI appears to have sent it. The preview lookups' error callbacks do not handle message-write failures.

Recommended follow-up: surface write failures, preserve or restore unsent text, and update previews only after an accepted message (preferably through the trusted flow above). This should be included in rule-change acceptance testing.

### 6. Required policy: validate data and deliberate deletion behavior

No local rules exist to demonstrate any of the following protections. The deployed rules must be inspected for them:

- Default-deny unrecognized paths; no public root reads/writes or broad authenticated root grants.
- Private profile ownership, ID/path consistency, type checks, required fields, reasonable length bounds, and rejection of unexpected fields. If retaining a database email copy, validate it against the authenticated email claim rather than trusting arbitrary input. Collected phone numbers are not verified phone identities.
- Message membership, `senderId == auth.uid`, ID/path consistency, nonempty bounded text, numeric timestamps, and prohibited unexpected fields. Make messages immutable after creation unless explicit edit/delete features are designed.
- Server-authoritative message timestamps, or a carefully bounded timestamp policy. Current timestamps use the device clock, so clients can forge ordering; a change to server timestamps requires client work.
- Explicit delete restrictions in `.write` conditions. Firebase skips `.validate` for deleted values, so validation alone cannot prevent deletion.
- Protection of membership and preview metadata against replacement, parent-level deletion, and multi-location updates that bypass an intended narrow rule.

These validation semantics are documented in [Firebase's rule conditions guide](https://firebase.google.com/docs/database/security/rules-conditions). Exact payload limits are a product decision, not values already enforced by this app.

## Recommended authorization target

This is a design target, not the current schema or a deployable rules specification:

```text
publicProfiles/{uid}                 Discoverable name/avatar only
users/{uid}                          Owner-only private profile
chats/{chatId}/members/{uid}          Validated, immutable participant set
chats/{chatId}/messages/{messageId}   Participant reads; validated sender creates
chatList/{uid}/{peerUid}              Owner reads; trusted backend writes
```

Keep the root denied and grant access only at deliberate paths. Child conditions must not rely on a permissive ancestor being overridden. App Check and API-key restrictions may complement this design but do not replace user authorization. Firebase Storage is declared as a dependency but has no implemented upload/download operations here; its separate deployed rules were not inspected.

## Verification plan before any deployment

Use a disposable Firebase emulator dataset and authenticated test clients A, B, and unrelated C. Test actual proposed rules with the [Firebase Rules testing tools](https://firebase.google.com/docs/rules/unit-tests), including all of these cases:

| Test | Expected result after coordinated implementation |
| --- | --- |
| Unauthenticated read/write at root and every protected path | Denied |
| A reads/updates A's private profile with valid fields | Allowed according to the chosen profile policy |
| A reads B's email/phone or the full private `users` collection | Denied |
| A reads discoverable profiles | Allowed without private fields |
| A writes B's private/public profile | Denied |
| A and B read their own conversation and create messages as themselves | Allowed |
| C reads or writes A/B messages, even with a known chat ID | Denied |
| A sends with `senderId = B`, a mismatched message ID, or unexpected fields | Denied |
| Existing message edited/deleted without an implemented policy | Denied |
| Client adds itself to an existing conversation or changes membership | Denied |
| A reads B's entire chat list or writes an arbitrary preview | Denied |
| Trusted preview fan-out after a valid A-to-B message | Correct entries for both users |
| Empty/oversized messages, wrong types, and invalid timestamps | Denied under chosen bounds |
| Delete or overwrite parent `users`, `chats`, membership, or `chatList` paths | Denied |
| Atomic multi-path attempt containing one unauthorized update | Entire operation rejected |
| Network/rules reject a legitimate send | App reports failure and preserves unsent text |

The Firebase CLI and rules-unit-testing package were not found in this project; no local emulator artifacts were found at the usual user-cache location. No rule compilation, emulator security tests, live authorization probes, or deployed policy verification were performed. No rules have been published.

Next input needed for the deployed-policy review: the current Firebase Console > Realtime Database > Rules text. Project credentials and database contents are unnecessary. After reviewing that policy, implement and test schema/client/rules changes together in a separate hardening change before enabling them for real users.
