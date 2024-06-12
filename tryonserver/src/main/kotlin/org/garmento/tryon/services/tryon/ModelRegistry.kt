package org.garmento.tryon.services.tryon

import java.net.URI
import java.net.URL

interface ModelRegistry {
    companion object {
        data class InferenceResult(val resultURL: URI? = null)
    }

    suspend fun inferByLatest(
        preprocessingResult: Preprocessor.Companion.Result,
        jobId: String,
    ): InferenceResult
}
