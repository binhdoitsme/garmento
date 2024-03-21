package org.garmento.tryon.adapters.api.auth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain


@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun filterChain(
        http: HttpSecurity, authService: AuthService
    ): SecurityFilterChain = http {
        csrf { disable() }
        authorizeHttpRequests {
            authorize("/actuator/**", permitAll)
            authorize("/tokens/**", permitAll)
            authorize(anyRequest, authenticated)
        }
        sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
//        addFilterBefore<UsernamePasswordAuthenticationFilter>(
//            TryOnAuthenticationFilter(authService)
//        )
    }.let { http.build() }
}