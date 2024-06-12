package org.garmento.tryon.services.tryon

import org.garmento.tryon.utils.throwIfInvalidUuidValue
import java.util.*

data class TryOnJobId(val value: String = UUID.randomUUID().toString()) {
    init {
        throwIfInvalidUuidValue(value, javaClass)
    }
}
