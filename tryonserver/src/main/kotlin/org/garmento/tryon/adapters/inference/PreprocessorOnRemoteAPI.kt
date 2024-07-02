package org.garmento.tryon.adapters.inference

import kotlinx.coroutines.reactor.awaitSingle
import org.garmento.tryon.services.tryon.Preprocessor
import org.springframework.beans.factory.annotation.Autowired
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
import java.time.Duration


@Component
class PreprocessorOnRemoteAPI @Autowired constructor(
    private val httpClient: WebClient,
    @Value("\${remote.preprocessingServiceId}") private val serviceId: String,
    @Value("\${remote.assetServiceId}") private val assetServiceId: String,
) : Preprocessor {
    companion object {
        private const val PUBLIC_BASE_ASSET_URL = "/api/assets/assets/"
    }

    private val baseURL: String = "http://$serviceId"
    private val baseAssetURL: String = "http://$assetServiceId"

    private fun createImageURL(url: URI) = if (url.toString().startsWith("/presets")) {
        URI.create("$baseURL$url")
    } else {
        URI.create(url.toString().replace(PUBLIC_BASE_ASSET_URL, baseAssetURL))
    }

    private suspend fun fetchImage(url: URI) = httpClient.get()
        .uri(createImageURL(url))
        .retrieve().bodyToMono(ByteArray::class.java).doOnError {
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
            .bodyToMono(Preprocessor.Companion.Result::class.java)

    private fun createJob(parts: MultiValueMap<String, HttpEntity<*>>) =
        httpClient.post().uri("$baseURL/jobs").contentType(MediaType.MULTIPART_FORM_DATA)
            .bodyValue(parts).retrieve().let(this::processJobStatusResponse)
            .mapNotNull { it.id }

    private suspend fun findJob(id: String) =
        httpClient.get().uri("$baseURL/jobs/$id").retrieve()
            .let(this::processJobStatusResponse)

    override suspend fun preprocess(
        referenceImageURL: URI,
        garmentImageURL: URI,
    ): Preprocessor.Companion.Result =
        createFormData(referenceImageURL, garmentImageURL).let(this::createJob)
            .awaitSingle().let { jobId ->
                // add polling logic here
                findJob(jobId).map {
                    requireNotNull(it?.refImage)
                    it
                }.retryWhen(Retry.backoff(10, Duration.ofSeconds(3)))
            }.awaitSingle()

    private suspend fun createFormData(
        referenceImageURL: URI,
        garmentImageURL: URI,
    ) = referenceImageURL.toString().let {
        if (it.startsWith("/presets")) {
            // /images/preset/{presetId}/... => split by / and get 2nd
            MultipartBodyBuilder().apply {
                part(
                    "garment_image",
                    fetchImage(garmentImageURL),
                    MediaType.IMAGE_JPEG
                ).header(
                    "Content-Disposition",
                    "form-data; name=garment_image; filename=garment_image.jpg"
                )
                part("preset_id", it.split("/")[2])
            }.build()
        } else {
            mapOf(
                ("ref_image" to fetchImage(referenceImageURL)),
                ("garment_image" to fetchImage(garmentImageURL)),
            ).entries.fold(MultipartBodyBuilder()) { acc, (fieldName, data) ->
                acc.apply {
                    part(fieldName, data, MediaType.IMAGE_JPEG).header(
                        "Content-Disposition",
                        "form-data; name=$fieldName; filename=$fieldName.jpg"
                    )
                }
            }.build()
        }
    }
}
