package com.dergoogler.mmrl.webui.interfaces

import android.content.pm.PackageInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.webkit.JavascriptInterface
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.graphics.createBitmap
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.hiddenApi.HiddenPackageManager
import com.dergoogler.mmrl.platform.hiddenApi.HiddenUserManager
import com.dergoogler.mmrl.webui.interfaces.UMApplicationInfo.Companion.toUMApplicationInfo
import com.dergoogler.mmrl.webui.model.JavaScriptInterface
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
    var category: Int = -1,
    @get:JavascriptInterface
    var className: String? = null,
    @get:JavascriptInterface
    var compatibleWidthLimitDp: Int = 0,
    @get:JavascriptInterface
    var compileSdkVersion: Int = 0,
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
    var flags: Int = 0,
    @get:JavascriptInterface
    var largestWidthLimitDp: Int = 0,
    @get:JavascriptInterface
    var manageSpaceActivityName: String? = null,
    @get:JavascriptInterface
    var minSdkVersion: Int = 0,
    @get:JavascriptInterface
    var nativeLibraryDir: String? = null,
    @get:JavascriptInterface
    var permission: String? = null,
    @get:JavascriptInterface
    var processName: String? = null,
    @get:JavascriptInterface
    var publicSourceDir: String? = null,
    @get:JavascriptInterface
    var requiresSmallestWidthDp: Int = 0,
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
    var targetSdkVersion: Int = 0,
    @get:JavascriptInterface
    var taskAffinity: String? = null,
    @get:JavascriptInterface
    var theme: Int = 0,
    @get:JavascriptInterface
    var uiOptions: Int = 0,
    @get:JavascriptInterface
    var uid: Int = 0,
) {
    companion object {
        fun PackageInfo.toUMApplicationInfo(wxOptions: WXOptions): UMApplicationInfo {
            val spm = wxOptions.context.packageManager

            return with(applicationInfo) {
                return@with UMApplicationInfo(
                    packageName = packageName,
                    name = name,
                    versionName = versionName,
                    versionCode = PackageInfoCompat.getLongVersionCode(this@toUMApplicationInfo),
                    label = this.loadLabel(spm).toString(),
                    nonLocalizedLabel = nonLocalizedLabel?.toString(),
                    appComponentFactory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) appComponentFactory else null,
                    backupAgentName = backupAgentName,
                    category = category,
                    className = className,
                    compileSdkVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) compileSdkVersion else -1,
                    compileSdkVersionCodename = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) compileSdkVersionCodename else null,
                    dataDir = dataDir,
                    deviceProtectedDataDir = deviceProtectedDataDir,
                    enabled = enabled,
                    flags = flags,
                    largestWidthLimitDp = largestWidthLimitDp,
                    manageSpaceActivityName = manageSpaceActivityName,
                    minSdkVersion = minSdkVersion,
                    nativeLibraryDir = nativeLibraryDir,
                    permission = permission,
                    processName = processName,
                    publicSourceDir = publicSourceDir,
                    requiresSmallestWidthDp = requiresSmallestWidthDp,
                    // sharedLibraryFiles = listToJson(sharedLibraryFiles.toList()),
                    sourceDir = sourceDir,
                    storageUuid = storageUuid?.toString(),
                    targetSdkVersion = targetSdkVersion,
                    taskAffinity = taskAffinity,
                    theme = theme,
                    uiOptions = uiOptions,
                    splitNames = listToJson(splitNames?.toList()),
                    splitPublicSourceDirs = listToJson(splitPublicSourceDirs?.toList()),
                    splitSourceDirs = listToJson(splitSourceDirs?.toList()),
                )
            }
        }
    }
}

class PackageManagerInterface(wxOptions: WXOptions) : WXInterface(wxOptions),
    CoroutineScope by MainScope() {
    private val pm get(): HiddenPackageManager = Platform.packageManager
    private val um get(): HiddenUserManager = Platform.userManager

    override var name: String = "\$packageManager"

    companion object {
        fun factory() = JavaScriptInterface(PackageManagerInterface::class.java)
    }

    @JavascriptInterface
    fun getPackageUid(packageName: String, flags: Int, userId: Int): Int {
        return pm.getPackageUid(packageName, flags, userId)
    }

    @JavascriptInterface
    fun getApplicationIcon(
        packageName: String,
        flags: Int,
        userId: Int,
    ): FileInputInterfaceStream? {
        val pf = pm.getApplicationInfo(packageName, flags, userId)
        return runBlocking {
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
            return@runBlocking ai.toUMApplicationInfo(wxOptions)
        }
    }

    @JavascriptInterface
    fun getApplicationInfo(packageName: String, flags: Int): UMApplicationInfo {
        val ai = pm.getPackageInfo(packageName, flags, um.myUserId)
        return runBlocking {
            return@runBlocking ai.toUMApplicationInfo(wxOptions)
        }
    }


    @JavascriptInterface
    fun getApplicationInfo(packageName: String): UMApplicationInfo {
        val ai = pm.getPackageInfo(packageName, 0, um.myUserId)
        return runBlocking {
            return@runBlocking ai.toUMApplicationInfo(wxOptions)
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