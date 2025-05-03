package com.dergoogler.mmrl.ui.activity.terminal.action

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.dergoogler.mmrl.ext.tmpDir
import com.dergoogler.mmrl.viewmodel.ActionViewModel
import com.dergoogler.mmrl.ui.activity.MMRLComponentActivity
import com.dergoogler.mmrl.ui.activity.setBaseContent
import kotlinx.coroutines.launch
import timber.log.Timber

class ActionActivity : MMRLComponentActivity() {
    private val viewModel: ActionViewModel by viewModels()

    override val windowFlags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("ActionActivity onCreate")
        super.onCreate(savedInstanceState)

        val modId = intent.getStringExtra("MOD_ID")

        if (modId.isNullOrEmpty()) {
            finish()
        } else {
            Timber.d("ActionActivity onCreate: $modId")
            initAction(modId)
        }

        setBaseContent {
            ActionScreen()
        }
    }

    override fun onDestroy() {
        Timber.d("InstallActivity onDestroy")
        tmpDir.deleteRecursively()
        super.onDestroy()
    }

    private fun initAction(modId: String) {
        lifecycleScope.launch {
            viewModel.runAction(
                modId = modId,
            )
        }
    }
}