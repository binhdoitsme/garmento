package org.garmento.tryon.adapters.db.catalogs

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CatalogItemDAO : JpaRepository<CatalogItemRecord, CatalogItemRecordId> {
    fun deleteAllById_CatalogId(catalogId: String)
}
