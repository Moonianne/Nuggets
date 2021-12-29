package com.gerardojim.nuggetscalculator.ui.main

import arrow.core.Option
import arrow.core.getOrElse

/**
 * Returns the [Option]'s value. This method should only be used in cases where the [Option] is not
 * expected to be empty.
 *
 * @throws NoSuchElementException if the [Option] is empty.
 */
fun <T> Option<T>.get(): T {
    return getOrElse { throw NoSuchElementException("None.get") }
}
