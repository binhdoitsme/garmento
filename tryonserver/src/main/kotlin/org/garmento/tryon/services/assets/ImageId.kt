package org.garmento.tryon.services.assets

import org.garmento.tryon.utils.throwIfInvalidUuidValue
import java.util.*

data class ImageId(val value: String = UUID.randomUUID().toString()) {
    init {
        throwIfInvalidUuidValue(value, javaClass)
    }
}