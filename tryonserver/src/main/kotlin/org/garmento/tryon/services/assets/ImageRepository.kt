package org.garmento.tryon.services.assets

import java.io.InputStream

interface ImageRepository {
    fun save(image: InputStream): Image
    fun findById(id: ImageId): Image?
    fun findAllById(ids: List<ImageId>): Map<ImageId, Image>
}