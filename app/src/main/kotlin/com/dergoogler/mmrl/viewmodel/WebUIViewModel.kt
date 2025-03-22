package com.dergoogler.mmrl.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.webkit.WebView
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.Compat
import com.dergoogler.mmrl.Platform
import com.dergoogler.mmrl.app.Const
import com.dergoogler.mmrl.datastore.model.developerMode
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import com.dergoogler.mmrl.repository.UserPreferencesRepository
import com.dergoogler.mmrl.utils.file.SuFile
import com.dergoogler.webui.plugin.Instance
import com.dergoogler.webui.plugin.Plugin
import com.dergoogler.webui.webUiConfig
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dalvik.system.InMemoryDexClassLoader
import dev.dergoogler.mmrl.compat.ext.isLocalWifiUrl
import dev.dergoogler.mmrl.compat.viewmodel.MMRLViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.nio.ByteBuffer


@HiltViewModel(assistedFactory = WebUIViewModel.Factory::class)
class WebUIViewModel @AssistedInject constructor(
    @Assisted val modId: String,
    application: Application,
    localRepository: LocalRepository,
    modulesRepository: ModulesRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : MMRLViewModel(
    application,
    localRepository,
    modulesRepository,
    userPreferencesRepository
) {
    private val userPrefs = runBlocking { userPreferencesRepository.data.first() }

    val isProviderAlive get() = Compat.isAlive

    val versionName: String
        get() = Compat.get("") {
            with(moduleManager) { version }
        }

    val versionCode: Int
        get() = Compat.get(-1) {
            with(moduleManager) { versionCode }
        }

    val platform: Platform
        get() = Compat.get(Platform.EMPTY) {
            platform
        }

    private val moduleDir = SuFile("/data/adb/modules", modId)
    val webRoot = SuFile(moduleDir, "webroot")

    private val configFile = SuFile(webRoot, "config.mmrl.json")
    private val pluginDir = SuFile(webRoot, "plugins")

    val sanitizedModId: String
        get() {
            return modId.replace(Regex("[^a-zA-Z0-9._]"), "_")
        }

    val sanitizedModIdWithFile
        get(): String {
            return "$${
                when {
                    sanitizedModId.length >= 2 -> sanitizedModId[0].uppercase() + sanitizedModId[1]
                    sanitizedModId.isNotEmpty() -> sanitizedModId[0].uppercase()
                    else -> ""
                }
            }File"
        }

    var dialogRequestAdvancedKernelSUAPI by mutableStateOf(false)
    var dialogRequestFileSystemAPI by mutableStateOf(false)

    fun isDomainSafe(domain: String): Boolean {
        val default = Const.WEBUI_DOMAIN_SAFE_REGEX.matches(domain)
        return userPrefs.developerMode({ useWebUiDevUrl }, default) {
            webUiDevUrl.isLocalWifiUrl()
        }
    }

    val config = webUiConfig(modId)

    private val indexFile
        get() = webRoot.list()
            .filter { !SuFile(it).isFile }
            .sortedWith(compareByDescending {
                when {
                    it.matches(Regex(".*\\.mmrl\\.(html|htm)\$")) -> 2
                    it.matches(Regex("index\\.(html|htm)\$")) -> 1
                    else -> 0
                }
            })
            .firstOrNull()

    val domainUrl
        get(): String {
            val default = when {
                BuildConfig.VERSION_CODE < config.require.version.required -> "https://mui.kernelsu.org/mmrl/assets/webui/requireNewVersion.html?versionCode=${config.require.version.required}&supportText=${config.require.version.supportText}&supportLink=${config.require.version.supportLink}"
                else -> "https://mui.kernelsu.org/$indexFile"
            }

            return userPrefs.developerMode({ useWebUiDevUrl }, default) {
                webUiDevUrl
            }
        }

    var recomposeCount by mutableIntStateOf(0)
    var hasRequestedAdvancedKernelSUAPI by mutableStateOf(false)
    var hasRequestFileSystemAPI by mutableStateOf(false)

    var topInset by mutableStateOf<Int?>(null)
        private set
    var bottomInset by mutableStateOf<Int?>(null)
        private set
    var leftInset by mutableStateOf<Int?>(null)
        private set
    var rightInset by mutableStateOf<Int?>(null)
        private set

    fun initInsets(density: Density, layoutDirection: LayoutDirection, insets: WindowInsets) {
        topInset = (insets.getTop(density) / density.density).toInt()
        bottomInset = (insets.getBottom(density) / density.density).toInt()
        leftInset = (insets.getLeft(density, layoutDirection) / density.density).toInt()
        rightInset = (insets.getRight(density, layoutDirection) / density.density).toInt()
    }

    @SuppressLint("JavascriptInterface")
    fun loadDexPluginsFromMemory(context: Context, webView: WebView) {
        if (config.plugins.isEmpty()) {
            Timber.d("config.mmrl.json plugins for $modId is invalid or empty!")
            return
        }

        if (!pluginDir.exists()) {
            Timber.d("$modId has no plugins.")
            return
        }

        val pluginDirFiles = pluginDir.listFiles { file ->
            file.extension == "dex" || file.extension == "jar" || file.extension == "apk"
        }

        pluginDirFiles.forEach {
            val dexPath = it.path

            Timber.d("Loading plugin from dex file $dexPath")

            if (!it.isFile) {
                return@forEach
            }

            try {
                val dexFileParcel = it.readBytes()
                val loader =
                    InMemoryDexClassLoader(ByteBuffer.wrap(dexFileParcel), context.classLoader)

                config.plugins.forEach { className ->
                    try {
                        val clazz = loader.loadClass(className)

                        val instance = clazz.getPluginMethod<Instance>(
                            name = "instance",
                            listOf(Plugin::class.java) to listOf(
                                Plugin(
                                    modId,
                                    context,
                                    webView,
                                    Compat.fileManager,
                                    platform,
                                    isProviderAlive
                                )
                            ),
                            emptyList<Class<*>>() to emptyList()
                        )

                        if (instance == null) {
                            Timber.e("Class $className does not have an instance method")
                            return
                        }

                        val targetModules = instance.targetModules
                        val instanceName = instance.name
                        val instanceObject = instance.instance

                        if (modId in targetModules) {
                            Timber.d(
                                "Skipping plugin $className with reserved for ${
                                    targetModules.joinToString(
                                        ","
                                    )
                                }. Not for $modId"
                            )
                            return
                        }

                        Timber.d("Added plugin $instanceName from dex file $dexPath")

                        webView.addJavascriptInterface(
                            instanceObject,
                            instanceName
                        )
                    } catch (e: ClassNotFoundException) {
                        Timber.e("Class $className not found in dex file $dexPath")
                    } catch (e: Exception) {
                        Timber.e(
                            "Error instantiating class $className from dex file $dexPath",
                            e
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e("Error loading plugin from dex file: $dexPath", e)
            }
        }
    }

    private fun Class<*>.setPluginField(name: String, value: Any) {
        try {
            val field = getDeclaredField(name)
            field.isAccessible = true
            field.set(null, value)
        } catch (e: Exception) {
            Timber.w("Failed to set field $name in $modId")
        }
    }

    private inline fun <reified T> Class<*>.getPluginField(vararg names: String): T? {
        for (name in names) {
            try {
                val field = getDeclaredField(name).apply { isAccessible = true }
                return field.get(null) as? T
            } catch (e: NoSuchFieldException) {
                //
            } catch (e: IllegalAccessException) {
                //
            }
        }
        return null
    }


    private inline fun <reified T> Class<*>.getPluginField(name: String, instance: Any): T? =
        try {
            getDeclaredField(name).apply { isAccessible = true }.get(instance) as? T
        } catch (e: Exception) {
            null
        }

    private inline fun <reified T> Class<*>.getPluginMethod(
        name: String,
        parameterTypes: List<Class<*>>,
        args: List<Any>,
    ): T? =
        try {
            getDeclaredMethod(name, *parameterTypes.toTypedArray()).apply { isAccessible = true }
                .invoke(null, *args.toTypedArray()) as? T
        } catch (e: Exception) {
            null
        }

    private inline fun <reified T> Class<*>.getPluginField(name: String): T? = try {
        getDeclaredField(name).apply { isAccessible = true }.get(null) as? T
    } catch (e: Exception) {
        null
    }

    private inline fun <reified T> Class<*>.getPluginMethod(
        name: String,
        vararg parameterSets: Pair<List<Class<*>>, List<Any?>>,
    ): T? {
        val method = declaredMethods.find { it.name == name }
            ?: return null.also { Timber.w("Method $name not found in $this") }

        for ((params, args) in parameterSets) {
            try {
                if (method.parameterTypes.size == params.size && method.parameterTypes.zip(params)
                        .all { it.first.isAssignableFrom(it.second) }
                ) {
                    return method.apply { isAccessible = true }
                        .invoke(null, *args.toTypedArray()) as? T
                }
            } catch (e: Exception) {
                Timber.i("Skipping $name with parameters ${params.joinToString()}: ${e.message}")
            }
        }
        return null
    }


    @AssistedFactory
    interface Factory {
        fun create(
            modId: String,
        ): WebUIViewModel
    }
}


