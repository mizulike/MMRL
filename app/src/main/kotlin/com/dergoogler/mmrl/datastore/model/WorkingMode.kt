package com.dergoogler.mmrl.datastore.model

enum class WorkingMode {
    FIRST_SETUP,
    MODE_MAGISK,
    MODE_KERNEL_SU,
    MODE_KERNEL_SU_NEXT,
    MODE_APATCH,
    MODE_NON_ROOT,
    MODE_SHIZUKU;

    companion object {
        val WorkingMode.isRoot get() = this == MODE_MAGISK || this == MODE_KERNEL_SU || this == MODE_KERNEL_SU_NEXT || this == MODE_APATCH || this == MODE_SHIZUKU
        val WorkingMode.isNonRoot get() = this == MODE_NON_ROOT
        val WorkingMode.isSetup get() = this == FIRST_SETUP
    }
}