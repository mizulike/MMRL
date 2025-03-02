package dev.dergoogler.mmrl.compat.impl

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.UserManager
import dev.dergoogler.mmrl.compat.stub.IKsuService
import rikka.parcelablelist.ParcelableListSlice


class KsuServiceImpl(
    private val context: Context,
) : IKsuService.Stub() {
    override fun getPackages(flags: Int): ParcelableListSlice<PackageInfo> {
        val list: List<PackageInfo> = getInstalledPackagesAll(flags)
        return ParcelableListSlice(list)
    }

    private fun getUserIds(): List<Int> {
        val result: MutableList<Int> = ArrayList()
        val um = context.getSystemService(Context.USER_SERVICE) as UserManager
        val userProfiles = um.userProfiles
        for (userProfile in userProfiles) {
            val userId = userProfile.hashCode()
            result.add(userId)
        }
        return result
    }

    private fun getInstalledPackagesAll(flags: Int): ArrayList<PackageInfo> {
        val packages = ArrayList<PackageInfo>()
        for (userId in getUserIds()) {
            packages.addAll(getInstalledPackagesAsUser(flags, userId))
        }
        return packages
    }

    private fun getInstalledPackagesAsUser(flags: Int, userId: Int): List<PackageInfo> {
        try {
            val pm: PackageManager = context.packageManager
            val getInstalledPackagesAsUser = pm.javaClass.getDeclaredMethod(
                "getInstalledPackagesAsUser",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType
            )
            return getInstalledPackagesAsUser.invoke(pm, flags, userId) as List<PackageInfo>
        } catch (e: Throwable) {
            //
        }

        return ArrayList()
    }

}