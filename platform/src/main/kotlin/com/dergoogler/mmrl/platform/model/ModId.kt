package com.dergoogler.mmrl.platform.model

import android.content.Intent
import android.os.Build
import android.os.Parcelable
import com.dergoogler.mmrl.platform.file.SuFile
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Parcelize
@JsonClass(generateAdapter = true)
data class ModId(var id: String) : Parcelable {
    /**
     * Represents the root directory of a module.
     *
     * This property constructs a [SuFile] object pointing to the path
     * `/data/adb/modules/<module_id>`. This is the base directory where
     * a Magisk module's files are typically located.
     *
     * @return A [SuFile] instance representing the module's root directory.
     */
    val root get() = SuFile("/data/adb/modules/$id")

    /**
     * Represents the webroot directory for a module.
     *
     * This property constructs a [SuFile] object pointing to the path
     * `/data/adb/modules/<module_id>/webroot`. This directory is typically
     * used by Magisk modules to store web-accessible files.
     *
     * @return A [SuFile] instance representing the module's webroot directory.
     */
    val webroot get() = SuFile(root, "webroot")

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ModId) return false
        return id == other.id
    }

    fun equals(other: String?, ignoreCase: Boolean = false): Boolean {
        if (!ignoreCase) {
            return id == other
        }

        return (id as java.lang.String).equalsIgnoreCase(other)
    }


    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object {
        const val INTENT_MOD_ID_AS_PARCELABLE = "MOD_ID_AS_PARCELABLE"
        const val INTENT_MOD_ID = "MOD_ID"
        const val INTENT_ID = "id"

        val String.asModId: ModId
            get() = ModId(this)

        val EMPTY = ModId("")

        /**
         * Retrieves a [ModId] from the Intent's extras.
         *
         * This function attempts to retrieve the ModId using three different keys in order:
         * 1. [INTENT_MOD_ID] (as a String)
         * 2. [INTENT_ID] (as a String)
         * 3. [INTENT_MOD_ID_AS_PARCELABLE] (as a Parcelable ModId)
         *
         * It handles API level differences for retrieving Parcelable extras.
         *
         * @return The [ModId] found in the Intent, or `null` if no ModId is found
         * using any of the supported keys.
         */
        fun Intent.getModId(): ModId? {
            val modId = getStringExtra(INTENT_MOD_ID)
            val id = getStringExtra(INTENT_ID)

            if (modId != null) {
                return modId.asModId
            }

            if (id != null) {
                return id.asModId
            }

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this.getParcelableExtra(INTENT_MOD_ID_AS_PARCELABLE, ModId::class.java)
            } else {
                @Suppress("DEPRECATION")
                this.getParcelableExtra<ModId>(INTENT_MOD_ID_AS_PARCELABLE)
            }
        }

        /**
         * Puts a [ModId] object into the Intent as an extra.
         *
         * This function stores the [ModId] under the key [INTENT_MOD_ID_AS_PARCELABLE].
         *
         * @param id The [ModId] to be put into the Intent.
         */
        fun Intent.putModId(id: ModId) {
            putExtra(INTENT_MOD_ID_AS_PARCELABLE, id)
        }

        /**
         * Puts a ModId string into the Intent as an extra.
         *
         * This function stores the ModId string under the key [INTENT_MOD_ID].
         *
         * @param id The ModId string to be put into the Intent.
         */
        fun Intent.putModId(id: String) {
            putExtra(INTENT_MOD_ID, id)
        }

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
