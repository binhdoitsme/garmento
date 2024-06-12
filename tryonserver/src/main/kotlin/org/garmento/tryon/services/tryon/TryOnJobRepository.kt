package org.garmento.tryon.services.tryon

import org.garmento.tryon.services.users.UserId

interface TryOnJobRepository {
    fun save(job: TryOnJob)
    fun findById(id: TryOnJobId): TryOnJob?
    fun findAllByUserId(userId: UserId): List<TryOnJob>
    fun findByIdAndUserId(id: TryOnJobId, userId: UserId): TryOnJob?
}