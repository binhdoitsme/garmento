package org.garmento.tryon.services.users

interface UserRepository {
    fun findById(id: UserId): User?
    fun findByEmail(email: String): User?
}