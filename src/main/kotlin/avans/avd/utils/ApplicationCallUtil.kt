package avans.avd.utils

import avans.avd.auth.UserPrincipal
import avans.avd.exceptions.MissingRoleException
import avans.avd.users.Role
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.util.pipeline.*

fun ApplicationCall.userName(): String? = user?.username
fun ApplicationCall.userId(): Long? = user?.id
fun ApplicationCall.userRole(): Role? = user?.role

fun PipelineContext<Unit, ApplicationCall>.isQualifiedOfficial() =
    call.userRole() in setOf(Role.ADMIN, Role.OFFICIAL)

fun PipelineContext<Unit, ApplicationCall>.assertIsQualified() =
    assertHasRole(Role.ADMIN, Role.OFFICIAL)

fun PipelineContext<Unit, ApplicationCall>.assertHasRole(vararg roles: Role) {
    if (call.userRole() !in roles)
        throw MissingRoleException(roles.toList())
}

private val ApplicationCall.user get() =
    principal<UserPrincipal>()?.user