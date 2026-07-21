# SwiftRAW

[![Android CI](https://github.com/HeapJ4/SwiftRAW-Project/actions/workflows/android.yml/badge.svg)](https://github.com/HeapJ4/SwiftRAW-Project/actions/workflows/android.yml)
[![License: GPL-3.0-or-later](https://img.shields.io/badge/License-GPL--3.0--or--later-blue.svg)](LICENSE)
[![Android 6.0+](https://img.shields.io/badge/Android-6.0%2B-3DDC84.svg)](https://developer.android.com/about/versions/marshmallow)

SwiftRAW is a private, offline Android gallery for reviewing camera RAW files directly from an SD card or USB reader. It extracts camera-generated JPEG previews, caches thumbnails, supports pinch-to-zoom viewing, and exports previews without modifying the originals.

> SwiftRAW displays embedded previews. It does not demosaic RAW sensor data.

## Features

- Recursively scans a folder chosen with Android's Storage Access Framework
- Supports common Nikon, Canon, Sony, Fujifilm, Olympus, Panasonic, Pentax and DNG formats
- Also displays JPEG, PNG, WebP, HEIF, TIFF and BMP files
- Works without network access or third-party runtime dependencies
- Caches thumbnails locally for faster browsing (may take up some space)
- Exports embedded previews as JPEG files

## Requirements

- Android 6.0 (API 23) or newer
- Android Studio with JDK 17 and Android SDK 35 for development

## Build from source

Clone the repository and open its root directory in Android Studio, then build the `app` module. From a terminal:

```bash
git clone https://github.com/HeapJ4/SwiftRAW-Project.git
cd SwiftRAW-Project
./gradlew assembleDebug
```

On Windows, run `gradlew.bat assembleDebug`. The debug APK is generated under `app/build/outputs/apk/debug/`.

## Contributing

Bug reports and pull requests are welcome. Read [CONTRIBUTING.md](CONTRIBUTING.md) before submitting changes. Please keep the application offline-first and avoid adding unnecessary dependencies or permissions.

## Privacy

SwiftRAW has no network permission. Scanning and preview extraction happen on-device, and the app reads only the folder explicitly selected by the user.

## License

SwiftRAW is licensed under the [GNU General Public License v3.0 or later](LICENSE).
