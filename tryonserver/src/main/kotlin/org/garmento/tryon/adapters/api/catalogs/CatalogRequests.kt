package org.garmento.tryon.adapters.api.catalogs

object CatalogRequests {
    data class CreateCatalogRequest(val name: String)

    data class ImageListRequest(val imageURLs: List<String>)

    data class CreatedByUser(val name: String)

    enum class CatalogAction {
        SUBMIT, APPROVE, UNAPPROVE, PUBLISH,
    }
}