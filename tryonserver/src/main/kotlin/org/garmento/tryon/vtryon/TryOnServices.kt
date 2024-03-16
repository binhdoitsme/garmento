package org.garmento.tryon.vtryon

import org.garmento.tryon.users.UserId
import org.garmento.tryon.users.UserServices
import java.io.InputStream

class SessionNotFound(val id: String) : RuntimeException("Try-On session not found: $id")

data class TryOnJobDTO(
    val id: String,
    val sessionId: String,
    val referenceImageURL: String,
    val garmentImageURL: String,
    val resultURL: String? = null,
)

data class TryOnSessionDTO(val id: String, val tryOnJobs: List<TryOnJobDTO>? = null) {
    companion object {
        fun fromDomain(session: TryOnSession, tryOnJobs: List<TryOnJobDTO>? = null) =
            TryOnSessionDTO(session.id.value, tryOnJobs)
    }
}

fun toDTO(session: TryOnSession, tryOnStore: TryOnStore): TryOnSessionDTO {
    val tryOnJobs = session.tryOnJobs.values.map {
        val referenceImageURL = tryOnStore.getPublicURL(it.referenceImage)
        val garmentImageURL = tryOnStore.getPublicURL(it.garmentImage)
        val resultURL = if (it.isSucceeded()) tryOnStore.getPublicURL(it.result!!) else null
        TryOnJobDTO(
            it.id.value,
            session.id.value,
            referenceImageURL.toString(),
            garmentImageURL.toString(),
            resultURL?.toString(),
        )
    }
    return TryOnSessionDTO(session.id.value, tryOnJobs)
}

class TryOnServices(
    private val repository: TryOnRepository,
    private val userServices: UserServices,
    private val tryOnStore: TryOnStore,
) {
    fun createSession(userId: UserId): TryOnSessionDTO {
        val user = userServices.findUser(userId)
        val session = TryOnSession(user.id).let { repository.save(it) }
        return TryOnSessionDTO.fromDomain(session)
    }

    private fun findSessionBy(userId: UserId, sessionId: TryOnSessionId): TryOnSession {
        val user = userServices.findUser(userId)
        return repository.findByUserAndId(user.id, sessionId) ?: throw SessionNotFound(sessionId.value)
    }

    private fun findSessionBy(sessionId: TryOnSessionId): TryOnSession {
        return repository.findById(sessionId) ?: throw SessionNotFound(sessionId.value)
    }


    fun getSession(userId: UserId, sessionId: TryOnSessionId): TryOnSessionDTO {
        val session = findSessionBy(userId, sessionId)
        return toDTO(session, tryOnStore)
    }

    fun createNewTryOnJob(
        userId: UserId, sessionId: TryOnSessionId, referenceImage: InputStream, garmentImage: InputStream
    ) = run {
        val session = findSessionBy(userId, sessionId)
        val referenceImageId = tryOnStore.save(referenceImage)
        val garmentImageId = tryOnStore.save(garmentImage)
        session.createTryOn(
            referenceImage = referenceImageId, garmentImage = garmentImageId
        ).also { repository.save(session) }.let { session.tryOnJobs[it]!! }.let {
            TryOnJobDTO(
                id = it.id.value,
                sessionId = session.id.value,
                referenceImageURL = tryOnStore.getPublicURL(it.referenceImage).toString(),
                garmentImageURL = tryOnStore.getPublicURL(it.garmentImage).toString()
            )
        }
    }

    fun processingTryOnJob(userId: UserId, sessionId: TryOnSessionId, jobId: TryOnId) {
        val session = findSessionBy(userId, sessionId)
        session.process(jobId)
        repository.save(session)
    }

    fun markTryOnJobSuccess(
        sessionId: TryOnSessionId, jobId: TryOnId, resultImage: InputStream
    ): TryOnSessionDTO {
        val session = findSessionBy(sessionId)
        val resultImageId = tryOnStore.save(resultImage)
        session.completedWithResult(jobId, resultImageId)
        return toDTO(repository.save(session), tryOnStore)
    }

    fun markTryOnJobFailed(sessionId: TryOnSessionId, jobId: TryOnId): TryOnSessionDTO {
        val session = findSessionBy(sessionId)
        session.failed(jobId)
        return toDTO(repository.save(session), tryOnStore)
    }
}
