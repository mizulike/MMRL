package com.dergoogler.mmrl.webui.view

import android.content.Context
import android.util.AttributeSet
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

open class WXSwipeRefresh : SwipeRefreshLayout {
    private var mView: WXView? = null

    constructor(context: Context, view: WXView) : super(context) {
        this.mView = view
        this.mView?.mSwipeView = this

        mView?.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            this.isEnabled = scrollY == 0
        }
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
}
