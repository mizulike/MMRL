package com.dergoogler.mmrl.ext

import android.view.View
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Returns the provided parameter if the receiver is non-null; otherwise, returns null.
 *
 * @param param The value to return if the receiver is non-null.
 * @return The parameter value if the receiver is non-null, or null otherwise.
 */
infix fun <T> Any?.nullable(param: T): T? = if (this != null) param else null

/**
 * Returns the provided parameter if the Boolean receiver is non-null and true; otherwise, returns null.
 *
 * @param param The value to return if the receiver is non-null and true.
 * @return The parameter value if the receiver is non-null and true, or null otherwise.
 */
infix fun <T> Boolean?.nullable(param: T): T? = if (this != null && this) param else null

/**
 * Runs the block if the receiver is non-null and returns its result; otherwise, returns null.
 *
 * @param block The function to run if the receiver is non-null.
 * @return The result of the block or null.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, R> T?.nullable(block: (T) -> R): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (this != null) block(this) else null
}

/**
 * Applies the given [block] to the receiver if it is not null, and returns the receiver itself.
 * If the receiver is null, returns null. This is similar to the standard library's `apply`
 * function, but specifically for nullable types.
 *
 * @param block A function literal with a receiver of type [T] that performs some operations on it.
 * @return The receiver [T] if it's not null and the block was applied, or null otherwise.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> T?.nullply(block: T.() -> Unit): T? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (this != null) {
        block()
        this
    } else null
}

/**
 * Runs the block if the receiver is non-null and returns its result; otherwise, returns the default value.
 *
 * @param default The value to return if the receiver is null.
 * @param block The function to run if the receiver is non-null.
 * @return The result of the block or the default value.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, R> T?.nullable(default: R, block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (this != null) block(this) else default
}

/**
 * Runs the block if the receiver is non-null and the condition is true; otherwise, returns null.
 *
 * @param condition A condition that must be true for the block to run.
 * @param block The function to run if the receiver is non-null and the condition is true.
 * @return The result of the block or null.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, R> T?.nullable(condition: Boolean, block: (T) -> R): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (condition && this != null) block(this) else null
}

/**
 * Runs the block if the receiver is non-null and satisfies the given condition; otherwise, returns null.
 *
 * @param condition A function to check if the receiver satisfies a condition.
 * @param block The function to run if the receiver is non-null and satisfies the condition.
 * @return The result of the block if the condition is met, or null otherwise.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, R> T?.nullable(condition: (T) -> Boolean, block: (T) -> R): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (this != null && condition(this)) block(this) else null
}

/**
 * Runs the block if the receiver is non-null and the condition is true; otherwise, returns the default value.
 *
 * @param condition A condition that must be true for the block to run.
 * @param default The value to return if the receiver is null or the condition is false.
 * @param block The function to run if the receiver is non-null and the condition is true.
 * @return The result of the block, or the default value.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, R> T?.nullable(condition: Boolean, default: R, block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (condition && this != null) block(this) else default
}

/**
 * Runs the block if the receiver is non-null and satisfies the given condition; otherwise, returns the default value.
 *
 * @param condition A function to check if the receiver satisfies a condition.
 * @param default The value to return if the receiver is null or does not satisfy the condition.
 * @param block The function to run if the receiver is non-null and satisfies the condition.
 * @return The result of the block if the condition is met, or the default value otherwise.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, R> T?.nullable(condition: (T) -> Boolean, default: R, block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (this != null && condition(this)) block(this) else default
}
