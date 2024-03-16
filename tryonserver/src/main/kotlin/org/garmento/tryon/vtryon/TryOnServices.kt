package org.garmento.tryon.vtryon

import org.garmento.tryon.users.UserServices
import java.io.InputStream

class SessionNotFound(id: String) : RuntimeException("Try-On session not found: $id")

data class TryOnJobDTO(
    val id: String,
    val referenceImageURL: String,
    val garmentImageURL: String,
    val resultURL: String?,
)

data class TryOnSessionDTO(val id: String, val tryOnJobs: List<TryOnJobDTO>? = null) {
    companion object {
        fun fromDomain(session: TryOnSession, tryOnJobs: List<TryOnJobDTO>? = null) =
            TryOnSessionDTO(session.id.value, tryOnJobs)
    }
}

class TryOnServices(
    private val repository: TryOnRepository,
    private val resultStore: TryOnStore,
    private val userServices: UserServices,
    private val imageRepository: ImageRepository,
) {
    fun createSession(userId: String): TryOnSessionDTO {
        val user = userServices.findUser(userId)
        val session = TryOnSession(user.id).let { repository.save(it) }
        return TryOnSessionDTO.fromDomain(session)
    }

    private fun findSessionBy(userId: String, sessionId: String): TryOnSession {
        val user = userServices.findUser(userId)
        return repository.findByUserAndId(user.id, TryOnSessionId(sessionId)) ?: throw SessionNotFound(sessionId)
    }

    private fun findSessionBy(sessionId: String): TryOnSession {
        return repository.findById(TryOnSessionId(sessionId)) ?: throw SessionNotFound(sessionId)
    }

    private fun toDTO(session: TryOnSession): TryOnSessionDTO {
        val tryOnJobs = session.tryOnJobs.values.map {
            val referenceImageURL = resultStore.getPublicURL(it.referenceImage)
            val garmentImageURL = resultStore.getPublicURL(it.garmentImage)
            val resultURL = if (it.isSucceeded()) resultStore.getPublicURL(it.result!!) else null
            TryOnJobDTO(
                it.id.value,
                referenceImageURL.toString(),
                garmentImageURL.toString(),
                resultURL?.toString(),
            )
        }
        return TryOnSessionDTO(session.id.value, tryOnJobs)
    }


    fun getSession(userId: String, sessionId: String): TryOnSessionDTO {
        val session = findSessionBy(userId, sessionId)
        return toDTO(session)
    }

    fun createNewTryOnJob(
        userId: String, sessionId: String, referenceImage: InputStream, garmentImage: InputStream
    ): TryOnSessionDTO {
        val session = findSessionBy(userId, sessionId)
        val referenceImageId = imageRepository.save(referenceImage)
        val garmentImageId = imageRepository.save(garmentImage)
        session.createTryOn(
            referenceImage = referenceImageId, garmentImage = garmentImageId
        )
        repository.save(session)
        return toDTO(session)
    }

    fun processingTryOnJob(userId: String, sessionId: String, jobId: String) {
        val session = findSessionBy(userId, sessionId)
        session.process(TryOnId(jobId))
    }

    fun markTryOnJobSuccess(
        sessionId: String, jobId: String, resultImage: InputStream
    ): TryOnSessionDTO {
        val session = findSessionBy(sessionId)
        val resultImageId = imageRepository.save(resultImage)
        session.completedWithResult(TryOnId(jobId), resultImageId)
        return toDTO(repository.save(session))
    }

    fun markTryOnJobFailed(sessionId: String, jobId: String): TryOnSessionDTO {
        val session = findSessionBy(sessionId)
        session.failed(TryOnId(jobId))
        return toDTO(repository.save(session))
    }
}
