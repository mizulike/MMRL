package com.dergoogler.mmrl.platform.util

import com.squareup.moshi.Moshi

val moshi: Moshi
    get() = Moshi.Builder()
        .build()