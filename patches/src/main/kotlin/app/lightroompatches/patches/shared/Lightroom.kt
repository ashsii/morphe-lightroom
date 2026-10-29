package app.lightroompatches.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.SupportedAbi

internal const val LIGHTROOM_PACKAGE = "com.adobe.lrmobile"
internal const val LIGHTROOM_CLONE_PACKAGE = "$LIGHTROOM_PACKAGE.morphe"

private fun lightroom930(packageName: String) = Compatibility(
    name = "Lightroom",
    packageName = packageName,
    apkFileType = ApkFileType.APK,
    appIconColor = 0x001E36,
    targets = listOf(
        AppTarget(
            version = "9.3.0",
            versionCodes = mapOf(SupportedAbi.ARM64_V8A to 710903001),
        ),
    ),
)

internal val lightroom930Compatibility = lightroom930(LIGHTROOM_PACKAGE)
internal val lightroom930CloneCompatibility = lightroom930(LIGHTROOM_CLONE_PACKAGE)
