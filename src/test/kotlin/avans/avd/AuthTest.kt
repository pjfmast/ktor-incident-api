package avans.avd

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthTest {

    @Test
    fun `login bad password`() = testApplication {
        client.post("/api/auth") {
            contentType(ContentType.Application.Json)
            setBody("{\"username\":\"Henk\",\"password\":\"pwd0\"}")
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun `login happy path`() = testApplication {
        client.post("/api/auth") {
            contentType(ContentType.Application.Json)
            setBody("{\"username\":\"Henk\",\"password\":\"pwd\"}")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

}
