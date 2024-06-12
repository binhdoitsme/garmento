package org.garmento.tryon.adapters

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import jakarta.jms.ConnectionFactory
import org.garmento.tryon.services.assets.ImageRepository
import org.garmento.tryon.services.catalogs.CatalogRepository
import org.garmento.tryon.services.catalogs.CatalogServices
import org.garmento.tryon.services.tryon.*
import org.garmento.tryon.services.users.UserRepository
import org.garmento.tryon.services.users.UserServices
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.jms.config.DefaultJmsListenerContainerFactory
import org.springframework.jms.support.converter.MappingJackson2MessageConverter
import org.springframework.jms.support.converter.MessageType
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
    ): TryOnServices = TryOnServices(imageRepository, jobRepository, scheduler)

    @Bean
    fun createJmsListenerContainerFactory(
        connectionFactory: ConnectionFactory,
        configurer: DefaultJmsListenerContainerFactoryConfigurer,
    ) = DefaultJmsListenerContainerFactory().also { factory ->
        // This provides all autoconfigured defaults to this factory, including the message converter
        configurer.configure(factory, connectionFactory)
        // You could still override some settings if necessary.
    }

    @Bean // Serialize message content to json using TextMessage
    fun createJacksonJmsMessageConverter() = MappingJackson2MessageConverter().apply {
        setTargetType(MessageType.TEXT)
        setTypeIdPropertyName("_type")
    }

    @Bean
    fun createRestClient() = RestClient.builder().build()

    @Bean
    fun createWebClient() = WebClient.builder().build()

    @Bean
    fun createTransport() = GoogleNetHttpTransport.newTrustedTransport()!!

    @Bean
    fun createJsonFactory() = GsonFactory()
}