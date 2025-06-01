package com.dergoogler.mmrl.ui.activity

import android.util.Log
import android.view.WindowManager
import com.dergoogler.mmrl.ext.nullply
import com.dergoogler.mmrl.ext.tmpDir
import com.dergoogler.mmrl.viewmodel.TerminalViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

open class TerminalActivity<T : TerminalViewModel> : MMRLComponentActivity() {
    protected open var terminalJob: Job? = null

    protected open lateinit var viewModel: T

    override val windowFlags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON

    protected open fun cancelJob(message: String) {
        try {
            terminalJob.nullply {
                cancel(message)
            }

            viewModel.shell.close()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel job", e)
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "$TAG onDestroy")
        tmpDir.deleteRecursively()
        cancelJob("$TAG was destroyed")
        super.onDestroy()
    }

    companion object {
        private const val TAG = "TerminalActivity"
    }
}