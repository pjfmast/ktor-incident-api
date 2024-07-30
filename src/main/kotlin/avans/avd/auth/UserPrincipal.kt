package avans.avd.auth

import avans.avd.users.User
import io.ktor.server.auth.*

data class UserPrincipal(val user: User): Principal