package org.garmento.tryon.utils

import java.util.*

fun <T : Any> throwIfInvalidUuidValue(value: String, javaClass: Class<T>) {
    try {
        UUID.fromString(value)
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("(${javaClass.canonicalName}) ID $value is invalid")
    }
}