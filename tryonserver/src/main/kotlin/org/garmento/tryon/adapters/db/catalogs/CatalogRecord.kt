package org.garmento.tryon.adapters.db.catalogs

import jakarta.persistence.*
import org.garmento.tryon.services.assets.Image
import org.garmento.tryon.services.assets.ImageId
import org.garmento.tryon.services.catalogs.Catalog
import org.garmento.tryon.services.catalogs.CatalogId
import org.garmento.tryon.services.catalogs.CatalogStatus
import org.garmento.tryon.services.users.UserId

@Entity(name = "catalogs")
data class CatalogRecord(
    @Id val id: String,
    @Column(name = "name") val name: String,
    @Column(name = "status") val status: String,
    @Column(name = "created_by") val createdBy: String,
    @OneToMany(fetch = FetchType.LAZY) @JoinColumn(
        name = "catalog_id",
        updatable = false
    ) val items: List<CatalogItemRecord>,
) {
    fun toDomain(images: Map<ImageId, Image>) = Catalog(
        id = CatalogId(id),
        name = name,
        status = CatalogStatus.fromString(status),
        createdBy = UserId(createdBy),
        imageAssets = items.map {
            Image(ImageId(it.id.imageId), images[ImageId(it.id.imageId)]?.url!!)
        }.toSet(),
    )

}