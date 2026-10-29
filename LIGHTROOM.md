# Lightroom 9.3.0 patch guide

This bundle includes forced-update removal, an optional clone package, and an
optional permanent cloud-sync pause. The forced-update patch makes Lightroom's
update predicate return `false` for the startup and import checks that use it.

## Target

- Package: `com.adobe.lrmobile`
- Version: `9.3.0`
- Version code: `710903001`
- Architecture: `arm64-v8a`
- Inspected original APK SHA-256:
  `fbbb271fc8ee59cbef6bca1c8e3572865cd845a26281f7bfd4535876d85c50a0`

## How it works

The resource `force_upgrade_msg` has ID `0x7f130536` in the inspected APK.
The dialog builder is `Lbf/p;->f(Landroid/content/Context;)Lcom/adobe/lrmobile/material/customviews/f0;`.
This class logs as `ForceUpgradeUtil`.

Its `i()V` method computes an update-required flag using persisted minimum-version
and denylist data. The side-effect-free `j()Z` getter exposes that flag.
`StorageCheckActivity.T2()` shows the blocking dialog and returns early when
`j()` returns `true`; import paths also use the same getter.

The patch finds the policy class through its preference and analytics strings,
then finds the two-instruction static boolean getter in that class. It requires
exactly one match for each fingerprint and makes that getter return `false`.
Obfuscated class and method names are documented here, rather than used as
fingerprint criteria.

See [fingerprints](patches/src/main/kotlin/app/lightroompatches/patches/forceupgrade/Fingerprints.kt)
and [patch implementation](patches/src/main/kotlin/app/lightroompatches/patches/forceupgrade/DisableForcedUpdatePatch.kt).

## Build and apply

Use JDK 21 and GitHub Packages credentials with `read:packages` in your user Gradle
configuration. The unused template Android extension has been removed, so this
bundle does not require Android Studio or an Android SDK.

From this repository, in PowerShell:

```powershell
.\gradlew.bat buildAndroid --console=plain
```

The bundle is written to `patches/build/libs/patches-<version>.mpp`.
Download [Morphe Desktop](https://github.com/MorpheApp/morphe-desktop/releases)
separately. The examples below assume its JAR is in your Downloads folder and
your original APK is at `apks/lightroom-9.3.0-original.apk`. Adjust those paths
to match your files. APKs and downloaded tools are not included in this repo.

Run all examples from the repository root in the same PowerShell session:

```powershell
New-Item -ItemType Directory -Force .\apks | Out-Null
$morpheDesktop = "$env:USERPROFILE\Downloads\morphe-desktop.jar"
$bundle = (Get-ChildItem .\patches\build\libs\patches-*.mpp |
    Sort-Object LastWriteTime -Descending | Select-Object -First 1).FullName
$originalApk = '.\apks\lightroom-9.3.0-original.apk'

java -jar $morpheDesktop patch `
    -p $bundle `
    --exclusive -e 'Disable forced update' --validate --bytecode-mode FULL `
    --out .\apks\lightroom-9.3.0-no-forced-update.apk `
    $originalApk
```

The output APK is re-signed. Android may reject installing it over an existing
Adobe-signed installation. Uninstalling an existing installation removes its
local app data, so preserve local photos and edits before replacing it.

## Validation

The following checks were recorded during development against the inspected
APK above. Re-run them when changing patch logic or using a different input.

- The bundle builds successfully without an Android SDK.
- Morphe applies the patch to the inspected original and reports one changed class.
- Inspecting the output with JADX confirms `bf.p.j()` returns `false`.
- All 6,508 original non-DEX, non-signature file entries retain identical contents.
- The output APK's v2 signature passes verification with Android's APK signature library.

Device testing is still required. Check startup with network access enabled,
relaunch the app, and try importing and editing a photo. The local predicate
change does not establish whether any particular online service supports this
old Lightroom version.

## Optional clone

The `Clone Lightroom` resource patch creates a separate installation with the
package name `com.adobe.lrmobile.morphe`. It updates all eight provider
authorities, the app's custom receiver permission and its uses, and explicit
task affinities. Application and component classes keep their original names.

To clone the already patched APK, continue in the same PowerShell session:

```powershell
java -jar $morpheDesktop patch `
    -p $bundle `
    --exclusive -e 'Clone Lightroom' --validate `
    --out .\apks\lightroom-9.3.0-morphe-clone.apk `
    .\apks\lightroom-9.3.0-no-forced-update.apk
```

The clone's five DEX files and 31 native libraries are byte-identical to the
patched input, so it retains the forced-update fix. Its APK v2 signature
verifies. The final manifest has no provider authority conflicts with the
original package, and the original application and launcher classes are
preserved. Startup, login, imports, and sharing still need device testing.

## Keep cloud sync paused

The optional `Keep cloud sync paused` patch supports the original package and
`com.adobe.lrmobile.morphe` at the version and architecture listed above.
It changes the sync controls, preserving the normal pause mechanism and its
notifications:

1. The `syncPausedByUser` restoration path always takes its true branch, even if
   an existing saved preference was false. This persists the user pause, records
   its reason, and invokes the real cloud-library pause command.
2. Requests to release `TI_SYNC_PAUSE_USER`, including Resume Syncing, return
   false. Other pause reasons still perform their normal storage/HDR cleanup.
3. The cloud library's `setSyncEnabled` wrapper always receives false.
4. Native `catalogModel` creation receives false for its sync-enabled argument,
   so the engine does not start enabled before pause restoration completes.

The inspected methods are `je.p.c()Z`, `je.p.q(je.p$d)Z`,
`com.adobe.lrmobile.thfoundation.library.c0.c2(Z)V`, and
`com.adobe.lrmobile.thfoundation.library.c0.u2(String,Z,o0)V`.
The patch locates them by strings, signatures, and class relationships, and
requires exactly one match for each.

See [sync fingerprints](patches/src/main/kotlin/app/lightroompatches/patches/sync/Fingerprints.kt)
and [sync patch](patches/src/main/kotlin/app/lightroompatches/patches/sync/KeepCloudSyncPausedPatch.kt).

After building the bundle and creating the clone, continue in the same session:

```powershell
java -jar $morpheDesktop patch `
    -p $bundle `
    --exclusive -e 'Keep cloud sync paused' --validate --bytecode-mode FULL `
    --out .\apks\lightroom-9.3.0-morphe-sync-paused.apk `
    .\apks\lightroom-9.3.0-morphe-clone.apk
```

Validation of this output:

- The bundle builds and all four fingerprints resolve uniquely.
- Morphe reports two changed classes. Inspection of the output confirms all four
  sync hooks and the existing forced-update override.
- All 6,508 original non-DEX, non-signature file entries match the clone input,
  including its manifest, resource table, and native libraries.
- The package remains `com.adobe.lrmobile.morphe`; all eight provider authorities,
  custom permissions, and launcher/application names pass clone checks.
- The APK v2 signature verifies and its signing certificate matches the earlier
  clone, so it uses the same installation identity.

Device testing is still required: restart the app with network access enabled,
confirm that syncing stays paused after attempting Resume Syncing and after
another restart, and try a local import/edit/export. Upload prevention has been
checked in the APK's control paths, not measured on a running device. Cloud-only
features follow Lightroom's normal paused-sync behavior.
