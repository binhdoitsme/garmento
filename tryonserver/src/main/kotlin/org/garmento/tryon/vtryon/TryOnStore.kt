package org.garmento.tryon.vtryon

import java.io.InputStream
import java.net.URL

interface TryOnStore {
    fun save(content: InputStream, id: ImageId = ImageId()): ImageId
    fun getPublicURL(imageId: ImageId): URL
}