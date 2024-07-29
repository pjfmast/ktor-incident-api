package avans.avd.util

import avans.avd.models.Role
import avans.avd.plugins.RoleBasedAuthorizationPlugin
import io.ktor.server.routing.*

fun Route.authorized(
    vararg hasAnyRole: Role,
    build: Route.() -> Unit
) {
    install(RoleBasedAuthorizationPlugin) { roles = hasAnyRole.toSet() }
    build()
}