package avans.avd

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthTest {
    @Test
    fun `login bad password`() = testApplication {
        application {
            installTestModules()
        }

        client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("{\"username\":\"Henk\",\"password\":\"pwd0\"}")
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun `login happy path`() = testApplication {
        application {
            installTestModules()
        }

        client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("{\"username\":\"Henk\",\"password\":\"pwd\"}")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }
}
