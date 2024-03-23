package org.garmento.tryon.adapters.db.assets

import org.garmento.tryon.services.assets.Image
import org.garmento.tryon.services.assets.ImageId
import org.garmento.tryon.services.assets.ImageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.io.InputStream
import java.net.URI

@Component
class ImageRepositoryOnRemoteAPI @Autowired constructor(
    private val httpClient: RestClient,
    private val discoveryClient: DiscoveryClient,
    @Value("\${remote.assetServiceId}") private val serviceId: String
) : ImageRepository {
    private val baseURL: String
        get() = discoveryClient.getInstances(serviceId).first().uri.toString()

    companion object {
        data class ImageAssetResponse(val id: String, val url: String)
        class CannotSaveImage(message: String) : RuntimeException(message)
    }

    override fun save(image: InputStream) = httpClient.post().uri("$baseURL/assets")
        .contentType(MediaType.MULTIPART_FORM_DATA).body(image).retrieve()
        .toEntity(ImageAssetResponse::class.java).body?.let {
            Image(
                id = ImageId(it.id), url = URI.create(it.url).toURL()
            )
        } ?: throw CannotSaveImage("Error when exchanging data with remote service")

    override fun findById(id: ImageId): Image? {
        val statusCode = httpClient.get().uri("$baseURL/assets/${id.value}").retrieve()
            .toBodilessEntity().statusCode
        if (statusCode.is4xxClientError) {
            return null
        }
        return Image(id = id, url = URI.create("$baseURL/assets/${id.value}").toURL())
    }

    override fun findAllById(ids: List<ImageId>): Map<ImageId, Image> =
        ids.zip(ids.map(this::findById)).filter { (_, maybeImage) -> maybeImage != null }
            .associate { (id, maybeImage) -> id to maybeImage!! }
}