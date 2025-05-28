package com.dergoogler.mmrl.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.dergoogler.mmrl.ui.R
import java.io.File
import java.io.InputStream

@Composable
fun Cover(
    modifier: Modifier = Modifier,
    url: String? = null,
    inputStream: InputStream? = null,
    shape: RoundedCornerShape = RoundedCornerShape(0.dp),
    aspectRatio: Float = 2.048f,
) {
    val context = LocalContext.current

    val model = when {
        !url.isNullOrEmpty() -> url
        inputStream != null -> remember(inputStream) { inputStream }
        else -> null
    }

    val painter = rememberAsyncImagePainter(
        model = model?.let {
            ImageRequest.Builder(context)
                .data(it)
                .apply {
                    if (it is InputStream) {
                        diskCachePolicy(CachePolicy.DISABLED)
                        memoryCachePolicy(CachePolicy.DISABLED)
                    } else {
                        memoryCacheKey(it.toString())
                        diskCacheKey(it.toString())
                        diskCachePolicy(CachePolicy.ENABLED)
                        memoryCachePolicy(CachePolicy.ENABLED)
                    }
                }
                .build()
        } ?: R.drawable.alert_triangle
    )

    if (painter.state !is AsyncImagePainter.State.Error && model != null) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .aspectRatio(aspectRatio)
                .then(modifier)
        )
    } else {
        Logo(
            icon = R.drawable.alert_triangle,
            shape = shape,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .then(modifier)
        )
    }
}