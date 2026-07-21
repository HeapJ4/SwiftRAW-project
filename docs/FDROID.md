# F-Droid readiness

SwiftRAW is a good candidate for F-Droid: its source is GPL-3.0-or-later, it has no network permission, and it currently uses only Android platform APIs.

## Before submitting

- Choose a unique, stable application ID and keep it unchanged after publication.
- Ensure every release has a monotonically increasing `versionCode` and a matching `versionName`.
- Tag the exact source used for each release, for example `v2.0.2`.
- Confirm `./gradlew assembleRelease` builds from a clean checkout without proprietary repositories or dependencies.
- Add store graphics, screenshots and localized descriptions where appropriate.
- Submit the application metadata to the official `fdroiddata` repository.

## Current APK discrepancy

The supplied `SwiftRAW.apk` is signed and identifies as version `2.0.2`, version code `23`, with minimum API level `26`. The v2.0.2 source currently declares version code `202` and minimum API level `23`.

Do not present that APK as a reproducible build of the current source until these values are reconciled. F-Droid builds applications from tagged source and will use the metadata declared by that source.

## Signing

Never commit a signing keystore or its passwords. F-Droid normally signs its own builds. If a separately distributed APK is maintained, preserve its signing key securely so Android can verify future updates.
