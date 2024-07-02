package org.garmento.tryon.adapters.api.auth

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.services.oauth2.Oauth2
import com.google.api.services.oauth2.model.Tokeninfo
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.garmento.tryon.services.auth.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey


@Component
class TokenHandler(
    @Value("\${jwt.secret}") private val secret: String,
    private val transport: HttpTransport,
    private val jsonFactory: JsonFactory,
) {
    companion object {
        const val SERVICE_TOKEN_PREFIX = "svc.0."
    }

    private val key: SecretKey
        get() {
            val bytes = Decoders.BASE64.decode(secret)
            return Keys.hmacShaKeyFor(bytes)
        }

    fun createToken(
        userInfo: User,
        currentTimeMillis: Long = System.currentTimeMillis(),
        expirationMillis: Long = 1000 * 60 * 60 * 24, // 24 hours
    ): String = Jwts.builder().subject(userInfo.email).claim("role", userInfo.role)
        .claim("name", userInfo.name).issuedAt(Date(currentTimeMillis))
        .expiration(Date(currentTimeMillis + expirationMillis)).signWith(key).compact()

    fun createServiceToken(
        currentTimeMillis: Long = System.currentTimeMillis(),
        expirationMillis: Long = 1000 * 60 * 60 * 24, // 24 hours
    ): String = Jwts.builder().subject("bot token").claim("role", "serviceToken")
        .claim("name", "serviceToken").issuedAt(Date(currentTimeMillis))
        .expiration(Date(currentTimeMillis + expirationMillis)).signWith(key).compact()
        .let { "$SERVICE_TOKEN_PREFIX$it" }

    fun parseToken(token: String): Jws<Claims> =
        if (token.startsWith(SERVICE_TOKEN_PREFIX)) {
            parseToken(token.removePrefix(SERVICE_TOKEN_PREFIX))
        } else {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token).also {
                println("token: $it")
            }
        }

    fun getSSOTokenInfo(token: String): Tokeninfo? = run {
        val requestInitializer: HttpRequestInitializer =
            GoogleCredential.Builder().build().setAccessToken(token)
        val oauth2 = Oauth2.Builder(transport, jsonFactory, requestInitializer)
            .setApplicationName("Garmento App").build()
        oauth2.tokeninfo().setAccessToken(token).execute()
    }
}

