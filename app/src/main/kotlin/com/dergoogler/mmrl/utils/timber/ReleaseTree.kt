package com.dergoogler.mmrl.utils.timber

import android.util.Log
import timber.log.Timber

class ReleaseTree : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }

        super.log(priority, "<MR_REL>$tag", message, t)
    }
}