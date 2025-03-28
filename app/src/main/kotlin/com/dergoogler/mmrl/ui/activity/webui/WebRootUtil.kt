package com.dergoogler.mmrl.ui.activity.webui

object WebRootUtil {
    fun cssInsets(top: Int?, bottom: Int?, left: Int?, right: Int?) = """
        :root {
            --safe-area-inset-top: ${top ?: 0}px;
            --safe-area-inset-right: ${right ?: 0}px;
            --safe-area-inset-bottom: ${bottom ?: 0}px;
            --safe-area-inset-left: ${left ?: 0}px;
            --window-inset-top: var(--safe-area-inset-top);
            --window-inset-bottom: var(--safe-area-inset-bottom);
            --window-inset-left: var(--safe-area-inset-left);
            --window-inset-right: var(--safe-area-inset-right);
        }
    """
}