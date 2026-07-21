# SwiftRAW

[![Android CI](https://github.com/HeapJ4/SwiftRAW-project/actions/workflows/android.yml/badge.svg)](https://github.com/HeapJ4/SwiftRAW-project/actions/workflows/android.yml)
[![License: GPL-3.0-or-later](https://img.shields.io/badge/License-GPL--3.0--or--later-blue.svg)](LICENSE)
[![Android 6.0+](https://img.shields.io/badge/Android-6.0%2B-3DDC84.svg)](https://developer.android.com/about/versions/marshmallow)

SwiftRAW is a lightweight, fully offline Android gallery for viewing camera RAW files directly from an SD card, USB card reader, or local folder.

It was created mainly for quickly reviewing Nikon NEF photographs without importing an entire card or uploading anything to the cloud. SwiftRAW also recognizes RAW formats used by Canon, Sony, Fujifilm, Panasonic, Olympus, Pentax, and other camera brands.

> SwiftRAW displays camera-generated JPEG previews embedded in RAW files. It does not demosaic the original sensor data.

## Features

- Fully offline, with no account, advertisements, analytics, or internet permission
- Recursively searches a folder selected through Android's system folder picker
- Displays the largest usable JPEG preview embedded in supported RAW files
- Creates and caches thumbnails for faster browsing
- Opens photographs in a pinch-to-zoom viewer
- Displays JPEG, PNG, WebP, HEIF, TIFF, and BMP images alongside RAW files
- Exports available RAW previews as JPEG files
- Never modifies the original photographs

## RAW support

SwiftRAW currently recognizes:

- Nikon: `NEF`, `NRW`
- Canon: `CR2`, `CR3`
- Sony: `ARW`, `SR2`, `SRF`
- Fujifilm: `RAF`
- Olympus / OM System: `ORF`
- Panasonic: `RW2`, `RAW`
- Pentax: `PEF`
- Adobe and other cameras: `DNG`
- Leica: `RWL`
- Hasselblad: `3FR`, `FFF`
- Phase One: `IIQ`
- Sigma: `X3F`
- Samsung: `SRW`
- Several additional and older RAW extensions

A RAW file without a usable embedded JPEG preview may not display correctly. Preview size and quality depend on what the camera stored in the file.

## Storage usage

SwiftRAW itself is small, but its cache can grow while browsing large camera cards. Thumbnail and preview copies are stored in the app's cache so the gallery can load faster later. The original files are not duplicated or changed.

The cache can be removed safely through Android's app settings and will be recreated as needed. Exported JPEG previews use additional space and are stored separately from the originals.

## Privacy

SwiftRAW works entirely on the device. It has no internet permission and does not upload photographs, filenames, metadata, or usage information. The app can access only the folder selected through Android's Storage Access Framework.

## Build from source

Requirements:

- Android Studio
- JDK 17
- Android SDK 35

Clone the repository and open its root directory in Android Studio, or build from a terminal:

```bash
git clone https://github.com/HeapJ4/SwiftRAW-project.git
cd SwiftRAW-project
./gradlew assembleDebug
```

On Windows, run `gradlew.bat assembleDebug`. The APK is written to `app/build/outputs/apk/debug/`.

## Contributing

Issues and pull requests are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) before starting a change. Small, focused PRs are easiest to review, and changes should preserve the app's offline-first approach.

## Project notes

Some early code and documentation work was developed with AI assistance and then reviewed and tested by the maintainer. Contributions are judged by the same standard regardless of the tools used to create them: they should be understandable, testable, and useful.

## License

SwiftRAW is free software licensed under the [GNU General Public License v3.0 or later](LICENSE).
