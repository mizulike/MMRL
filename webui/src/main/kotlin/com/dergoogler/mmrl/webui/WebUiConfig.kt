package com.dergoogler.mmrl.webui

import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.webui.model.ModId
import com.dergoogler.mmrl.webui.model.WebUIConfig

/**
 * Loads the configuration file located at `/data/adb/modules/<ID>/webroot/config.json` 
 * to configure the general behavior of the WebUI.
 *
 * This function can also be invoked using the extension function `String.toWebUiConfig()` 
 * on module IDs.
 *
 * If the file doesn't exists or cannot be read, an empty [WebUIConfig] object will be returned.
 *
 * @param modId The module ID used to locate the configuration file.
 * @return A [WebUIConfig] object containing the loaded configuration.
 */
fun webUiConfig(modId: ModId): WebUIConfig {
    val moduleDir = SuFile("/data/adb/modules", modId.id)
    val webRoot = SuFile(moduleDir, "webroot")
    val configFile = SuFile(webRoot, "config.json")

    if (!configFile.exists()) {
        return WebUIConfig()
    }

    val json = configFile.readText()
    val jsonAdapter = moshi.adapter(WebUIConfig::class.java)
    return jsonAdapter.fromJson(json) ?: WebUIConfig()
}