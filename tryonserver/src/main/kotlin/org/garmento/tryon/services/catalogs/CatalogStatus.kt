package org.garmento.tryon.services.catalogs

sealed class CatalogStatus(val value: String) {
    data object Draft : CatalogStatus("DRAFT")
    data object Submitted : CatalogStatus("SUBMITTED")
    data object Approved : CatalogStatus("APPROVED")
    data object Published : CatalogStatus("PUBLISHED")

    companion object {
        fun fromString(value: String) = when (value) {
            Draft.value -> Draft
            Submitted.value -> Submitted
            Approved.value -> Approved
            Published.value -> Published
            else -> throw IllegalArgumentException()
        }
    }
}
