package org.garmento.tryon.vtryon

import java.net.URL

interface TryOnStore {
    fun getPublicURL(imageId: ImageId): URL
}