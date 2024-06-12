package org.garmento.tryon.adapters.messaging

import kotlinx.coroutines.runBlocking
import org.garmento.tryon.services.tryon.*
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Component
class TryOnJobSchedulerOnJms @Autowired constructor(
    private val messageHandler: RabbitTemplate,
    private val modelRegistry: ModelRegistry,
    private val preprocessor: Preprocessor,
    private val jobRepository: TryOnJobRepository,
) : TryOnScheduler {
    override fun schedule(jobId: TryOnJobId) = messageHandler.convertAndSend(
        Destinations.START_PROCESSING_TRY_ON_JOBS, jobId.value
    )

    @RabbitListener(queues = [Destinations.START_PROCESSING_TRY_ON_JOBS])
    override fun processJob(jobId: String) {
        runBlocking {
            println("Processing job ID $jobId")
            val job =
                jobRepository.findById(TryOnJobId(jobId)) ?: throw NoSuchElementException()
            try {
                job.processing().also(jobRepository::save)
                val preprocessingResult = preprocessor.preprocess(
                    referenceImageURL = job.referenceImageURL,
                    garmentImageURL = job.garmentImageURL,
                )
                println("Done preprocessing for job ID $jobId")
                val resultURL =
                    modelRegistry.inferByLatest(preprocessingResult, jobId).resultURL
                        ?: throw NullPointerException()
                println("Done inference for job ID $jobId")
                job.successWith(resultURL).also(jobRepository::save)
            } catch (e: Exception) {
                e.printStackTrace()
                job.failed().also(jobRepository::save)
            }
        }
    }
}
