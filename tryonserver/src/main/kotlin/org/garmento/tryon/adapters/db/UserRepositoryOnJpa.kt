package org.garmento.tryon.adapters.db

import org.garmento.tryon.services.users.User
import org.garmento.tryon.services.users.UserId
import org.garmento.tryon.services.users.UserRepository
import org.springframework.stereotype.Component

@Component
class UserRepositoryOnJpa : UserRepository {
    override fun findById(id: UserId): User? = when (id.value) {
        "1" -> User(UserId("1"), "thiensudianguc2000hp@gmail.com", "Manager")
        "2" -> User(UserId("2"), "binhdoitsme@gmail.com", "BinhDH")
        else -> null
    }

    override fun findByEmail(email: String): User? {
        if (email == "thiensudianguc2000hp@gmail.com") {
            return User(UserId("1"), email, "Manager")
        }
        if (email == "binhdoitsme@gmail.com") {
            return User(UserId("2"), email, "BinhDH")
        }
        return null
    }
}