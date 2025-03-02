package com.dergoogler.mmrl

class Platform(private val realPlatform: String) {
    private val magisk = "magisk"
    private val kernelSU = "kernelsu"
    private val kernelSuNext = "ksunext"
    private val apatch = "apatch"
    private val shizuku = "shizuku"

    val isMagisk get() = realPlatform == magisk
    val isKernelSU get() = realPlatform == kernelSU
    val isKernelSuNext get() = realPlatform == kernelSuNext
    val isAPatch get() = realPlatform == apatch
    val isShizuku get() = realPlatform == shizuku

    val isNotMagisk get() = realPlatform != magisk
    val isNotKernelSU get() = realPlatform != kernelSU || realPlatform != kernelSuNext
    val isNotKernelSuNext get() = realPlatform != kernelSuNext
    val isNotAPatch get() = realPlatform != apatch
    val isNotShizuku get() = realPlatform != shizuku

    val isValid get() = isMagisk || isKernelSU || isAPatch || isKernelSuNext || isShizuku
    val isKernelSuOrNext get() = isKernelSuNext || isKernelSU

    val current: String
        get() = when {
            isMagisk -> magisk
            isKernelSU -> kernelSU
            isKernelSuNext -> kernelSuNext
            isAPatch -> apatch
            isShizuku -> shizuku
            else -> ""
        }

    fun only(platform: Boolean, block: Platform.() -> Unit) {
        if (platform) {
            block()
        }
    }

    companion object {
        val EMPTY = Platform("")
    }
}