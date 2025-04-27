package com.dergoogler.mmrl.platform.content


import android.os.IBinder
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import com.dergoogler.mmrl.platform.stub.IServiceManager

interface IService {
    val name: String
    fun create(manager: IServiceManager): IBinder
}

@Parcelize
class Service<T : IService>(
    private val cls: Class<T>,
) : Parcelable, IService {
    @IgnoredOnParcel
    private val original by lazy {
        cls.getDeclaredConstructor().let {
            it.isAccessible = true
            it.newInstance()
        }
    }

    override val name: String get() = original.name

    override fun create(manager: IServiceManager): IBinder {
        return original.create(manager)
    }
}