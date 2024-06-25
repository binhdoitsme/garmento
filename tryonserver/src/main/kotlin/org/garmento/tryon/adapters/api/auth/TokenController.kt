package org.garmento.tryon.adapters.api.auth

import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.garmento.tryon.services.auth.AuthRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/tokens")
class TokenController @Autowired constructor(
    private val tokenHandler: TokenHandler,
    private val repository: AuthRepository,
    private val httpSession: HttpSession,
) {
    companion object {
        data class TokenRequest(
            val token: String,
        )
    }

    @PostMapping
    fun exchangeToken(
        @RequestBody token: TokenRequest,
        response: HttpServletResponse,
    ): ResponseEntity<Void> = runCatching<TokenController, ResponseEntity<Void>> {
        val tokenInfo = tokenHandler.getSSOTokenInfo(token.token)
        if (tokenInfo?.verifiedEmail != true) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
        val email = tokenInfo.email
        val userInfo = repository.findByEmail(email) ?: return ResponseEntity.status(
            HttpStatus.BAD_REQUEST
        ).build()
        val clientToken = tokenHandler.createToken(userInfo)
        // Set JWT token in an HTTP-only cookie
        val cookie = ResponseCookie.from("accessToken", clientToken).httpOnly(true)
//                .secure(true) // Set to true if using HTTPS
            .maxAge(3600) // Set cookie expiration time (in seconds) as per your requirement
            .path("/") // Set cookie path as per your requirement
            .build()
        val headers = HttpHeaders()
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString())
        ResponseEntity.noContent().headers(headers).build()
    }.getOrElse {
        ResponseEntity.badRequest().build()
    }

    @DeleteMapping
    fun revokeToken(@RequestBody token: TokenRequest, response: HttpServletResponse) =
        httpSession.invalidate().let { ResponseEntity.noContent().build<Void>() }
}
