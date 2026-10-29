group = "app.lightroompatches"

patches {
    about {
        name = "Personal Lightroom patches"
        description = "Lightroom Android 9.3.0 patches for use with Morphe."
        source = "https://github.com/ashsii/morphe-lightroom"
        author = "ashsii"
        contact = "https://github.com/ashsii/morphe-lightroom/issues"
        website = "https://github.com/ashsii/morphe-lightroom"
        license = "GPLv3"
    }
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}
