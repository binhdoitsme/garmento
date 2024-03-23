package org.garmento.tryon.services.auth

interface AuthRepository {
    fun findByEmail(email: String): User?
}