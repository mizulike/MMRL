package com.dergoogler.mmrl.webui.model

import com.dergoogler.mmrl.webui.webUiConfig

data class ModId(var id: String) {
    val sanitizedId: String
        get() {
            return id.replace(Regex("[^a-zA-Z0-9._]"), "_")
        }

    val sanitizedIdWithFile
        get(): String {
            return "$${
                when {
                    sanitizedId.length >= 2 -> sanitizedId[0].uppercase() + sanitizedId[1]
                    sanitizedId.isNotEmpty() -> sanitizedId[0].uppercase()
                    else -> ""
                }
            }File"
        }

    val sanitizedIdWithFileInputStream = "${sanitizedIdWithFile}InputStream"

    fun toWebUIConfig() = webUiConfig(this)
}