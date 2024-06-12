package org.garmento.tryon.services.tryon

import org.garmento.tryon.services.users.UserId
import java.net.URI

data class TryOnJob(
    val referenceImageURL: URI,
    val garmentImageURL: URI,
    val id: TryOnJobId = TryOnJobId(),
    val userId: UserId? = null,
    val resultImageURL: URI? = null,
    val status: Status = Status.PENDING,
) {
    companion object {
        enum class Status {
            PENDING, IN_PROGRESS, SUCCESS, FAILED, ABORTED
        }
    }

    fun processing() = copy(status = Status.IN_PROGRESS)
    fun successWith(resultImageURL: URI) = copy(
        resultImageURL = resultImageURL,
        status = Status.SUCCESS,
    )

    fun failed() = copy(status = Status.FAILED)
    fun aborted() = copy(status = Status.ABORTED)
}
