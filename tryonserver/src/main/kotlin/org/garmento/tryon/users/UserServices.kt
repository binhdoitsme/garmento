package org.garmento.tryon.users

class UserNotFound(userId: String) : Exception("User not found: $userId")

data class UserId(val value: String)

data class User(val id: UserId)

interface UserRepository {
    fun findById(id: UserId): User?
}

class UserServices(private val repository: UserRepository) {
    fun findUser(userId: String): User = repository.findById(UserId(userId)).let {
        it ?: throw UserNotFound(userId)
    }
}