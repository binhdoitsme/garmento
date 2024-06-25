package org.garmento.tryon.services.tryon

import java.net.URI

interface PresetRepository {
    companion object {
        data class PresetMetadata(
            val name: String,
            val refImage: URI,
            val denseposeImage: URI,
            val segmented: URI,
            val poseKeypoints: URI,
        ) {
            fun replaceURIPrefix(
                originalPrefix: String, newPrefix: String,
            ) = this.copy(
                refImage = "$refImage".replace(originalPrefix, newPrefix).let(URI::create),
                denseposeImage = "$denseposeImage".replace(originalPrefix, newPrefix)
                    .let(URI::create),
                segmented = "$segmented".replace(originalPrefix, newPrefix)
                    .let(URI::create),
                poseKeypoints = "$poseKeypoints".replace(originalPrefix, newPrefix)
                    .let(URI::create),
            )
        }
    }

    fun getPreset(name: String): PresetMetadata?
}
