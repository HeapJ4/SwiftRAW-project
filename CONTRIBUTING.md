# Contributing to SwiftRAW

Thanks for helping improve SwiftRAW.

## Development workflow

1. Fork the repository and create a focused branch from `main`.
2. Make one logical change at a time.
3. Build the debug APK with `./gradlew assembleDebug` (`gradlew.bat assembleDebug` on Windows).
4. Test folder selection, RAW preview extraction, image viewing and export on a supported Android device.
5. Open a pull request describing the change and how it was tested.

## Project principles

- Preserve offline operation and user privacy.
- Do not add network permissions, analytics or tracking.
- Prefer Android platform APIs over new dependencies.
- Never commit signing keys, credentials, local SDK paths, APKs or build output.
- Keep changes compatible with the minimum supported Android version.

## Reporting bugs

Include the Android version, device model, file format, steps to reproduce and relevant logs. Do not upload private photographs unless you intentionally created a safe test file.
