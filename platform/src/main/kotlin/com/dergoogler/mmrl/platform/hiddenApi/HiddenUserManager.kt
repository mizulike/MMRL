package com.dergoogler.mmrl.platform.hiddenApi

import android.content.Context
import android.content.pm.UserInfo
import android.os.IUserManager
import com.dergoogler.mmrl.platform.Platform.Companion.getSystemService
import com.dergoogler.mmrl.platform.stub.IServiceManager

class HiddenUserManager(
    private val service: IServiceManager
) {
    private val userManager by lazy {
        IUserManager.Stub.asInterface(
            service.getSystemService(Context.USER_SERVICE)
        )
    }

    fun getUsers(): List<UserInfo> {
        return userManager.getUsers(true, true, true)
    }

    fun getUserInfo(userId: Int): UserInfo {
        return userManager.getUserInfo(userId)
    }
}