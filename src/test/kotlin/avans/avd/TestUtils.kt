package avans.avd

import avans.avd.auth.JwtService
import avans.avd.auth.LoginRequest
import avans.avd.users.FakeUserRepository
import avans.avd.users.Role
import avans.avd.users.UserService
import io.ktor.client.request.*

suspend fun HttpRequestBuilder.authenticate(role: Role) {
    val userService = UserService(FakeUserRepository)
    val jwtService = JwtService(
        "my secret",
        "http://localhost",
        "my-audience",
        "my realm",
        userService
    )
    val user = userService.findAll().find { it.role == role }
        ?: throw AssertionError("No user in repository for role: ${role.name}")
    val token = jwtService.authenticate(LoginRequest(user.username, user.password))
        ?: throw AssertionError("Failed to authenticate: ${user.username}")
    header("Authorization", "Bearer $token")
}