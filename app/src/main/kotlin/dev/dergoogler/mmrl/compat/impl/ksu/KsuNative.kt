package dev.dergoogler.mmrl.compat.impl.ksu

object KsuNative {
    // minimal supported kernel version
    // 10915: allowlist breaking change, add app profile
    // 10931: app profile struct add 'version' field
    // 10946: add capabilities
    // 10977: change groups_count and groups to avoid overflow write
    // 11071: Fix the issue of failing to set a custom SELinux type.
    const val MINIMAL_SUPPORTED_KERNEL = 11071

    // 11640: Support query working mode, LKM or GKI
    // when MINIMAL_SUPPORTED_KERNEL > 11640, we can remove this constant.
    const val MINIMAL_SUPPORTED_KERNEL_LKM = 11648

    // 12404: Support disable sucompat mode
    const val MINIMAL_SUPPORTED_SU_COMPAT = 12404
    const val KERNEL_SU_DOMAIN = "u:r:su:s0"

    const val ROOT_UID = 0
    const val ROOT_GID = 0

    const val MODE_LTS = 0
    const val MODE_LKM = 1

    init {
        System.loadLibrary("kernelsu")
    }

    external fun getAllowList(): IntArray
    external fun isSuEnabled(): Boolean
    external fun setSuEnabled(enabled: Boolean): Boolean
    external fun isSafeMode(): Boolean
    external fun getVersion(): Int
    external fun isLkmMode(): Boolean
    external fun getLkmMode(): Int
}