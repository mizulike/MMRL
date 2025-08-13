package com.dergoogler.mmrl.utils

import io.github.z4kn4fein.semver.toVersionOrNull

object Versioning {
    /**
     * Determine if an update is available by preferring semantic version comparison when both
     * version names are parseable; otherwise, fall back to versionCode comparison.
     */
    @JvmStatic
    fun isUpdateAvailable(
        installedVersionName: String?,
        installedVersionCode: Int,
        remoteVersionName: String?,
        remoteVersionCode: Int,
    ): Boolean {
        val iName = installedVersionName.orEmpty().removePrefix("v")
        val rName = remoteVersionName.orEmpty().removePrefix("v")

        // Try semver compare first when both names are parseable (non-strict)
        val iSem = iName.toVersionOrNull(strict = false)
        val rSem = rName.toVersionOrNull(strict = false)
        if (iSem != null && rSem != null) {
            return rSem > iSem
        }

        // Fallback to versionCode compare if both are valid (non-negative)
        val codesComparable = installedVersionCode >= 0 && remoteVersionCode >= 0
        return if (codesComparable) {
            remoteVersionCode > installedVersionCode
        } else {
            // Unknown comparison: avoid nagging users when local metadata is missing
            false
        }
    }
}
