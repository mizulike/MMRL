package com.dergoogler.mmrl.datastore.superuser

data class SuperUserMenuCompat(
    val showSystemApps: Boolean,
    val alwaysShowShell: Boolean,
    val descending: Boolean,
) {
    constructor(original: SuperUserMenu) : this(
        showSystemApps = original.showSystemApps,
        alwaysShowShell = original.alwaysShowShell,
        descending = original.descending
    )

    fun toProto(): SuperUserMenu = SuperUserMenu.newBuilder()
        .setShowSystemApps(showSystemApps)
        .setAlwaysShowShell(alwaysShowShell)
        .setDescending(descending)
        .build()

    companion object {
        fun default() = SuperUserMenuCompat(
            showSystemApps = false,
            alwaysShowShell = true,
            descending = false
        )
    }
}