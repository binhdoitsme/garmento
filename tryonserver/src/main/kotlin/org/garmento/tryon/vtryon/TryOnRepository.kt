package org.garmento.tryon.vtryon

import org.garmento.tryon.users.UserId

interface TryOnRepository {
    fun save(session: TryOnSession): TryOnSession
    fun findByUserAndId(userId: UserId, id: TryOnSessionId): TryOnSession?
    fun findByUser(userId: UserId): Iterable<TryOnSession>
}