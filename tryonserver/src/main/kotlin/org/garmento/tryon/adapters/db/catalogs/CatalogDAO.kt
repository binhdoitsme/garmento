package org.garmento.tryon.adapters.db.catalogs

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CatalogDAO : JpaRepository<CatalogRecord, String> {
    fun findByCreatedBy(createdBy: String, pageable: Pageable): Page<CatalogRecord>
}

