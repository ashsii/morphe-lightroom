package app.lightroompatches.patches.forceupgrade

import app.lightroompatches.patches.shared.lightroom930Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.returnEarly

@Suppress("unused")
val disableForcedUpdatePatch = bytecodePatch(
    name = "Disable forced update",
    description = "Disables the forced-update check that blocks startup and imports.",
    default = true,
) {
    compatibleWith(lightroom930Compatibility)

    execute {
        // Require exactly one policy method and one getter. Fail if the app differs.
        val upgradeClass = ForceUpgradeStateFingerprint.matchAll(1..1).single().originalClassDef
        val upgradeRequired = ForceUpgradeRequiredFingerprint.matchAll(upgradeClass, 1..1).single()

        // In the inspected APK this is Lbf/p;->j()Z. Startup and import paths use
        // this flag to block access. Only override its getter; do not skip startup
        // initialization or merely suppress the non-cancelable dialog.
        upgradeRequired.method.returnEarly(false)
    }
}
