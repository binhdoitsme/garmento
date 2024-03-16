package org.garmento.tryon.vtryon

import org.garmento.tryon.utils.throwIfInvalidUuidValue
import org.garmento.tryon.users.UserId
import java.util.UUID

class TryOnJobNotFound(id: TryOnId) : RuntimeException("Try-On job not found in the current session: $id")

class NotCompleted(id: TryOnId) : RuntimeException("Try-On job is not completed: $id")

data class TryOnSessionId(val value: String = UUID.randomUUID().toString()) {
    init {
        throwIfInvalidUuidValue(value, javaClass)
    }
}

data class TryOnSession(
    val userId: UserId,
    val id: TryOnSessionId = TryOnSessionId(),
    val tryOnJobs: MutableMap<TryOnId, TryOn> = mutableMapOf(),
) {

    fun createTryOn(referenceImage: ImageId, garmentImage: ImageId, id: TryOnId = TryOnId()) = run {
        TryOn(referenceImage, garmentImage, id = id).also { tryOnJobs[it.id] = it }
        id
    }

    private fun throwIfNotFound(tryOnId: TryOnId) {
        if (tryOnId !in tryOnJobs) {
            throw TryOnJobNotFound(tryOnId)
        }
    }

    private fun throwIfNotCompleted(tryOnId: TryOnId) {
        if (tryOnJobs[tryOnId]?.isCompleted() == false) {
            throw NotCompleted(tryOnId)
        }
    }

    fun process(tryOnId: TryOnId) = throwIfNotFound(tryOnId).let {
        tryOnJobs[tryOnId] = tryOnJobs[tryOnId]!!.process()
    }

    fun completedWithResult(tryOnId: TryOnId, result: ImageId) = throwIfNotFound(tryOnId).let {
        tryOnJobs[tryOnId] = tryOnJobs[tryOnId]!!.succeeded(result)
    }

    fun failed(tryOnId: TryOnId) = throwIfNotFound(tryOnId).let {
        tryOnJobs[tryOnId] = tryOnJobs[tryOnId]!!.failed()
    }

    fun getResult(tryOnId: TryOnId) = throwIfNotFound(tryOnId).run {
        throwIfNotCompleted(tryOnId)
    }.let { tryOnJobs[tryOnId]!!.result!! }
}
