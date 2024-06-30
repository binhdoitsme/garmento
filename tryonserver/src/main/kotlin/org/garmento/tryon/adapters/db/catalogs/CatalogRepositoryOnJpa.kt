package org.garmento.tryon.adapters.db.catalogs

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.garmento.tryon.services.assets.ImageId
import org.garmento.tryon.services.assets.ImageRepository
import org.garmento.tryon.services.catalogs.Catalog
import org.garmento.tryon.services.catalogs.CatalogId
import org.garmento.tryon.services.catalogs.CatalogRepository
import org.garmento.tryon.services.users.UserId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class CatalogRepositoryOnJpa @Autowired constructor(
    private val catalogDAO: CatalogDAO,
    private val catalogItemDAO: CatalogItemDAO,
    private val imageRepository: ImageRepository,
    @PersistenceContext private val entityManager: EntityManager,
) : CatalogRepository {
    @Transactional
    override fun save(catalog: Catalog) {
        val catalogRecord = catalogDAO.findById(catalog.id.value).orElse(null)
            ?.apply { entityManager.detach(this) } ?: CatalogRecord(
            id = catalog.id.value,
            name = catalog.name,
            status = catalog.status.value,
            createdBy = catalog.createdBy.value,
            items = catalog.imageAssets.map {
                CatalogItemRecord(
                    id = CatalogItemRecordId(
                        catalogId = catalog.id.value,
                        imageId = it.id.value,
                    )
                )
            },
        )
        val newItems = catalog.imageAssets.map {
            CatalogItemRecord(
                id = CatalogItemRecordId(
                    catalogId = catalog.id.value,
                    imageId = it.id.value,
                )
            )
        }
        catalogItemDAO.deleteAllById_CatalogId(catalog.id.value)
        catalogItemDAO.flush()
        catalogDAO.deleteById(catalogRecord.id)
        catalogDAO.save(catalogRecord.copy(items = listOf(), status = catalog.status.value))
        catalogDAO.flush()
        catalogItemDAO.saveAllAndFlush(newItems)
    }

    private fun recordToDomain(record: CatalogRecord) =
        record.items.map { image -> ImageId(image.id.imageId) }
            .let(imageRepository::findAllById).let(record::toDomain)

    override fun findById(id: CatalogId): Catalog? =
        catalogDAO.findById(id.value).orElse(null)?.let(this::recordToDomain)

    override fun findByUserId(userId: UserId, page: Int, pageSize: Int): List<Catalog> =
        Pageable.ofSize(pageSize).withPage(page - 1).let { pageable ->
            println("page" to pageable)
            catalogDAO.findByCreatedBy(userId.value, pageable)
        }.map(this::recordToDomain).toList()


    override fun findAll(page: Int, pageSize: Int): List<Catalog> =
        catalogDAO.findAll(Pageable.ofSize(pageSize).withPage(page - 1))
            .map(this::recordToDomain).toList()

    @Transactional
    override fun delete(id: CatalogId) = catalogDAO.findById(id.value).map { catalog ->
        catalogItemDAO.deleteAll(catalog.items)
        catalogDAO.delete(catalog)
    }.get()

}