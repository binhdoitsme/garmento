package org.garmento.tryon.adapters.db

import org.garmento.tryon.services.auth.AuthRepository
import org.garmento.tryon.services.auth.Role
import org.garmento.tryon.services.auth.User
import org.springframework.stereotype.Component

@Component
class AuthRepositoryInMemory : AuthRepository {
    override fun findByEmail(email: String): User? {
        if (email == "thiensudianguc2000hp@gmail.com") {
            return User("1", email, "Manager", Role("1", "MANAGER"))
        }
        if (email == "binhdoitsme@gmail.com") {
            return User("2", email, "BinhDH", Role("2", "DESIGNER"))
        }
        return null
    }
}