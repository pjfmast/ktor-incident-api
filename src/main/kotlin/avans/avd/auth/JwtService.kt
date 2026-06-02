package avans.avd.auth

import avans.avd.users.User
import avans.avd.users.UserService
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.jwt.*
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String
)

class JwtService(
    private val jwtConfig: JwtConfig,
    private val userService: UserService
) {
    val jwtRealm: String = jwtConfig.realm

    val jwtVerifier: JWTVerifier =
        JWT
            .require(Algorithm.HMAC256(jwtConfig.secret))
            .withAudience(jwtConfig.audience)
            .withIssuer(jwtConfig.issuer)
            .build()

    suspend fun authenticate(loginRequest: LoginRequest): String? {
        val foundUser = userService.findByUsername(loginRequest.username)

        return if (foundUser != null && foundUser.password == loginRequest.password) {
            createAccessToken(foundUser)
        } else null
    }

    private fun createAccessToken(foundUser: User): String = JWT
        .create()
        .withAudience(jwtConfig.audience)
        .withIssuer(jwtConfig.issuer)
        .withClaim("id", foundUser.id)
        .withClaim("role", foundUser.role.toString())
        .withExpiresAt(Date(System.currentTimeMillis() + 3_600_000))
        .sign(Algorithm.HMAC256(jwtConfig.secret))

    // ... existing code ...

    suspend fun customValidator(credential: JWTCredential): UserPrincipal? {
        val id = credential.payload.getClaim("id").asLong()
        return if (audienceMatches(credential) && id != null) {
            userService.findById(id)?.let { UserPrincipal(it) }
        } else null
    }

    private fun audienceMatches(credential: JWTCredential): Boolean =
        credential.payload.audience.contains(jwtConfig.audience)

    // ... existing code ...
}
