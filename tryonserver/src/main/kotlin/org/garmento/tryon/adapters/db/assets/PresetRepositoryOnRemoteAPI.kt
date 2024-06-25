package org.garmento.tryon.adapters.db.assets

import org.garmento.tryon.services.tryon.PresetRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class PresetRepositoryOnRemoteAPI @Autowired constructor(
    private val httpClient: WebClient,
    @Value("\${remote.preprocessingServiceId}") private val serviceId: String,
) : PresetRepository {
    private val baseURL: String = "http://$serviceId"

    companion object {
        private const val PUBLIC_BASE_URL = "/api/preprocessor"
    }

    override fun getPreset(name: String): PresetRepository.Companion.PresetMetadata? =
        runCatching {
            "$baseURL/presets/$name".let { url ->
                httpClient.get().uri(url).retrieve()
                    .bodyToMono(PresetRepository.Companion.PresetMetadata::class.java)
                    .block()
            }
        }.getOrNull()
}