package com.dergoogler.mmrl.ui.component

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.ui.component.listItem.ListItem

@Composable
fun SELinuxStatus(
    contentPaddingValues: PaddingValues = PaddingValues(vertical = 16.dp, horizontal = 25.dp),
) {
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

    ListItem(
        contentPaddingValues = contentPaddingValues,
        icon = R.drawable.shield_bolt,
        title = stringResource(id = R.string.selinux_status),
        desc = stringResource(id = seLinuxStatus)
    )
}