package org.garmento.tryon.services.tryon

import org.garmento.tryon.services.assets.ImageRepository
import java.io.InputStream


class TryOnServices(
    private val imageRepository: ImageRepository,
    private val jobRepository: TryOnJobRepository,
    private val scheduler: TryOnScheduler,
    private val presetRepository: PresetRepository,
) {
    fun createJob(referenceImage: InputStream, garmentImage: InputStream): TryOnJob {
        val referenceImageURL = imageRepository.save(referenceImage).url
        val garmentImageURL = imageRepository.save(garmentImage).url
        val job = TryOnJob(
            referenceImageURL = referenceImageURL,
            garmentImageURL = garmentImageURL,
        )
        jobRepository.save(job).also { scheduler.schedule(job.id) }
        return job
    }

    fun createJobForPreset(preset: String, garmentImage: InputStream): TryOnJob {
        val referenceImageURL = presetRepository.getPreset(preset)!!.refImage
        val garmentImageURL = imageRepository.save(garmentImage).url
        val job = TryOnJob(
            referenceImageURL = referenceImageURL,
            garmentImageURL = garmentImageURL,
        )
        jobRepository.save(job).also { scheduler.schedule(job.id) }
        return job
    }

    fun findJobById(id: String) = TryOnJobId(id).let {
        jobRepository.findById(it)
    }
}
