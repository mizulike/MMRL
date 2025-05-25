package com.dergoogler.mmrl.platform.model

import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.platform.util.moshi
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Locale

@JsonClass(generateAdapter = true)
data class ModuleConfig(
    @Json(name = "name")
    val nam: Any? = null,
    @Json(name = "description")
    val desc: Any? = null,
    @Json(name = "webui-engine")
    val webuiEngine: String? = "wx",
) {
    private fun getLocale(prop: Any?, default: String = "en"): String? {
        val locale = Locale.getDefault().language

        return try {
            when (prop) {
                is String -> prop
                is Map<*, *> -> prop[locale] as? String ?: prop[default] as? String
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    val description
        get(): String? = getLocale(desc)

    val name
        get(): String? = getLocale(nam)

    companion object {
        val ModId.asModuleConfig get(): ModuleConfig = id.asModuleConfig
        val String.asModuleConfig
            get(): ModuleConfig {
                val moduleDir = SuFile("/data/adb/modules", this)
                val configFile =
                    moduleDir.fromPaths("config.json", "config.mmrl.json") ?: return ModuleConfig()
                val json = configFile.readText()
                val jsonAdapter = moshi.adapter(ModuleConfig::class.java)
                return jsonAdapter.fromJson(json) ?: ModuleConfig()
            }
    }
}