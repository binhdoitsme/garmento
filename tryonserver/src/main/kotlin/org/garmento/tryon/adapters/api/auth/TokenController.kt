package org.garmento.tryon.adapters.api.auth

import jakarta.servlet.http.HttpServletResponse
import org.garmento.tryon.auth.AuthRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/tokens")
class TokenController @Autowired constructor(
    private val tokenHandler: TokenHandler, private val repository: AuthRepository
) {
    @PostMapping("/")
    fun exchangeToken(@RequestBody token: String, response: HttpServletResponse): ResponseEntity<Void> = run {
        val tokenInfo = tokenHandler.getSSOTokenInfo(token)
        if (tokenInfo?.verifiedEmail != true) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
        val email = tokenInfo.email
        val userInfo = repository.findByEmail(email) ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        val clientToken = tokenHandler.createToken(userInfo)
        // Set JWT token in an HTTP-only cookie
        val cookie =
            ResponseCookie.from("accessToken", clientToken).httpOnly(true).secure(true) // Set to true if using HTTPS
                .maxAge(3600) // Set cookie expiration time (in seconds) as per your requirement
                .path("/") // Set cookie path as per your requirement
                .build()
        val headers = HttpHeaders()
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString())
        ResponseEntity.noContent().headers(headers).build()
    }
}
