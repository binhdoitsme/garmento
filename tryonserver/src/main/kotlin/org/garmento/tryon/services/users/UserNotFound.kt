package org.garmento.tryon.services.users

class UserNotFound(userId: String) : Exception("User not found: $userId")
