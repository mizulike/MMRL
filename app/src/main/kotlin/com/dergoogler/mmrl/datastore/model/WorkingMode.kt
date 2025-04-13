package com.dergoogler.mmrl.datastore.model

import com.dergoogler.mmrl.platform.Platform

enum class WorkingMode {
    FIRST_SETUP,
    MODE_MAGISK,
    MODE_KERNEL_SU,
    MODE_KERNEL_SU_NEXT,
    MODE_APATCH,
    MODE_NON_ROOT;

    fun toPlatform() = when (this) {
        MODE_MAGISK -> Platform.Magisk
        MODE_KERNEL_SU -> Platform.KernelSU
        MODE_KERNEL_SU_NEXT -> Platform.KsuNext
        MODE_APATCH -> Platform.APatch
        MODE_NON_ROOT -> Platform.NonRoot
        FIRST_SETUP -> Platform.NonRoot
    }

    companion object {
        val WorkingMode.isRoot get() = this == MODE_MAGISK || this == MODE_KERNEL_SU || this == MODE_KERNEL_SU_NEXT || this == MODE_APATCH
        val WorkingMode.isNonRoot get() = this == MODE_NON_ROOT
        val WorkingMode.isSetup get() = this == FIRST_SETUP


    }
}