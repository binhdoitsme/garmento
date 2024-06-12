package org.garmento.tryon.adapters.api.tryon

import org.garmento.tryon.services.tryon.TryOnJob

data class TryOnResponse(
    val id: String,
    val status: String,
    val referenceImageURL: String,
    val garmentImageURL: String,
    val resultImageURL: String?,
) {
    companion object {
        fun convert(job: TryOnJob) = TryOnResponse(
            id = job.id.value,
            status = job.status.name,
            referenceImageURL = job.referenceImageURL.toString(),
            garmentImageURL = job.garmentImageURL.toString(),
            resultImageURL = job.resultImageURL?.toString(),
        )
    }
}
