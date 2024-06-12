package org.garmento.tryon.services.tryon

import java.net.URI

/**
 * Service contract of Preprocessor service.
 */
interface Preprocessor {
    companion object {
        data class Result(
            val id: String,
            val refImage: String?,
            val garmentImage: String?,
            val maskedGarmentImage: String?,
            val denseposeImage: String?,
            val segmentedImage: String?,
            val poseKeypoints: String?,
        )
    }

    suspend fun preprocess(referenceImageURL: URI, garmentImageURL: URI): Result
}