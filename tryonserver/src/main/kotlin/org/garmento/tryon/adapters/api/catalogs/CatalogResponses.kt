package org.garmento.tryon.adapters.api.catalogs

import org.garmento.tryon.adapters.api.catalogs.CatalogRequests.CreatedByUser
import org.garmento.tryon.services.auth.User
import org.garmento.tryon.services.catalogs.Catalog
import java.net.URI

object CatalogResponses {
    data class CatalogResponse(
        val id: String,
        val name: String,
        val status: String,
        val createdBy: CreatedByUser,
        // first item in catalog or null if the catalog is empty
        val thumbnail: URI? = null,
    ) {
        companion object {
            fun fromDomain(catalog: Catalog, user: User) = CatalogResponse(
                id = catalog.id.value,
                name = catalog.name,
                status = catalog.status.value,
                createdBy = CreatedByUser(user.name),
                thumbnail = catalog.imageAssets.firstOrNull()?.url,
            )
        }
    }

    data class ImageResponse(
        val id: String,
        val url: URI,
    )

    data class CatalogWithImagesResponse(
        val id: String,
        val name: String,
        val status: String,
        val createdBy: CreatedByUser,
        // first item in catalog or null if the catalog is empty
        val items: List<ImageResponse>,
    ) {
        companion object {
            fun fromDomain(catalog: Catalog, user: User) =
                CatalogWithImagesResponse(id = catalog.id.value,
                    name = catalog.name,
                    status = catalog.status.value,
                    createdBy = CreatedByUser(user.name),
                    items = catalog.imageAssets.map { image ->
                        ImageResponse(id = image.id.value, url = image.url)
                    })
        }
    }
}