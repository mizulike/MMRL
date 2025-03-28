package com.dergoogler.mmrl.ui.activity.webui

object WebRootUtil {
    fun cssInsets(top: Int?, bottom: Int?, left: Int?, right: Int?) = """
        <style>
            :root {
                --safe-area-inset-top: ${top ?: 0}px;
                --safe-area-inset-right: ${right ?: 0}px;
                --safe-area-inset-bottom: ${bottom ?: 0}px;
                --safe-area-inset-left: ${left ?: 0}px;
                --window-inset-top: var(--safe-area-inset-top, 0px);
                --window-inset-bottom: var(--safe-area-inset-bottom, 0px);
                --window-inset-left: var(--safe-area-inset-left, 0px);
                --window-inset-right: var(--safe-area-inset-right, 0px);
            }
        </style>
    """
}