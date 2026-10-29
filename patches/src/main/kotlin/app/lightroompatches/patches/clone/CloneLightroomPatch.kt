package app.lightroompatches.patches.clone

import app.lightroompatches.patches.shared.LIGHTROOM_CLONE_PACKAGE
import app.lightroompatches.patches.shared.LIGHTROOM_PACKAGE
import app.lightroompatches.patches.shared.lightroom930Compatibility
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

@Suppress("unused")
val cloneLightroomPatch = resourcePatch(
    name = "Clone Lightroom",
    description = "Changes the package to com.adobe.lrmobile.morphe for a separate installation.",
    default = false,
) {
    compatibleWith(lightroom930Compatibility)

    finalize {
        document("AndroidManifest.xml").use { document ->
            val manifest = document.documentElement
            check(manifest.getAttribute("package") == LIGHTROOM_PACKAGE) {
                "Expected the original Lightroom package before cloning."
            }

            val nodes = document.getElementsByTagName("*")
            val elements = (0 until nodes.length).map { nodes.item(it) as Element }
            val permissionNames = elements.filter { it.tagName == "permission" }.associate {
                val oldName = it.getAttribute("android:name")
                check(oldName.startsWith("$LIGHTROOM_PACKAGE.")) {
                    "Unexpected custom permission: $oldName"
                }
                oldName to oldName.replaceFirst(LIGHTROOM_PACKAGE, LIGHTROOM_CLONE_PACKAGE)
            }

            for (element in elements) {
                // Component class names still refer to the original DEX classes. Expand any
                // package-relative names before changing the manifest's application ID.
                val componentAttributes = when (element.tagName) {
                    "application" -> listOf("android:name", "android:backupAgent",
                        "android:manageSpaceActivity", "android:appComponentFactory")
                    "activity", "receiver", "service", "provider", "instrumentation" ->
                        listOf("android:name", "android:parentActivityName")
                    "activity-alias" -> listOf("android:name", "android:targetActivity")
                    else -> emptyList()
                }
                for (attribute in componentAttributes) {
                    val value = element.getAttribute(attribute)
                    if (value.startsWith('.')) {
                        element.setAttribute(attribute, LIGHTROOM_PACKAGE + value)
                    } else if (value.isNotEmpty() && '.' !in value) {
                        element.setAttribute(attribute, "$LIGHTROOM_PACKAGE.$value")
                    }
                }

                if (element.tagName == "provider") {
                    val authorities = element.getAttribute("android:authorities").split(';')
                    val renamed = authorities.map { authority ->
                        // Includes the hyphenated AdobeEngagement authority as well as dot suffixes.
                        check(authority.startsWith(LIGHTROOM_PACKAGE)) {
                            "Unexpected provider authority: $authority"
                        }
                        authority.replaceFirst(LIGHTROOM_PACKAGE, LIGHTROOM_CLONE_PACKAGE)
                    }
                    element.setAttribute("android:authorities", renamed.joinToString(";"))
                }

                if (element.tagName == "permission" || element.tagName == "uses-permission") {
                    val oldName = element.getAttribute("android:name")
                    permissionNames[oldName]?.let { element.setAttribute("android:name", it) }
                }
                for (attribute in listOf("android:permission", "android:readPermission", "android:writePermission")) {
                    permissionNames[element.getAttribute(attribute)]?.let { element.setAttribute(attribute, it) }
                }

                // Explicit task affinities must also be unique across the two installations.
                val taskAffinity = element.getAttribute("android:taskAffinity")
                if (taskAffinity.isNotEmpty()) {
                    element.setAttribute("android:taskAffinity", "$taskAffinity.morphe")
                }
            }

            manifest.setAttribute("package", LIGHTROOM_CLONE_PACKAGE)
        }
    }
}
