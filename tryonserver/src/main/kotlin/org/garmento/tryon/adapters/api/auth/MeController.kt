package org.garmento.tryon.adapters.api.auth

import org.garmento.tryon.services.users.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/me")
class MeController @Autowired constructor(
    private val tokenHandler: TokenHandler, private val userRepository: UserRepository
) {
    @PostMapping
    fun getMyInformation(@CookieValue("accessToken") token: String) =
        tokenHandler.parseToken(token).payload.subject.let { email ->
            when (val user = userRepository.findByEmail(email)) {
                null -> ResponseEntity.notFound().build()
                else -> ResponseEntity.ok(mapOf("email" to user.email, "name" to user.name))
            }
        }
}