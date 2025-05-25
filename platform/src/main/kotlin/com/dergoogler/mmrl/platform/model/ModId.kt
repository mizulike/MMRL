package com.dergoogler.mmrl.platform.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModId(var id: String) : Parcelable {
    val sanitizedId: String
        get() {
            return id.replace(Regex("[^a-zA-Z0-9_]"), "_")
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

    val sanitizedIdWithFileInputStream get(): String = "${sanitizedIdWithFile}InputStream"

    companion object {
        val String.asModId: ModId
            get() = ModId(this)
    }
}
