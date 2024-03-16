package org.garmento.tryon.vtryon.services

interface TryOnRepository {
    fun save(session: TryOnSession)
    fun find(id: TryOnSessionId): TryOnSession?
    fun findByUser(userId: UserId): Iterable<TryOnSession>
}