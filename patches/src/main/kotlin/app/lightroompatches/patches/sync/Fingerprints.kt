package app.lightroompatches.patches.sync

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.Opcode

internal object RestoreUserSyncPauseFingerprint : Fingerprint(
    isStatic = false,
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        string("syncPausedByUser"),
        methodCall(
            definingClass = "Ljava/lang/Boolean;",
            name = "booleanValue",
            parameters = emptyList(),
            returnType = "Z",
        ),
        opcode(Opcode.MOVE_RESULT, MatchAfterImmediately()),
        fieldAccess(opcode = Opcode.SGET_OBJECT, name = "TI_SYNC_PAUSE_USER"),
    ),
)

internal object ReleaseSyncPauseFingerprint : Fingerprint(
    isStatic = false,
    returnType = "Z",
    parameters = listOf("L"),
    strings = listOf("syncPausedByUser", "ReleaseSyncRequest:"),
)

internal object SetSyncEnabledFingerprint : Fingerprint(
    isStatic = false,
    returnType = "V",
    parameters = listOf("Z"),
    strings = listOf("setSyncEnabled"),
)

internal object CreateCatalogModelFingerprint : Fingerprint(
    isStatic = false,
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Z", "L"),
    strings = listOf("catalogModel", "enablePDL", "enableDirectDownload"),
)
