package org.garmento.tryon.services.catalogs

import org.garmento.tryon.services.assets.Image
import org.garmento.tryon.services.assets.ImageId
import org.garmento.tryon.services.assets.ImageRepository
import org.garmento.tryon.services.users.UserId
import java.net.URI

class CatalogServices(
    private val repository: CatalogRepository,
) {
    fun create(name: String, createdBy: UserId): Catalog =
        Catalog(name = name, createdBy = createdBy).apply(repository::save)

    fun find(page: Int, pageSize: Int) = repository.findAll(page, pageSize)
    fun findBy(id: CatalogId) = repository.findById(id)
    fun findByUser(userId: UserId, page: Int, pageSize: Int) =
        repository.findByUserId(userId, page, pageSize)

    fun addImages(images: List<Image>, catalog: Catalog) =
        catalog.addImages(images).also(repository::save)

    fun removeImages(imageIds: List<ImageId>, catalog: Catalog) =
        catalog.removeImages(imageIds).also(repository::save)

    fun submit(catalog: Catalog) = catalog.submitted().also(repository::save)
    fun approve(catalog: Catalog) = catalog.approved().also(repository::save)
    fun unapprove(catalog: Catalog) = catalog.unapproved().also(repository::save)
    fun publish(catalog: Catalog) = catalog.published().also(repository::save)

    fun delete(catalogId: CatalogId) = repository.delete(catalogId)
}