package org.garmento.tryon.services.users

class UserServices(private val repository: UserRepository) {
    fun findUser(userId: UserId): User =
        repository.findById(userId) ?: throw UserNotFound(userId.value)

    fun findUserByEmail(email: String): User =
        repository.findByEmail(email) ?: throw UserNotFound(email)
}