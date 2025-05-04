package com.dergoogler.mmrl.model.local

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.datastore.model.WorkingMode
import com.dergoogler.mmrl.ui.component.dialog.RadioOptionItem

data class FeaturedManager(
    @StringRes val name: Int,
    @DrawableRes val icon: Int,
    val workingMode: WorkingMode
) {
    @Composable
    fun toRadioOption() = RadioOptionItem(
        title = stringResource(name),
        value = workingMode
    )

    companion object {
        val managers
            get() = listOf(
                FeaturedManager(
                    name = R.string.working_mode_magisk_title,
                    icon = com.dergoogler.mmrl.ui.R.drawable.magisk_logo,
                    workingMode = WorkingMode.MODE_MAGISK,
                ),

                FeaturedManager(
                    name = R.string.working_mode_kernelsu_title,
                    icon = com.dergoogler.mmrl.ui.R.drawable.kernelsu_logo,
                    workingMode = WorkingMode.MODE_KERNEL_SU,
                ),

                FeaturedManager(
                    name = R.string.working_mode_kernelsu_next_title,
                    icon = com.dergoogler.mmrl.ui.R.drawable.kernelsu_next_logo,
                    workingMode = WorkingMode.MODE_KERNEL_SU_NEXT,
                ),

                FeaturedManager(
                    name = R.string.working_mode_apatch_title,
                    icon = com.dergoogler.mmrl.ui.R.drawable.brand_android,
                    workingMode = WorkingMode.MODE_APATCH
                ),

                FeaturedManager(
                    name = R.string.setup_non_root_title,
                    icon = R.drawable.shield_lock,
                    workingMode = WorkingMode.MODE_NON_ROOT
                ),
            )
    }
}

