package org.garmento.tryon.services.catalogs

import org.garmento.tryon.utils.throwIfInvalidUuidValue
import java.util.*

data class CatalogId(val value: String = UUID.randomUUID().toString()) {
    init {
        throwIfInvalidUuidValue(value, javaClass)
    }
}
