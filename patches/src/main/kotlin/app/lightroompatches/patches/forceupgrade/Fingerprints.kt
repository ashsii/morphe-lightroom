package app.lightroompatches.patches.forceupgrade

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.Opcode

/** Identifies ForceUpgradeUtil through its persisted policy and analytics strings. */
internal object ForceUpgradeStateFingerprint : Fingerprint(
    isStatic = true,
    returnType = "V",
    parameters = emptyList(),
    strings = listOf(
        "com.adobe.lrmobile.app_min_version_key",
        "com.adobe.lrmobile.denylisted_version_set_key",
        "ForceUpgrade:Triggered",
    ),
)

/** Matches the side-effect-free getter in the class found above, regardless of obfuscated names. */
internal object ForceUpgradeRequiredFingerprint : Fingerprint(
    isStatic = true,
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.SGET_BOOLEAN,
            definingClass = "this",
            type = "Z",
        ),
        opcode(Opcode.RETURN, MatchAfterImmediately()),
    ),
    custom = { method, _ -> method.implementation?.instructions?.count() == 2 },
)
