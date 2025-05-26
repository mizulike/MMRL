package com.dergoogler.mmrl.webui.util

import android.webkit.WebMessage

enum class PostWindowEventMessage(val message: WebMessage) {
    ON_BACK(WebMessage("wxBack")),
    ON_RESUME(WebMessage("wxResume")),
    ON_VIEW_RESUME(WebMessage("wxViewResume")),
    ON_VIEW_PAUSE(WebMessage("wxViewPause")),
    ON_PAUSE(WebMessage("wxPause")),
    ON_DESTROY(WebMessage("wxDestroy"))
}