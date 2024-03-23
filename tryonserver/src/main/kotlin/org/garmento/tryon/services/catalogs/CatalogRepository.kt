package org.garmento.tryon.services.catalogs

import org.garmento.tryon.services.users.UserId

interface CatalogRepository {
    fun save(catalog: Catalog)
    fun findById(id: CatalogId): Catalog?
    fun findByUserId(userId: UserId, page: Int, pageSize: Int): List<Catalog>
    fun findAll(page: Int, pageSize: Int): List<Catalog>
    fun delete(id: CatalogId)
}
