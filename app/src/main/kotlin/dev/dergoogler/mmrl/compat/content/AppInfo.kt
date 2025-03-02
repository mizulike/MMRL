package dev.dergoogler.mmrl.compat.content

import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable
import android.os.Parcelable
import dev.dergoogler.mmrl.compat.delegate.ContextDelegate
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class AppInfo(
    val packageName: String,
    val profile: AppProfile?,
) : Parcelable {

    @IgnoredOnParcel
    private val context by lazy { ContextDelegate.getContext() }

    @IgnoredOnParcel
    @Contextual
    val packageInfo: PackageInfo = context.packageManager.getPackageInfo(packageName, 0)

    @IgnoredOnParcel
    val label: String = packageInfo.applicationInfo!!.loadLabel(context.packageManager).toString()

    val icon: Drawable get() = packageInfo.applicationInfo!!.loadIcon(context.packageManager)

    val uid: Int
        get() = packageInfo.applicationInfo!!.uid

    val allowSu: Boolean
        get() = profile != null && profile.allowSu
    val hasCustomProfile: Boolean
        get() {
            if (profile == null) {
                return false
            }

            return if (profile.allowSu) {
                !profile.rootUseDefault
            } else {
                !profile.nonRootUseDefault
            }
        }
}