package avans.avd.plugins

import avans.avd.services.JwtService
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureSecurity(
    jwtService: JwtService
) {
    // Please read the jwt property from the config file if you are using EngineMain
    fun getConfigProperty(path: String) = environment.config.property(path).getString()


    authentication {
        jwt {
            realm = jwtService.jwtRealm
            verifier(jwtService.jwtVerifier)

            validate { credential ->
                jwtService.customValidator(credential)
            }
        }

        jwt("access-admin-only") {
            realm = jwtService.jwtRealm
            verifier(jwtService.jwtVerifier)

            validate { credential ->
                jwtService.customValidator(credential)
            }
        }
    }
}