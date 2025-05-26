package com.dergoogler.mmrl.webui.model

import com.dergoogler.mmrl.webui.util.WebUIOptions
import com.dergoogler.mmrl.webui.view.WXView

data class Renderer(
    val options: WebUIOptions,
    val view: WXView = WXView(options),
)