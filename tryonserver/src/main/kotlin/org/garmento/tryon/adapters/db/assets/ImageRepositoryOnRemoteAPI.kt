package org.garmento.tryon.adapters.db.assets

import org.garmento.tryon.services.assets.Image
import org.garmento.tryon.services.assets.ImageId
import org.garmento.tryon.services.assets.ImageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.io.InputStream
import java.net.URI

@Component
class ImageRepositoryOnRemoteAPI @Autowired constructor(
    private val httpClient: WebClient,
    @Value("\${remote.assetServiceId}") private val serviceId: String,
) : ImageRepository {
    private val baseURL: String = "http://$serviceId"

    companion object {
        private const val PUBLIC_BASE_URL: String = "/api/assets/assets/"

        data class ImageAssetResponse(val id: String, val url: String)
        class CannotSaveImage(message: String) : RuntimeException(message)
    }

    override fun save(id: ImageId, url: String): Image =
        httpClient.post().uri("$baseURL/assets/upsert")
            .body(Mono.just(mapOf("id" to id.value, "url" to url)), Map::class.java)
            .retrieve()
            .toEntity(ImageAssetResponse::class.java).mapNotNull {
                Image(
                    id = ImageId(it.body!!.id),
                    url = URI.create("${PUBLIC_BASE_URL}${it.body!!.url.removePrefix("/assets/")}")
                )
            }.block()!!

    override fun save(image: InputStream) = image.readAllBytes().runCatching {
        LinkedMultiValueMap<String, Any>().apply {
            add("file", object : ByteArrayResource(this@runCatching) {
                override fun getFilename() = "file.jpg"
            })
        }.let { parts ->
            httpClient.post().uri("$baseURL/assets")
                .contentType(MediaType.MULTIPART_FORM_DATA).bodyValue(parts).retrieve()
                .toEntity(ImageAssetResponse::class.java).mapNotNull {
                    Image(
                        id = ImageId(it.body!!.id),
                        url = URI.create("${PUBLIC_BASE_URL}${it.body!!.url}")
                    )
                }
        }.block()!!
    }.getOrElse {
        throw CannotSaveImage("Error when exchanging data with remote service: ${it.message}")
    }

    override fun findById(id: ImageId): Image? {
        val statusCode = httpClient.get().uri("$baseURL/assets/${id.value}").retrieve()
            .toBodilessEntity().block()!!.statusCode
        if (statusCode.is4xxClientError) {
            return null
        }
        return Image(id = id, url = URI.create("$PUBLIC_BASE_URL${id.value}"))
    }

    override fun findAllById(ids: List<ImageId>): Map<ImageId, Image> =
        ids.zip(ids.map(this::findById)).filter { (_, maybeImage) -> maybeImage != null }
            .associate { (id, maybeImage) -> id to maybeImage!! }
}