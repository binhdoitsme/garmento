package org.garmento.tryon.adapters.db.catalogs

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class CatalogItemRecordId(
    @Column(name = "catalog_id") val catalogId: String,
    @Column(name = "image_id") val imageId: String
)