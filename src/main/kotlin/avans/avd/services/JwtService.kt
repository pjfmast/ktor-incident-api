package avans.avd.services

import avans.avd.dto.LoginRequest
import avans.avd.models.User
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import java.util.*

class JwtService(
    private val application: Application,
    private val userService: UserService
) {
    private val jwtSecret = getConfigProperty("jwt.secret")
    private val jwtIssuer = getConfigProperty("jwt.issuer")
    private val jwtAudience = getConfigProperty("jwt.audience")

    val jwtRealm = getConfigProperty("jwt.realm")

    val jwtVerifier: JWTVerifier =
        JWT
            .require(Algorithm.HMAC256(jwtSecret))
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .build()

    suspend fun authenticate(loginRequest: LoginRequest): String? {
        val foundUser = userService.findByUsername(loginRequest.username)

        return if (foundUser != null && foundUser.password == loginRequest.password) {
            createAccesToken(foundUser) // when check in jwt.io this signature can be entered in 'verify signature'
        } else null
    }

    private fun createAccesToken(foundUser: User): String = JWT
        .create()
        .withAudience(jwtAudience)
        .withIssuer(jwtIssuer)
        .withClaim("id", foundUser.id)
        // note when getting a claim of type String use claim.asString() instead of claim.toString()
        .withClaim("username", foundUser.username)
        .withClaim("role", foundUser.role.toString())
        .withExpiresAt(Date(System.currentTimeMillis() + 3_600_000)) // with this the JWT is also unique after each new request
        .sign(Algorithm.HMAC256(jwtSecret))

    // A credential is a set of properties for a server to authenticate a principal (here a JWTCredential)
    // A principal is an entity that can be authenticated (here a JWTPrincipal)
    fun customValidator(credential: JWTCredential): JWTPrincipal? {
        val username = extractUsername(credential)
        val foundUser = username?.let { userService::findByUsername }

        return foundUser?.let { user ->
            if (audienceMatches(credential)) {
                JWTPrincipal(credential.payload)
            } else null
        }
    }

//    fun isAdminValidator(credential: JWTCredential): JWTPrincipal? {
//        val username = extractUsername(credential)
//        val foundUser = username?.let { userService::findByUsername }
//        val role = extractRole(credential)
//
//        return foundUser?.let { user ->
//            if (audienceMatches(credential)) {
//                JWTPrincipal(credential.payload)
//            } else null
//        }
//    }

    private fun audienceMatches(credential: JWTCredential): Boolean =
        credential.payload.audience.contains(jwtAudience)

    private fun extractUsername(credential: JWTCredential): String? =
        credential.payload.getClaim("username").asString()

    private fun extractRole(credential: JWTCredential): String? =
        credential.payload.getClaim("role").asString()

    private fun getConfigProperty(path: String) = application.environment.config.property(path).getString()
}