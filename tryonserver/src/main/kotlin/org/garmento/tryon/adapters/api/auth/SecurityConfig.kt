package org.garmento.tryon.adapters.api.auth

import org.garmento.tryon.services.auth.AuthRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun filterChain(
        http: HttpSecurity,
        tokenHandler: TokenHandler,
        authRepository: AuthRepository,
    ): SecurityFilterChain = http {
        csrf { disable() }
        authorizeHttpRequests {
            authorize("/actuator/**", permitAll)
            authorize("/tokens/**", permitAll)
            authorize("/service-tokens/**", permitAll)
            authorize("/error", permitAll)
            authorize(anyRequest, authenticated)
        }
        sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
        addFilterBefore<UsernamePasswordAuthenticationFilter>(
            JwtAuthenticationFilter(tokenHandler, authRepository)
        )
    }.let { http.build() }
}