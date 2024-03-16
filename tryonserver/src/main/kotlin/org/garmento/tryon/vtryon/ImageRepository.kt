package org.garmento.tryon.vtryon

import java.io.InputStream

interface ImageRepository {
    fun save(content: InputStream, id: ImageId = ImageId()): ImageId
}