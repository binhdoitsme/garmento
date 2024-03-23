package org.garmento.tryon.adapters.api.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.garmento.tryon.auth.AuthRepository
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val tokenHandler: TokenHandler,
    private val authRepository: AuthRepository
) : OncePerRequestFilter() {
    companion object {
        const val COOKIE_NAME = "accessToken"
        val PUBLIC_ROUTES = listOf("/tokens", "/actuator")
    }

    override fun shouldNotFilter(request: HttpServletRequest) =
        PUBLIC_ROUTES.any { request.servletPath.startsWith(it) }


    override fun doFilterInternal(
        request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain
    ) = runCatching {
        val token = extractJwtToken(request)
        val parsed = tokenHandler.parseToken(token)
        val claims = parsed.payload
        val email = claims.subject
        val user = authRepository.findByEmail(email) ?: throw IllegalArgumentException()
        val authorities = listOf(SimpleGrantedAuthority(user.role.name))
        val authentication = UsernamePasswordAuthenticationToken(email, null, authorities)
        SecurityContextHolder.getContext().authentication = authentication
        filterChain.doFilter(request, response)
    }.getOrElse {
        // Token is invalid or expired, clear security context
        SecurityContextHolder.clearContext()
        response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid or expired JWT token")
    }

    private fun extractJwtToken(request: HttpServletRequest): String =
        request.cookies?.find { it.name == COOKIE_NAME }?.value
            ?: throw IllegalArgumentException("Missing token in cookies")

}