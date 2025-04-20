package com.dergoogler.mmrl.ext

import java.util.Locale

fun Int.toDollars(divideBy: Double = 100.0): String {
    return "$%.2f".format(Locale.getDefault(), this / divideBy)
}