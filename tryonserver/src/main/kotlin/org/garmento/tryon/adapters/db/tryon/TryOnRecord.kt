package org.garmento.tryon.adapters.db.tryon

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.garmento.tryon.services.tryon.TryOnJob
import org.garmento.tryon.services.tryon.TryOnJobId
import org.garmento.tryon.services.users.UserId
import java.net.URI


@Entity(name = "try_on_jobs")
data class TryOnRecord(
    @Id val id: String,
    @Column(name = "status") val status: String,
    @Column(name = "reference_image_url") val referenceImageURL: String,
    @Column(name = "garment_image_url") val garmentImageURL: String,
    @Column(name = "result_image_url", nullable = true) val resultImageURL: String?,
    @Column(name = "user_id", nullable = true) val userId: String?,
) {
    companion object {
        fun fromDomain(job: TryOnJob) = TryOnRecord(
            id = job.id.value,
            status = job.status.name,
            referenceImageURL = job.referenceImageURL.toString(),
            garmentImageURL = job.garmentImageURL.toString(),
            resultImageURL = job.resultImageURL?.toString(),
            userId = job.userId?.value,
        )
    }

    fun toDomain() = TryOnJob(
        id = TryOnJobId(id),
        status = TryOnJob.Companion.Status.valueOf(status),
        garmentImageURL = URI.create(garmentImageURL),
        referenceImageURL = URI.create(referenceImageURL),
        resultImageURL = resultImageURL?.let(URI::create),
        userId = userId?.let(::UserId),
    )
}
