package org.garmento.tryon.adapters.inference

import kotlinx.coroutines.reactor.awaitSingle
import org.garmento.tryon.services.tryon.ModelRegistry
import org.garmento.tryon.services.tryon.Preprocessor
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.net.URI
import java.net.URL
import java.time.Duration


@Component
class ModelRegistryOnRemoteAPI(
    private val httpClient: WebClient,
    @Value("\${remote.inferenceServiceId}") private val serviceId: String,
    @Value("\${remote.preprocessingServiceId}") private val preprocessingServiceId: String,
) : ModelRegistry {
    companion object {
        operator fun <T> List<T>.component6() = this[5]
        private const val PUBLIC_BASE_URL = "/api/model-api"
    }

    private val baseURL: String = "http://$serviceId"
    private val basePreprocessorURL: String = "http://$preprocessingServiceId"

    private suspend fun fetchImage(url: URI) =
        httpClient.get().uri(url).retrieve().bodyToMono(ByteArray::class.java)
            .doOnError {
                throw IllegalArgumentException("Failed to fetch image from URL: $url")
            }.map { InputStreamResource(it.inputStream()) }.awaitSingle()

    private fun handleErrorStatus(kind: String) = { response: ClientResponse ->
        response.bodyToMono(String::class.java).flatMap { errorBody ->
            println("$kind Error: $errorBody")
            Mono.error<Throwable>(
                WebClientResponseException(
                    response.statusCode().value(),
                    response.statusCode().value().toString(),
                    response.headers().asHttpHeaders(),
                    errorBody.toByteArray(),
                    null
                )
            )
        }
    }

    private fun processJobStatusResponse(responseSpec: WebClient.ResponseSpec) =
        responseSpec.onStatus(HttpStatusCode::is4xxClientError, handleErrorStatus("4xx"))
            .onStatus(HttpStatusCode::is5xxServerError, handleErrorStatus("5xx"))
            .bodyToMono(ModelRegistry.Companion.InferenceResult::class.java)

    private fun createJob(parts: MultiValueMap<String, HttpEntity<*>>, jobId: String) =
        httpClient.post().uri("$baseURL/try-on/$jobId")
            .contentType(MediaType.MULTIPART_FORM_DATA).bodyValue(parts).retrieve()
            .let(this::processJobStatusResponse).mapNotNull { it }

    private suspend fun findJob(id: String) =
        httpClient.get().uri("$baseURL/try-on/$id").retrieve()
            .let(this::processJobStatusResponse)

    override suspend fun inferByLatest(
        preprocessingResult: Preprocessor.Companion.Result,
        jobId: String,
    ): ModelRegistry.Companion.InferenceResult = mapOf(
        "ref_image" to fetchImage(
            URI.create("$basePreprocessorURL/${preprocessingResult.refImage!!}")
        ),
        "garment_image" to fetchImage(
            URI.create("$basePreprocessorURL/${preprocessingResult.garmentImage!!}")
        ),
        "densepose_image" to fetchImage(
            URI.create("$basePreprocessorURL/${preprocessingResult.denseposeImage!!}")
        ),
        "masked_garment_image" to fetchImage(
            URI.create("$basePreprocessorURL/${preprocessingResult.maskedGarmentImage!!}")
        ),
        "pose_keypoints" to fetchImage(
            URI.create("$basePreprocessorURL/${preprocessingResult.poseKeypoints!!}")
        ),
        "segmented_image" to fetchImage(
            URI.create("$basePreprocessorURL/${preprocessingResult.segmentedImage!!}")
        ),
    ).entries.fold(MultipartBodyBuilder()) { acc, (fieldName, data) ->
        acc.apply {
            if (fieldName == "pose_keypoints") {
                part(fieldName, data, MediaType.APPLICATION_JSON).header(
                    "Content-Disposition",
                    "form-data; name=$fieldName; filename=$fieldName.json"
                )
            } else {
                part(fieldName, data, MediaType.IMAGE_JPEG).header(
                    "Content-Disposition",
                    "form-data; name=$fieldName; filename=$fieldName.jpg"
                )
            }
        }
    }.build().let { this.createJob(it, jobId) }.awaitSingle().let {
        // add polling logic here
        findJob(jobId).map {
            requireNotNull(it?.resultURL)
            it
        }.retryWhen(Retry.backoff(7, Duration.ofSeconds(3)))
    }.awaitSingle().let { inferenceResult ->
        // transform result URL to host URL instead of internal URL
        "$PUBLIC_BASE_URL${inferenceResult.resultURL.toString()}"
            .let(URI::create)
            .let { inferenceResult.copy(resultURL = it) }
    }
}
