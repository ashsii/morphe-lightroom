package app.lightroompatches.patches.sync

import app.lightroompatches.patches.shared.lightroom930CloneCompatibility
import app.lightroompatches.patches.shared.lightroom930Compatibility
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Suppress("unused")
val keepCloudSyncPausedPatch = bytecodePatch(
    name = "Keep cloud sync paused",
    description = "Keeps cloud photo syncing paused and prevents resuming it.",
    default = false,
) {
    compatibleWith(lightroom930Compatibility, lightroom930CloneCompatibility)

    execute {
        // Resolve every target before modifying either class, with exactly one match each.
        val restorePause = RestoreUserSyncPauseFingerprint.matchAll(1..1).single()
        val releasePause = ReleaseSyncPauseFingerprint
            .matchAll(restorePause.originalClassDef, 1..1).single()
        val setSyncEnabled = SetSyncEnabledFingerprint.matchAll(1..1).single()
        val createCatalog = CreateCatalogModelFingerprint
            .matchAll(setSyncEnabled.originalClassDef, 1..1).single()

        // Preserve the normal pause-restoration body. Its true branch persists the
        // user pause, adds the pause reason, and calls the real cloud-library pause
        // mechanism. Returning true early would skip those required side effects.
        val pauseResult = restorePause.instructionMatches[2]
        val pauseRegister = pauseResult.getInstruction<OneRegisterInstruction>().registerA
        val userPauseReason = restorePause.instructionMatches[3]
            .getInstruction<ReferenceInstruction>().reference
        restorePause.method.addInstruction(pauseResult.index + 1, "const/16 v$pauseRegister, 0x1")

        // Deny releasing the user pause. Other reasons still go through the original
        // cleanup, so an HDR import or low-storage transition can finish normally.
        releasePause.method.apply {
            addInstructionsWithLabels(
                0,
                """
                    sget-object v0, $userPauseReason
                    if-ne p1, v0, :release_other_reasons
                    const/4 v0, 0x0
                    return v0
                """,
                ExternalLabel("release_other_reasons", getInstruction(0)),
            )
        }

        // In the inspected APK these are c0.c2(Z)V and c0.u2(String,Z,o0)V.
        // Preserve initialization and model notifications while forcing their boolean
        // sync-enabled inputs off, including the first creation of the native model.
        setSyncEnabled.method.addInstruction(0, "const/16 p1, 0x0")
        createCatalog.method.addInstruction(0, "const/16 p2, 0x0")
    }
}
