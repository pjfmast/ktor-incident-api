package avans.avd

import avans.avd.users.Role
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class IncidentsTest {
    // Todo fix this for Koin di

    @Test
    fun `list of incidents - happy path`() = testApplication {
        client.get("/api/incidents") {
            authenticate(Role.ADMIN)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun `list of incidents - no access`() = testApplication {
        client.get("/api/incidents").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }
}