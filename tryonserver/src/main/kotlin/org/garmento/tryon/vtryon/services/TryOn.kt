package org.garmento.tryon.vtryon.services

import java.time.Instant
import java.util.UUID

enum class TryOnStatus {
    PENDING,
    IN_PROGRESS,
    FAILED,
    SUCCEEDED
}

data class TryOnId(val value: UUID = UUID.randomUUID())
data class ImageId(val value: UUID = UUID.randomUUID())

data class TryOn(
        val referenceImage: ImageId,
        val garmentImage: ImageId,
        val result: ImageId? = null,
        val id: TryOnId = TryOnId(),
        val status: TryOnStatus = TryOnStatus.PENDING,
        val creationDateTime: Instant = Instant.now(),
) {
    private fun isFailed() = this.status == TryOnStatus.FAILED
    private fun isSucceeded() = this.status == TryOnStatus.SUCCEEDED
    fun isCompleted() = this.isSucceeded() || this.isFailed()

    fun process() = this.copy(status = TryOnStatus.IN_PROGRESS)
    fun failed() = this.copy(status = TryOnStatus.FAILED)
    fun succeeded(result: ImageId) = this.copy(
            status = TryOnStatus.SUCCEEDED, result = result
    )
}