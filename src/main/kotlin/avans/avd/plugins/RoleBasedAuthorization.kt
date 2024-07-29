package avans.avd.plugins

import avans.avd.models.Role
import avans.avd.models.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

class PluginConfiguration {
    var roles: Set<Role> = emptySet()
}

val RoleBasedAuthorizationPlugin = createRouteScopedPlugin(
    name = "RbacPlugin",
    createConfiguration = ::PluginConfiguration // ::MyClass is a constructor reference, here of type () -> PluginConfiguration
) {
    var roles: Set<Role> = pluginConfig.roles
    pluginConfig.apply {

        // also executed for option authentication or for routes without authentication (Principal is null)
        on(AuthenticationChecked) { call ->
            val tokenRole = extractRoleFromToken(call)

            val authorized = roles.contains(tokenRole)
            if (!authorized) {
                println("Only access with one of the roles: $roles")
                call.respond(HttpStatusCode.Forbidden)
            }
        }
    }
}

fun extractRoleFromToken(call: ApplicationCall): Role =
    call.principal<JWTPrincipal>()
        ?.payload
        ?.getClaim("role")
        ?.let { roleClaim ->
            Role.values().firstOrNull { it.name == roleClaim.asString() }
        }
        // if principal is null or an unknown roleClaim then return Role ANONYMOUS
        ?: Role.ANONYMOUS

fun extractUserIdFromToken(call: ApplicationCall): Long? =
    call.principal<JWTPrincipal>()
        ?.payload
        ?.getClaim("id")
        ?.asLong()
