package com.dergoogler.mmrl.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ui.component.listItem.ListItem
import com.dergoogler.mmrl.utils.createRootShell
import com.topjohnwu.superuser.Shell

private val Shell.seLinuxStatus: String
    @Composable get() {
        val list = ArrayList<String>()
        val result = this.use {
            it.newJob().add("getenforce").to(list, list).exec()
        }
        val output = result.out.joinToString("\n").trim()

        if (result.isSuccess) {
            return when (output) {
                "Enforcing" -> stringResource(R.string.selinux_status_enforcing)
                "Permissive" -> stringResource(R.string.selinux_status_permissive)
                "Disabled" -> stringResource(R.string.selinux_status_disabled)
                else -> stringResource(R.string.selinux_status_unknown)
            }
        }

        return if (output.endsWith("Permission denied")) {
            stringResource(R.string.selinux_status_enforcing)
        } else {
            stringResource(R.string.selinux_status_unknown)
        }
    }

@Composable
fun SELinuxStatus(
    contentPaddingValues: PaddingValues = PaddingValues(vertical = 16.dp, horizontal = 25.dp),
) {
    val shell = createRootShell(
        commands = arrayOf("sh")
    )

    ListItem(
        contentPaddingValues = contentPaddingValues,
        icon = R.drawable.shield_bolt,
        title = stringResource(id = R.string.selinux_status),
        desc = shell.seLinuxStatus
    )
}