package com.dergoogler.mmrl.ui.component

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.Item
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Description
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Icon
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title

@Composable
fun ListScope.SELinuxStatus() {
    val seLinuxStatus = remember {
        PlatformManager.get(R.string.selinux_status_unknown) {
            try {
                if (!isSELinuxEnabled) {
                    return@get R.string.selinux_status_disabled
                }

                if (isSELinuxEnforced) {
                    return@get R.string.selinux_status_enforcing
                }

                return@get R.string.selinux_status_permissive
            } catch (e: Exception) {
                Log.e("SELinuxStatus", "Failed to check SELinux status", e)
                return@get R.string.selinux_status_unknown
            }
        }
    }

    Item {
        Icon(painter = painterResource(R.drawable.shield_bolt))
        Title(R.string.selinux_status)
        Description(seLinuxStatus)
    }
}