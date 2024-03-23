package org.garmento.tryon.adapters

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import jakarta.jms.ConnectionFactory
import org.garmento.tryon.services.catalogs.CatalogRepository
import org.garmento.tryon.services.catalogs.CatalogServices
import org.garmento.tryon.inference.InferenceServices
import org.garmento.tryon.inference.ModelRegistry
import org.garmento.tryon.services.users.UserRepository
import org.garmento.tryon.services.users.UserServices
import org.garmento.tryon.vtryon.TryOnRepository
import org.garmento.tryon.vtryon.TryOnServices
import org.garmento.tryon.vtryon.TryOnStore
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.jms.config.DefaultJmsListenerContainerFactory
import org.springframework.jms.support.converter.MappingJackson2MessageConverter
import org.springframework.jms.support.converter.MessageType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient


@Component
class Dependencies {
    @Bean
    fun createTryOnServices(
        repository: TryOnRepository, userServices: UserServices, tryOnStore: TryOnStore
    ) = TryOnServices(repository, userServices, tryOnStore)

    @Bean
    fun createUserServices(repository: UserRepository) = UserServices(repository)

    @Bean
    fun createCatalogServices(repository: CatalogRepository) = CatalogServices(repository)

    @Bean
    fun createJmsListenerContainerFactory(
        connectionFactory: ConnectionFactory, configurer: DefaultJmsListenerContainerFactoryConfigurer
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
    fun createInferenceService(
        modelRegistry: ModelRegistry, tryOnRepository: TryOnRepository, tryOnStore: TryOnStore
    ) = InferenceServices(modelRegistry, tryOnRepository, tryOnStore)

    @Bean
    fun createTransport() = GoogleNetHttpTransport.newTrustedTransport()!!

    @Bean
    fun createJsonFactory() = GsonFactory()
}