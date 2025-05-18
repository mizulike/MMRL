package com.dergoogler.mmrl.webui.model

import android.content.pm.PackageInfo

data class RequireNewVersion(
    val required: Boolean,
    val supportLink: String?,
    val supportText: String?,
    val packageInfo: PackageInfo,
    val requiredCode: Int
) {
}