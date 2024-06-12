package org.garmento.tryon.adapters.messaging

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Destinations {
    companion object {
        const val START_PROCESSING_TRY_ON_JOBS = "startProcessingTryOnJobs"
        private const val EXCHANGE_NAME = "exchangeTryOnJobs"
        private const val ROUTING_KEY = "routingKey"
        const val TEST = "test"
    }

    @Bean
    fun rabbitAdmin(connectionFactory: ConnectionFactory): RabbitAdmin {
        return RabbitAdmin(connectionFactory)
    }

    @Bean
    fun startProcessingTryOnJobs(
        connectionFactory: ConnectionFactory,
    ) = connectionFactory.createConnection().createChannel(false).queueDeclare(
        START_PROCESSING_TRY_ON_JOBS, true, false, false, null
    ).queue.let(::Queue)

    @Bean
    fun myQueue(): Queue {
        return Queue(START_PROCESSING_TRY_ON_JOBS, true)
    }

    @Bean
    fun myExchange(): TopicExchange {
        return TopicExchange(EXCHANGE_NAME)
    }

    @Bean
    fun binding(myQueue: Queue, myExchange: TopicExchange?): Binding {
        return BindingBuilder.bind(myQueue).to(myExchange).with(ROUTING_KEY)
    }
}