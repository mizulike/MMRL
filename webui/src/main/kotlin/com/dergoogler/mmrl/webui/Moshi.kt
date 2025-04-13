package com.dergoogler.mmrl.webui

import com.squareup.moshi.Moshi

val moshi: Moshi
    get() = Moshi.Builder()
        .build()