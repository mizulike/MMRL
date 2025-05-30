package com.dergoogler.mmrl.webui.interfaces

import android.content.Context
import android.content.pm.PackageInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.graphics.createBitmap
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.platform.hiddenApi.HiddenPackageManager
import com.dergoogler.mmrl.platform.hiddenApi.HiddenUserManager
import com.dergoogler.mmrl.webui.interfaces.UMApplicationInfo.Companion.toUMApplicationInfo
import com.dergoogler.mmrl.webui.moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

fun <T> listToJson(list: List<T>?): String? {
    if (list == null) return null

    val adapter = moshi.adapter(List::class.java)
    return adapter.toJson(list)
}

data class UMApplicationInfo(
    @get:JavascriptInterface
    val packageName: String,
    @get:JavascriptInterface
    val name: String?,
    @get:JavascriptInterface
    val label: String?,
    @get:JavascriptInterface
    val versionName: String?,
    @get:JavascriptInterface
    val versionCode: Long,
    @get:JavascriptInterface
    val nonLocalizedLabel: String?,
    @get:JavascriptInterface
    var appComponentFactory: String? = null,
    @get:JavascriptInterface
    var backupAgentName: String? = null,
    @get:JavascriptInterface
    var category: Int? = -1,
    @get:JavascriptInterface
    var className: String? = null,
    @get:JavascriptInterface
    var compatibleWidthLimitDp: Int = 0,
    @get:JavascriptInterface
    var compileSdkVersion: Int? = 0,
    @get:JavascriptInterface
    var compileSdkVersionCodename: String? = null,
    @get:JavascriptInterface
    var dataDir: String? = null,
    @get:JavascriptInterface
    var description: String? = null,
    @get:JavascriptInterface
    var deviceProtectedDataDir: String? = null,
    @get:JavascriptInterface
    var enabled: Boolean = true,
    @get:JavascriptInterface
    var flags: Int? = 0,
    @get:JavascriptInterface
    var largestWidthLimitDp: Int? = 0,
    @get:JavascriptInterface
    var manageSpaceActivityName: String? = null,
    @get:JavascriptInterface
    var minSdkVersion: Int? = 0,
    @get:JavascriptInterface
    var nativeLibraryDir: String? = null,
    @get:JavascriptInterface
    var permission: String? = null,
    @get:JavascriptInterface
    var processName: String? = null,
    @get:JavascriptInterface
    var publicSourceDir: String? = null,
    @get:JavascriptInterface
    var requiresSmallestWidthDp: Int? = 0,
    @get:JavascriptInterface
    var sharedLibraryFiles: String? = null,
    @get:JavascriptInterface
    var sourceDir: String? = null,
    @get:JavascriptInterface
    var splitNames: String? = null,
    @get:JavascriptInterface
    var splitPublicSourceDirs: String? = null,
    @get:JavascriptInterface
    var splitSourceDirs: String? = null,
    @get:JavascriptInterface
    var storageUuid: String? = null,
    @get:JavascriptInterface
    var targetSdkVersion: Int? = 0,
    @get:JavascriptInterface
    var taskAffinity: String? = null,
    @get:JavascriptInterface
    var theme: Int? = 0,
    @get:JavascriptInterface
    var uiOptions: Int? = 0,
    @get:JavascriptInterface
    var uid: Int = 0,
) {
    companion object {
        fun PackageInfo.toUMApplicationInfo(context: Context): UMApplicationInfo {
            val spm = context.packageManager

            return with(applicationInfo) {
                return@with UMApplicationInfo(
                    packageName = packageName,
                    name = this?.name,
                    versionName = versionName,
                    versionCode = PackageInfoCompat.getLongVersionCode(this@toUMApplicationInfo),
                    label = this?.loadLabel(spm).toString(),
                    nonLocalizedLabel = this?.nonLocalizedLabel?.toString(),
                    appComponentFactory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) this?.appComponentFactory else null,
                    backupAgentName = this?.backupAgentName,
                    category = this?.category,
                    className = this?.className,
                    compileSdkVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) this?.compileSdkVersion else null,
                    compileSdkVersionCodename = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) this?.compileSdkVersionCodename else null,
                    dataDir = this?.dataDir,
                    deviceProtectedDataDir = this?.deviceProtectedDataDir,
                    enabled = this?.enabled == true,
                    flags = this?.flags,
                    largestWidthLimitDp = this?.largestWidthLimitDp,
                    manageSpaceActivityName = this?.manageSpaceActivityName,
                    minSdkVersion = this?.minSdkVersion,
                    nativeLibraryDir = this?.nativeLibraryDir,
                    permission = this?.permission,
                    processName = this?.processName,
                    publicSourceDir = this?.publicSourceDir,
                    requiresSmallestWidthDp = this?.requiresSmallestWidthDp,
                    // sharedLibraryFiles = listToJson(sharedLibraryFiles.toList()),
                    sourceDir = this?.sourceDir,
                    storageUuid = this?.storageUuid?.toString(),
                    targetSdkVersion = this?.targetSdkVersion,
                    taskAffinity = this?.taskAffinity,
                    theme = this?.theme,
                    uiOptions = this?.uiOptions,
                    splitNames = listToJson(splitNames.toList()),
                    splitPublicSourceDirs = listToJson(this?.splitPublicSourceDirs?.toList()),
                    splitSourceDirs = listToJson(this?.splitSourceDirs?.toList()),
                )
            }
        }
    }
}

@Keep
class PackageManagerInterface(wxOptions: WXOptions) : WXInterface(wxOptions),
    CoroutineScope by MainScope() {
    private val pm get(): HiddenPackageManager = PlatformManager.packageManager
    private val um get(): HiddenUserManager = PlatformManager.userManager

    override var name: String = "\$packageManager"
    override var tag: String = "PackageManagerInterface"

    @JavascriptInterface
    fun getPackageUid(packageName: String, flags: Int, userId: Int): Int {
        return pm.getPackageUid(packageName, flags, userId)
    }

    @JavascriptInterface
    fun getApplicationLogo(
        packageName: String,
        flags: Int,
        userId: Int,
    ): FileInputInterfaceStream? {
        val pf = pm.getApplicationInfo(packageName, flags, userId)
        return runBlocking {
            return@runBlocking getDrawableBase64InputStream(
                drawable = pf.loadLogo(context.packageManager)
            )
        }
    }


    @JavascriptInterface
    fun getApplicationLogo(
        packageName: String,
        flags: Int,
    ): FileInputInterfaceStream? {
        val pf = pm.getApplicationInfo(packageName, flags, 0)
        return runBlocking {
            return@runBlocking getDrawableBase64InputStream(
                drawable = pf.loadLogo(context.packageManager)
            )
        }
    }

    @JavascriptInterface
    fun getApplicationLogo(
        packageName: String,
    ): FileInputInterfaceStream? {
        val pf = pm.getApplicationInfo(packageName, 0, 0)
        return runBlocking {
            return@runBlocking getDrawableBase64InputStream(
                drawable = pf.loadLogo(context.packageManager)
            )
        }
    }

    @JavascriptInterface
    fun getApplicationIcon(
        packageName: String,
        flags: Int,
        userId: Int,
    ): FileInputInterfaceStream? = runJsCatching {
        val pf = pm.getApplicationInfo(packageName, flags, userId)
        return@runJsCatching runBlocking {
            return@runBlocking getDrawableBase64InputStream(
                drawable = pf.loadIcon(context.packageManager)
            )
        }
    }

    @JavascriptInterface
    fun getApplicationIcon(
        packageName: String,
        flags: Int,
    ): FileInputInterfaceStream? {
        val pf = pm.getApplicationInfo(packageName, flags, 0)
        return runBlocking {
            return@runBlocking getDrawableBase64InputStream(
                drawable = pf.loadIcon(context.packageManager)
            )
        }
    }

    @JavascriptInterface
    fun getApplicationIcon(
        packageName: String,
    ): FileInputInterfaceStream? {
        val pf = pm.getApplicationInfo(packageName, 0, 0)
        return runBlocking {
            return@runBlocking getDrawableBase64InputStream(
                drawable = pf.loadIcon(context.packageManager)
            )
        }
    }

    @JavascriptInterface
    fun getApplicationUnbadgedIcon(
        packageName: String,
        flags: Int,
        userId: Int,
    ): FileInputInterfaceStream? {
        val pf = pm.getApplicationInfo(packageName, flags, userId)
        return runBlocking {
            return@runBlocking getDrawableBase64InputStream(
                drawable = pf.loadUnbadgedIcon(context.packageManager)
            )
        }
    }

    @JavascriptInterface
    fun getApplicationUnbadgedIcon(
        packageName: String,
        flags: Int,
    ): FileInputInterfaceStream? {
        val pf = pm.getApplicationInfo(packageName, flags, um.myUserId)
        return runBlocking {
            return@runBlocking getDrawableBase64InputStream(
                drawable = pf.loadUnbadgedIcon(context.packageManager)
            )
        }
    }

    @JavascriptInterface
    fun getApplicationUnbadgedIcon(
        packageName: String,
    ): FileInputInterfaceStream? {
        val pf = pm.getApplicationInfo(packageName, 0, um.myUserId)
        return runBlocking {
            return@runBlocking getDrawableBase64InputStream(
                drawable = pf.loadUnbadgedIcon(context.packageManager)
            )
        }
    }

    @JavascriptInterface
    fun getInstalledPackages(flags: Int, userId: Int): String {
        val ip = pm.getInstalledPackages(flags, userId)
        val list = ip.map { it.packageName }
        return listToJson(list) ?: "[]"
    }

    @JavascriptInterface
    fun getInstalledPackages(flags: Int): String {
        val ip = pm.getInstalledPackages(flags, um.myUserId)
        val list = ip.map { it.packageName }
        return listToJson(list) ?: "[]"
    }

    @JavascriptInterface
    fun getInstalledPackages(): String {
        val ip = pm.getInstalledPackages(0, um.myUserId)
        val list = ip.map { it.packageName }
        return listToJson(list) ?: "[]"
    }

    @JavascriptInterface
    fun getApplicationInfo(packageName: String, flags: Int, userId: Int): UMApplicationInfo {
        val ai = pm.getPackageInfo(packageName, flags, userId)
        return runBlocking {
            return@runBlocking ai.toUMApplicationInfo(context)
        }
    }

    @JavascriptInterface
    fun getApplicationInfo(packageName: String, flags: Int): UMApplicationInfo {
        val ai = pm.getPackageInfo(packageName, flags, um.myUserId)
        return runBlocking {
            return@runBlocking ai.toUMApplicationInfo(context)
        }
    }


    @JavascriptInterface
    fun getApplicationInfo(packageName: String): UMApplicationInfo {
        val ai = pm.getPackageInfo(packageName, 0, um.myUserId)
        return runBlocking {
            return@runBlocking ai.toUMApplicationInfo(context)
        }
    }

    private suspend fun getDrawableBase64InputStream(
        drawable: Drawable,
    ): FileInputInterfaceStream? = withContext(Dispatchers.IO) {
        val bitmap = drawableToBitmap(drawable)

        val pngOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, pngOutputStream)
        val pngByteArray = pngOutputStream.toByteArray()

        val inputStream = ByteArrayInputStream(pngByteArray)
        FileInputInterfaceStream(inputStream, wxOptions)
    }


    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 1
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 1
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

}