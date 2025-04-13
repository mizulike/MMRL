package com.dergoogler.mmrl.model.local

import com.dergoogler.mmrl.utils.Utils
import com.dergoogler.mmrl.platform.content.LocalModule
import com.dergoogler.mmrl.platform.content.LocalModuleFeatures

typealias LocalModule = com.dergoogler.mmrl.platform.content.LocalModule
typealias LocalModuleFeatures = com.dergoogler.mmrl.platform.content.LocalModuleFeatures

val com.dergoogler.mmrl.platform.content.LocalModule.versionDisplay get() = Utils.getVersionDisplay(version, versionCode)

val com.dergoogler.mmrl.platform.content.LocalModuleFeatures.hasFeatures
    get() = webui ||
            action ||
            service ||
            postFsData ||
            resetprop ||
            sepolicy ||
            zygisk ||
            apks ||
            postMount ||
            bootCompleted


fun com.dergoogler.mmrl.platform.content.LocalModule.Companion.example() =
    com.dergoogler.mmrl.platform.content.LocalModule(
        id = "local_example",
        name = "Example",
        version = "2022.08.16",
        versionCode = 1703,
        author = "Sanmer",
        description = "This is an example!",
        updateJson = "",
        state = State.ENABLE,
        features = com.dergoogler.mmrl.platform.content.LocalModuleFeatures.EMPTY,
        size = 0,
        lastUpdated = 0L,
    )