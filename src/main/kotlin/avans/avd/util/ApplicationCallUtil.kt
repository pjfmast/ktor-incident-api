package avans.avd.util

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun extractPrincipalName(call: ApplicationCall): String? =
    call.principal<JWTPrincipal>()
        ?.payload
        ?.getClaim("username")
        ?.asString()

fun extractPrincipalId(call: ApplicationCall): Long? =
    call.principal<JWTPrincipal>()
        ?.payload
        ?.getClaim("id")
        ?.asLong()