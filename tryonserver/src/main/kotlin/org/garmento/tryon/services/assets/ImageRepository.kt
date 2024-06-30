package org.garmento.tryon.services.assets

import java.io.InputStream
import java.net.URI

interface ImageRepository {
    fun save(image: InputStream): Image
    fun save(id: ImageId, url: String): Image
    fun findById(id: ImageId): Image?
    fun findAllById(ids: List<ImageId>): Map<ImageId, Image>
}