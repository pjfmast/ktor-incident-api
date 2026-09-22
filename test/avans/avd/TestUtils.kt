package avans.avd

import avans.avd.auth.JwtService
import avans.avd.auth.LoginRequest
import avans.avd.users.FakeUserRepository
import avans.avd.users.Role
import avans.avd.users.UserService
import io.ktor.client.request.*

suspend fun HttpRequestBuilder.authenticate(role: Role) {
    // A separate, identically seeded fake: the token only depends on the demo data and the secret,
    // which testJwtConfig shares with the test application.
    val userService = UserService(FakeUserRepository.withDemoData())
    val jwtService = JwtService(testJwtConfig, userService)
    val user = userService.findAll().find { it.role == role }
        ?: throw AssertionError("No user in repository for role: ${role.name}")
    val token = jwtService.authenticate(LoginRequest(user.username, user.password))
        ?: throw AssertionError("Failed to authenticate: ${user.username}")
    header("Authorization", "Bearer $token")
}