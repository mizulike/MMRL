package com.dergoogler.mmrl.webui.interfaces

import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.platform.hiddenApi.HiddenUserManager
import com.dergoogler.mmrl.webui.model.UMUserInfo
import com.dergoogler.mmrl.webui.moshi

@Keep
class UserManagerInterface(wxOptions: WXOptions) : WXInterface(wxOptions) {
    private val um get(): HiddenUserManager = PlatformManager.userManager

    override var name: String = "\$userManager"
    override var tag: String = "UserManagerInterface"

    @JavascriptInterface
    fun getUserCount(): Int = um.getUsers().size

    @JavascriptInterface
    fun getUsers(): String {
        val uis = um.getUsers()
        val uisMapped = uis.map {
            UMUserInfo(
                name = it.name,
                id = it.id,
                isPrimary = it.isPrimary,
                isAdmin = it.isAdmin,
                isEnabled = it.isEnabled
            )
        }
        val adapter = moshi.adapter(List::class.java)
        return adapter.toJson(uisMapped)
    }

    @get:JavascriptInterface
    val myUserId get(): Int = um.myUserId

    @JavascriptInterface
    fun isSameUser(uid1: Int, uid2: Int): Boolean = um.isSameUser(uid1, uid2)

    @JavascriptInterface
    fun getUserInfo(userId: Int): UMUserInfo {
        val ui = um.getUserInfo(userId)

        return UMUserInfo(
            name = ui.name,
            id = ui.id,
            isPrimary = ui.isPrimary,
            isAdmin = ui.isAdmin,
            isEnabled = ui.isEnabled
        )
    }
}