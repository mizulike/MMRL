package com.dergoogler.mmrl.model.state

object Permissions {
    const val MAGISK_SERVICE = "magisk.permission.SERVICE"
    const val MAGISK_POST_FS_DATA = "magisk.permission.POST_FS_DATA"
    const val MAGISK_RESETPROP = "magisk.permission.RESETPROP"
    const val MAGISK_SEPOLICY = "magisk.permission.SEPOLICY"
    const val MAGISK_ZYGISK = "magisk.permission.ZYGISK"
    const val MAGISK_ACTION = "magisk.permission.ACTION"

    const val KERNELSU_WEBUI = "kernelsu.permission.WEBUI"
    const val KERNELSU_POST_MOUNT = "kernelsu.permission.POST_MOUNT"
    const val KERNELSU_BOOT_COMPLETED = "kernelsu.permission.BOOT_COMPLETED"

    const val MMRL_WEBUI = "mmrl.permission.WEBUI"
    const val MMRL_WEBUI_CONFIG = "mmrl.permission.WEBUI_CONFIG"
    const val MMRL_APKS = "mmrl.permission.APKS"
}
