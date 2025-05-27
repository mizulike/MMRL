package com.dergoogler.mmrl.webui.model

import com.dergoogler.mmrl.webui.interfaces.WXOptions
import com.dergoogler.mmrl.webui.interfaces.WXInterface

/**
 * Represents a JavaScript interface that can be exposed to a web view.
 *
 * This class encapsulates the necessary information to create and manage an instance of a
 * Kotlin class that will be accessible from JavaScript running within a web view.
 *
 * @param T The type of the WebUIInterface that this interface will create.
 * @property clazz The Class object representing the Kotlin class implementing WebUIInterface.
 * @property initargs Optional arguments to be passed to the constructor of the class. If null, the constructor taking a WXOptions object will be used.
 * @property parameterTypes Optional array of parameter types for the constructor. If null, the constructor taking a WXOptions object will be used.
 */
data class JavaScriptInterface<T : WXInterface>(
    val clazz: Class<T>,
    val initargs: Array<Any>? = null,
    val parameterTypes: Array<Class<*>>? = null,
) {
    data class Instance(
        val name: String,
        val instance: Any,
    )

    fun createNew(wxOptions: WXOptions): Instance {
        val constructor = if (parameterTypes != null) {
            clazz.getDeclaredConstructor(WXOptions::class.java, *parameterTypes)
        } else {
            clazz.getDeclaredConstructor(WXOptions::class.java)
        }

        val newInstance = if (initargs != null) {
            constructor.newInstance(wxOptions, *initargs)
        } else {
            constructor.newInstance(wxOptions)
        }

        val name = (newInstance as WXInterface).name

        return Instance(name, newInstance)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JavaScriptInterface<*>

        if (clazz != other.clazz) return false
        if (!initargs.contentEquals(other.initargs)) return false
        if (!parameterTypes.contentEquals(other.parameterTypes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clazz.hashCode()
        result = 31 * result + initargs.contentHashCode()
        result = 31 * result + parameterTypes.contentHashCode()
        return result
    }
}