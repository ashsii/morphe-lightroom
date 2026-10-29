# Lightroom patches for Morphe

Personal patches for **Lightroom for Android 9.3.0**, compatible with
[Morphe](https://morphe.software/). This is an independent personal fork of the
[Morphe patches template](https://github.com/MorpheApp/morphe-patches-template),
not an official Adobe or Morphe project.

Source: [ashsii/morphe-lightroom](https://github.com/ashsii/morphe-lightroom).

## Supported app

| App | Package | Version | Version code | Architecture |
| --- | --- | --- | --- | --- |
| Lightroom | `com.adobe.lrmobile` | `9.3.0` | `710903001` | `arm64-v8a` |

Other versions and architectures have not been validated. Bring your own APK;
this repository contains patch source only.

## Patches

<!-- PATCHES_START EXPANDED -->

| Patch | Behavior | Enabled by default |
| --- | --- | --- |
| Disable forced update | Disables the update check that blocks startup and imports. | Yes |
| Clone Lightroom | Uses `com.adobe.lrmobile.morphe` for a separate installation. | No |
| Keep cloud sync paused | Preserves the user pause and prevents resuming cloud photo sync. Also supports the clone package. | No |

<!-- PATCHES_END -->

See [the Lightroom guide](LIGHTROOM.md) for implementation details, application
commands, recorded APK checks, and device-testing limitations.

## Build

Install **JDK 21**. An Android SDK is not required. Dependencies are downloaded
from GitHub Packages; configure a GitHub account and token with `read:packages`
in your **user-level** Gradle properties file (`~/.gradle/gradle.properties`):

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_PACKAGES_TOKEN
```

Keep credentials outside this repository. GitHub CLI login and Gradle package
credentials are configured separately.

From the repository root:

```powershell
# Windows PowerShell
.\gradlew.bat buildAndroid --console=plain
```

```sh
# Linux / macOS
./gradlew buildAndroid --console=plain
```

The bundle is written to `patches/build/libs/patches-<version>.mpp`. Apply it
with [Morphe Desktop](https://github.com/MorpheApp/morphe-desktop) or a compatible
Morphe client. Only enable the optional patches when you need their behavior.

Patching re-signs the APK. Preserve local photos and edits before replacing an
existing installation. Startup, sign-in, imports, exports, and cloud behavior
still need testing on a device; support for online services is not guaranteed.

## Development and releases

Work on `dev`; merge into `main` for stable releases. The inherited
[release workflow](.github/workflows/release.yml) uses semantic-release:
`feat:` and `fix:` commits create releases, while `chore:` commits do not.
The workflow also verifies compilation when a release is not needed.

Release automation generates `CHANGELOG.md`, `patches-list.json`,
`patches-bundle.json`, and the patch table above. Do not edit generated release
metadata by hand or upload release bundles manually. The initial source fork
does not include a published patch release.

See [contributing](CONTRIBUTING.md) for checks and release setup.

## License and attribution

Distributed under [GPL-3.0](LICENSE), with the upstream [NOTICE](NOTICE)
preserved. Adobe Lightroom and Morphe names identify the applications this
personal patch bundle works with; their owners do not endorse this project.
