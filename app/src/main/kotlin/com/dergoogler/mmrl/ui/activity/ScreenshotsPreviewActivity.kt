package com.dergoogler.mmrl.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ui.activity.terminal.install.InstallScreen
import com.dergoogler.mmrl.ui.component.ConfirmDialog
import dev.dergoogler.mmrl.compat.BuildCompat
import dev.dergoogler.mmrl.compat.activity.MMRLComponentActivity
import dev.dergoogler.mmrl.compat.activity.setBaseContent
import dev.dergoogler.mmrl.compat.ext.tmpDir
import kotlinx.coroutines.launch
import timber.log.Timber

class ScreenshotsPreviewActivity : MMRLComponentActivity() {
    override val windowFlags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("ScreenshotsPreviewActivity onCreate")
        super.onCreate(savedInstanceState)


        val urls: ArrayList<String>? = intent.getStringArrayListExtra("urls")
        val index: Int = intent.getIntExtra("index", 0)

        if (urls.isNullOrEmpty()) {
            finish()
            return
        }

        setBaseContent {
            ScreenshotsPreviewScreen(index, urls)
        }
    }

    override fun onDestroy() {
        Timber.d("InstallActivity onDestroy")
        tmpDir.deleteRecursively()
        super.onDestroy()
    }

    companion object {
        fun start(context: Context, url: List<String>, index: Int) {
            val intent = Intent(context, ScreenshotsPreviewActivity::class.java)
                .apply {
                    putExtra("index", index)
                    putStringArrayListExtra("urls", ArrayList(url))
                }

            context.startActivity(intent)
        }

        fun start(context: Context, url: String, index: Int) {
            start(context, listOf(url), index)
        }
    }
}