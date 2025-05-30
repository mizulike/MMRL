package com.dergoogler.mmrl.platform

import android.app.ActivityThread
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.ServiceManager
import android.util.Log
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dergoogler.mmrl.platform.content.IService
import com.dergoogler.mmrl.platform.content.Service
import com.dergoogler.mmrl.platform.hiddenApi.HiddenPackageManager
import com.dergoogler.mmrl.platform.hiddenApi.HiddenUserManager
import com.dergoogler.mmrl.platform.model.IProvider
import com.dergoogler.mmrl.platform.model.PlatformConfig
import com.dergoogler.mmrl.platform.model.PlatformConfigImpl
import com.dergoogler.mmrl.platform.stub.IFileManager
import com.dergoogler.mmrl.platform.stub.IModuleManager
import com.dergoogler.mmrl.platform.stub.IServiceManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.io.FileDescriptor

const val TIMEOUT_MILLIS = 15_000L
const val PLATFORM_KEY = "PLATFORM"
internal const val BINDER_TRANSACTION = 84398154

/**
 * Represents the various platforms supported by the application.
 *
 * @property id A unique identifier for the platform.
 */
enum class Platform(val id: String) {
    Magisk("magisk"),
    KernelSU("kernelsu"),
    KsuNext("ksunext"),
    APatch("apatch"),
    MKSU("mksu"),
    SukiSU("sukisu"),
    RKSU("rksu"),
    NonRoot("nonroot"),
    Unknown("unknown");

    companion object {
        fun from(value: String): Platform {
            return entries.firstOrNull { it.id == value } ?: NonRoot
        }
    }

    val isMagisk get() = this == Magisk
    val isKernelSU get() = this == KernelSU
    val isKernelSuNext get() = this == KsuNext
    val isAPatch get() = this == APatch

    val isNotMagisk get() = !isMagisk
    val isNotKernelSU get() = this != KernelSU && this != KsuNext
    val isNotKernelSuNext get() = !isKernelSuNext
    val isNotAPatch get() = !isAPatch

    val isNotNonRoot get() = this != NonRoot
    val isNonRoot get() = this == NonRoot
    val isValid get() = this != NonRoot
    val isNotValid get() = !isValid
    val isKernelSuOrNext get() = this == KernelSU || this == KsuNext

    val current get() = id
}

