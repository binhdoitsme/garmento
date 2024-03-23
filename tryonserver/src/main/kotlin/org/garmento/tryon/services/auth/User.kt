package org.garmento.tryon.services.auth

data class User(
    val id: String,
    val email: String,
    val name: String,
    val role: Role,
)
