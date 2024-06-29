package org.garmento.tryon.adapters.api.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/service-tokens")
class ServiceTokenController @Autowired constructor(private val tokenHandler: TokenHandler) {
    @PostMapping
    fun authenticateAsService() = tokenHandler.createServiceToken().let { token ->
        ResponseCookie.from("accessToken", token).httpOnly(true)
//                .secure(true) // Set to true if using HTTPS
            .maxAge(3600) // Set cookie expiration time (in seconds) as per your requirement
            .path("/") // Set cookie path as per your requirement
            .build()
    }.let { cookie ->
        ResponseEntity.ok()
            .headers(HttpHeaders().apply { add(HttpHeaders.SET_COOKIE, cookie.toString()) })
            .build<Void>()
    }
}