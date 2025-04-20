package com.dergoogler.mmrl.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ui.component.listItem.ListItem
import com.dergoogler.mmrl.ui.component.listItem.ListItemDefaults
import com.dergoogler.mmrl.ui.component.listItem.ListItemTextStyle
import com.dergoogler.mmrl.webui.model.WebUIPermissions

@Composable
fun PermissionItem(
    permissions: List<String>,
    contentPaddingValues: PaddingValues = PaddingValues(vertical = 16.dp, horizontal = 25.dp),
    itemTextStyle: ListItemTextStyle = ListItemDefaults.itemStyle,
) = permissions.forEach {
    val permission = getPermissionDetails(it)
    permission.nullable { p ->
        ListItem(
            itemTextStyle = itemTextStyle,
            contentPaddingValues = contentPaddingValues,
            title = stringResource(id = p.title),
            desc = stringResource(id = p.desc),
            base = {
                labels = p.rootSolutions
            }
        )
    }
}

data class Permission(
    @StringRes val title: Int,
    @StringRes val desc: Int,
    val rootSolutions: List<@Composable RowScope.() -> Unit>? = null,
)

@Composable
private fun getPermissionDetails(id: String): Permission? {
    val permissionMap = mapOf(
        "magisk.permission.SERVICE" to Permission(
            R.string.view_module_features_service,
            R.string.view_module_features_service_sub
        ),
        "magisk.permission.POST_FS_DATA" to Permission(
            R.string.view_module_features_post_fs_data,
            R.string.view_module_features_post_fs_data_sub
        ),
        "magisk.permission.RESETPROP" to Permission(
            R.string.view_module_features_system_properties,
            R.string.view_module_features_resetprop_sub
        ),
        "magisk.permission.SEPOLICY" to Permission(
            R.string.view_module_features_selinux_policy, R.string.view_module_features_sepolicy_sub
        ),
        "magisk.permission.ZYGISK" to Permission(
            R.string.view_module_features_zygisk,
            R.string.view_module_features_zygisk_sub
        ),
        "magisk.permission.ACTION" to Permission(
            R.string.view_module_features_action,
            R.string.view_module_features_action_sub
        ),
        "kernelsu.permission.WEBUI" to Permission(
            R.string.view_module_features_webui,
            R.string.view_module_features_webui_sub,
            listOf { KernelSuLabel(); APatchLabel(); MMRLLabel() }
        ),
        "kernelsu.permission.POST_MOUNT" to Permission(
            R.string.view_module_features_post_mount,
            R.string.view_module_features_postmount_sub,
            listOf { KernelSuLabel(); APatchLabel() }
        ),
        "kernelsu.permission.BOOT_COMPLETED" to Permission(
            R.string.view_module_features_boot_completed,
            R.string.view_module_features_bootcompleted_sub,
            listOf { KernelSuLabel(); APatchLabel() }
        ),
        "mmrl.permission.WEBUI" to Permission(
            R.string.view_module_features_mmrl_webui,
            R.string.view_module_features_mmrl_webui_sub,
            listOf { MMRLLabel() }
        ),
        "mmrl.permission.WEBUI_CONFIG" to Permission(
            R.string.view_module_features_mmrl_webui_config,
            R.string.view_module_features_mmrl_webui_config_sub,
            listOf { MMRLLabel() }
        ),
        "mmrl.permission.APKS" to Permission(
            R.string.view_module_features_apks,
            R.string.view_module_features_apks_sub
        ),
        WebUIPermissions.PLUGIN_DEX_LOADER to Permission(
            R.string.view_module_features_dex_loader,
            R.string.view_module_features_dex_loader_sub,
            listOf { MMRLLabel() }
        ),
    )

    return permissionMap[id]
}