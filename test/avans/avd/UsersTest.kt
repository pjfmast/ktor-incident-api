package avans.avd

import avans.avd.users.Role
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class UsersTest {
    @Test
    fun `get me - happy path`() = testApplication {
        application {
            installTestModules()
        }

        client.get("/api/users/me") {
            authenticate(Role.USER)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun `get me - no access`() = testApplication {
        application {
            installTestModules()
        }

        client.get("/api/users/me").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun `put me - happy path`() = testApplication {
        application {
            installTestModules()
        }

        client.put("/api/users/me") {
            authenticate(Role.USER)
            contentType(ContentType.Application.Json)
            setBody("{\"username\":\"updatedUser\"}")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun `put me - no access`() = testApplication {
        application {
            installTestModules()
        }

        client.put("/api/users/me") {
            contentType(ContentType.Application.Json)
            setBody("{\"username\":\"updatedUser\"}")
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun `get user by id - ADMIN is allowed`() = testApplication {
        application {
            installTestModules()
        }

        client.get("/api/users/1") {
            authenticate(Role.ADMIN)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun `get user by id - OFFICIAL is forbidden`() = testApplication {
        application {
            installTestModules()
        }

        client.get("/api/users/1") {
            authenticate(Role.OFFICIAL)
        }.apply {
            assertEquals(HttpStatusCode.Forbidden, status)
        }
    }

    @Test
    fun `get user by id - USER is forbidden`() = testApplication {
        application {
            installTestModules()
        }

        client.get("/api/users/1") {
            authenticate(Role.USER)
        }.apply {
            assertEquals(HttpStatusCode.Forbidden, status)
        }
    }
}
