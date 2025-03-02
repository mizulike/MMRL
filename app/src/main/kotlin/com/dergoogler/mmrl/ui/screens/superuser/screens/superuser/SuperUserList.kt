package com.dergoogler.mmrl.ui.screens.superuser.screens.superuser

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ui.component.LabelItem
import com.dergoogler.mmrl.ui.component.listItem.ListAppItem
import com.dergoogler.mmrl.ui.component.listItem.ListItemDefaults
import com.dergoogler.mmrl.ui.providable.LocalNavController
import dev.dergoogler.mmrl.compat.content.AppInfo

@Composable
fun SuperUserList(
    list: List<AppInfo>,
    state: LazyListState,
    shouldUnmount: (Int) -> Boolean,
) {
    val navController = LocalNavController.current

    val listItemStyle = ListItemDefaults.itemStyle.copy(iconSize = 48.dp)

    LazyColumn(
        state = state,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(
            items = list,
            key = { it.packageName }
        ) { app ->
            ListAppItem(
                itemTextStyle = listItemStyle,
                app = app,
                onClick = {
//                    navController.navigateSingleTopTo(app)
                },
                base = {
                    labels = listOf {
                        AllowSuProfileLabel(app, shouldUnmount(app.uid)); CustomProfileLabel(app)
                    }
                }
            )
        }
    }
}

@Composable
fun CustomProfileLabel(app: AppInfo) {
    if (app.hasCustomProfile) {
        LabelItem(
            text = "CUSTOM",
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}


@Composable
fun AllowSuProfileLabel(app: AppInfo, shouldUnmount: Boolean) {
    if (app.allowSu) {
        LabelItem(
            text = "ROOT",
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    } else {
        if (shouldUnmount) {
            LabelItem(
                text = "UMOUNT",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}