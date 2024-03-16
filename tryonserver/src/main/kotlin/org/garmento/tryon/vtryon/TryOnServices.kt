package org.garmento.tryon.vtryon

import org.garmento.tryon.users.UserServices
import java.util.*

class SessionNotFound(id: String) : RuntimeException("Try-On session not found: $id")

data class TryOnJobDTO(
        val id: String,
        val referenceImageURL: String,
        val garmentImageURL: String,
        val resultURL: String?,
)

data class TryOnSessionDTO(val id: String, val tryOnJobs: List<TryOnJobDTO>? = null) {
    companion object {
        fun fromDomain(session: TryOnSession, tryOnJobs: List<TryOnJobDTO>? = null) = run {
            TryOnSessionDTO(session.id.value.toString(), tryOnJobs)
        }
    }
}

class TryOnServices(
        private val repository: TryOnRepository,
        private val resultStore: TryOnStore,
        private val userServices: UserServices,
) {
    fun createSession(userId: String): TryOnSessionDTO {
        val user = userServices.findUser(userId)
        val session = TryOnSession(user.id).let { repository.save(it) }
        return TryOnSessionDTO.fromDomain(session)
    }

    fun getSession(userId: String, sessionId: String): TryOnSessionDTO {
        val user = userServices.findUser(userId)
        val session = repository.findByUserAndId(user.id, TryOnSessionId(UUID.fromString(sessionId)))
                ?: throw SessionNotFound(sessionId)
        val tryOnJobs = session.tryOnJobs.values.map {
            val referenceImageURL = resultStore.getPublicURL(it.referenceImage)
            val garmentImageURL = resultStore.getPublicURL(it.garmentImage)
            val resultURL = if (it.isCompleted()) resultStore.getPublicURL(it.result!!) else null
            TryOnJobDTO(
                    it.id.value.toString(),
                    referenceImageURL.toString(),
                    garmentImageURL.toString(),
                    resultURL?.toString(),
            )
        }
        return TryOnSessionDTO(session.id.value.toString(), tryOnJobs)
    }
}