package org.garmento.tryon.adapters.db.catalogs

import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity

@Entity(name = "catalog_items")
data class CatalogItemRecord(
    @EmbeddedId val id: CatalogItemRecordId
)