package org.garmento.tryon.services.catalogs

import org.garmento.tryon.services.assets.Image
import org.garmento.tryon.services.assets.ImageId
import org.garmento.tryon.services.users.UserId


data class Catalog(
    val name: String,
    val createdBy: UserId,
    val id: CatalogId = CatalogId(),
    val status: CatalogStatus = CatalogStatus.Draft,
    val imageAssets: Set<Image> = setOf(),
) {
    fun addImages(images: List<Image>) = copy(imageAssets = imageAssets + images)

    fun removeImages(imageIds: List<ImageId>) = copy(
        imageAssets = imageAssets.filter { it.id !in imageIds }.toSet()
    )

    fun submitted() = copy(status = CatalogStatus.Submitted)
    fun approved() = copy(status = CatalogStatus.Approved)
    fun unapproved() = copy(status = CatalogStatus.Submitted)
    fun published() = copy(status = CatalogStatus.Published)
}

