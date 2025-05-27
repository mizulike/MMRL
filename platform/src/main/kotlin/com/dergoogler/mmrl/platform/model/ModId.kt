package com.dergoogler.mmrl.platform.model

import android.os.Parcelable
import com.dergoogler.mmrl.ext.ifNotEmpty
import kotlinx.parcelize.Parcelize
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.text.isNullOrEmpty

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

    override fun toString(): String = id

    companion object {
        val String.asModId: ModId
            get() = ModId(this)

        val EMPTY = ModId("")

        /**
         * Checks if the [ModId] is null or its underlying [id] string is empty.
         *
         * This function uses contracts to provide smart casting benefits to the caller.
         * If this function returns `false`, the compiler will know that the `ModId` instance
         * is not null.
         *
         * @return `true` if the [ModId] is null or its [id] is empty, `false` otherwise.
         */
        @OptIn(ExperimentalContracts::class)
        fun ModId?.isNullOrEmpty(): Boolean {
            contract {
                returns(false) implies (this@isNullOrEmpty != null)
            }

            return this == null || this.id.isEmpty()
        }
    }
}
