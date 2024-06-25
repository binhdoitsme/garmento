package org.garmento.tryon.adapters

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.garmento.tryon.services.assets.ImageRepository
import org.garmento.tryon.services.catalogs.CatalogRepository
import org.garmento.tryon.services.catalogs.CatalogServices
import org.garmento.tryon.services.tryon.*
import org.garmento.tryon.services.users.UserRepository
import org.garmento.tryon.services.users.UserServices
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.reactive.function.client.WebClient


@Component
class Dependencies {
    @Bean
    fun createUserServices(repository: UserRepository) = UserServices(repository)

    @Bean
    fun createCatalogServices(repository: CatalogRepository) = CatalogServices(repository)

    @Bean
    fun createTryOnServices(
        modelRegistry: ModelRegistry,
        preprocessor: Preprocessor,
        imageRepository: ImageRepository,
        jobRepository: TryOnJobRepository,
        scheduler: TryOnScheduler,
        presetRepository: PresetRepository,
    ): TryOnServices =
        TryOnServices(imageRepository, jobRepository, scheduler, presetRepository)

    @Bean
    fun createRestClient() = RestClient.builder().build()

    @Bean
    fun createWebClient() = WebClient.builder().build()

    @Bean
    fun createTransport() = GoogleNetHttpTransport.newTrustedTransport()!!

    @Bean
    fun createJsonFactory() = GsonFactory()
}