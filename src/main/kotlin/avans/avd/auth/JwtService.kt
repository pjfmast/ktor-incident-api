package avans.avd.auth

import avans.avd.users.User
import avans.avd.users.UserService
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import java.util.*

class JwtService(
    private val jwtSecret: String,
    private val jwtIssuer: String,
    private val jwtAudience: String,
    val jwtRealm: String,
    private val userService: UserService
) {
    constructor(application: Application, userService: UserService): this(
        jwtSecret = application.getConfigProperty("jwt.secret"),
        jwtIssuer = application.getConfigProperty("jwt.issuer"),
        jwtAudience = application.getConfigProperty("jwt.audience"),
        jwtRealm = application.getConfigProperty("jwt.realm"),
        userService = userService
    )

    val jwtVerifier: JWTVerifier =
        JWT
            .require(Algorithm.HMAC256(jwtSecret))
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .build()

    suspend fun authenticate(loginRequest: LoginRequest): String? {
        val foundUser = userService.findByUsername(loginRequest.username)

        return if (foundUser != null && foundUser.password == loginRequest.password) {
            createAccessToken(foundUser) // when check in jwt.io the signature can be entered in 'verify signature'
        } else null
    }

    private fun createAccessToken(foundUser: User): String = JWT
        .create()
        .withAudience(jwtAudience)
        .withIssuer(jwtIssuer)
        .withClaim("id", foundUser.id)
        .withClaim("role", foundUser.role.toString())
        .withExpiresAt(Date(System.currentTimeMillis() + 3_600_000)) // with this the JWT is also unique after each new request
        .sign(Algorithm.HMAC256(jwtSecret))

    // A credential is a set of properties for a server to authenticate a principal (here a JWTCredential)
    // A principal is an entity that can be authenticated (here a JWTPrincipal)
    suspend fun customValidator(credential: JWTCredential): UserPrincipal? {
        val userId = credential.payload.getClaim("id").asLong() ?: return null
        val foundUser = userService.findById(userId)

        return foundUser?.let { user ->
            if (audienceMatches(credential)) {
                UserPrincipal(user)
            } else null
        }
    }


    private fun audienceMatches(credential: JWTCredential): Boolean =
        credential.payload.audience.contains(jwtAudience)

    // note when getting a claim of type String, use claim.asString() instead of claim.toString()
    private fun extractUsername(credential: JWTCredential): String? =
        credential.payload.getClaim("username").asString()

    private fun extractRole(credential: JWTCredential): String? =
        credential.payload.getClaim("role").asString()

}

private fun Application.getConfigProperty(path: String) = environment.config.property(path).getString()
