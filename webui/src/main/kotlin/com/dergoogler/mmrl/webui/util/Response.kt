package com.dergoogler.mmrl.webui.util

import com.dergoogler.mmrl.webui.model.WXEventHandler

enum class PostWindowEventMessage {
    WX_ON_BACK,
    WX_ON_RESUME,
    WX_ON_PAUSE;

    companion object {
        val PostWindowEventMessage.asEvent: WXEventHandler get() = WXEventHandler(this.name)
    }
}
