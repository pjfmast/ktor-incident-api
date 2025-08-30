package avans.avd

import avans.avd.users.Role
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class IncidentsTest {
    @Test
    fun `list of incidents - happy path`() = testApplication {
        application {
            installTestModules()
        }

        client.get("/api/incidents") {
            authenticate(Role.ADMIN)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun `list of incidents - no access`() = testApplication {
        application {
            installTestModules()
        }

        client.get("/api/incidents").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }
}