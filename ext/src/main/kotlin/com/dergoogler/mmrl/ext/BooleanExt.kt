package com.dergoogler.mmrl.ext

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Executes the given [block] if the Boolean value is `true`.
 *
 * This function is an inline extension function for nullable Booleans (`Boolean?`).
 * It checks if the Boolean value is exactly `true`. If it is, the provided `block`
 * is executed with the Boolean value (which will be `true`) as its argument.
 *
 * Example:
 * ```kotlin
 * val isLoggedIn: Boolean? = true
 * isLoggedIn.takeTrue {
 *     println("User is logged in.")
 * }
 *
 * val isActive: Boolean? = false
 * isActive.takeTrue {
 *     // This block will not be executed
 *     println("User is active.")
 * }
 *
 * val userStatus: Boolean? = null
 * userStatus.takeTrue {
 *     // This block will not be executed
 *     println("User status is true.")
 * }
 * ```
 *
 * @param block A lambda function that takes a non-null Boolean (which will be `true`) as input and returns Unit.
 *              This block is executed only if the receiver Boolean is `true`.
 */
inline fun Boolean?.takeTrue(block: (Boolean) -> Unit) {
    if (this == true) {
        block(this)
    }
}

/**
 * Executes the given [block] if this Boolean is `false`.
 *
 * @param block The block to execute. The receiver Boolean (which is `false`) is passed as its argument.
 */
inline fun Boolean?.takeFalse(block: (Boolean) -> Unit) {
    if (this == false) {
        block(this)
    }
}

@OptIn(ExperimentalContracts::class)
fun Boolean?.isNullOrFalse(): Boolean {
    contract {
        returns(true) implies (this@isNullOrFalse == null)
    }

    return this == null || this == false

}

fun Boolean?.isNotNullOrFalse() = !isNullOrFalse()