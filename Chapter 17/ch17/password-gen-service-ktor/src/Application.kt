package ch17.passwordGen

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.*
import io.ktor.client.request.get
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.*

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

private val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    val client = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = JacksonSerializer()
        }
    }

    suspend fun ApplicationCall.genPasswords(): GeneratorResult<String> {
        val length = parameters["length"]?.toIntOrNull()
            ?: return errorResult("Length must be an integer")
        val quantity = parameters["quantity"]?.toIntOrNull()
            ?: return errorResult("Quantity must be an integer")
        if (quantity <= 0) return errorResult("Quantity must be positive")
        val prefix = "http://localhost:8080/random/int"
        val url = "$prefix/from/0/to/${chars.lastIndex}/quantity/$length"
        val passwords = (1..quantity).map {
            val result = client.get<GeneratorResult<Int>>(url)
            String(result.values.map { chars[it] }.toCharArray())
        }
        return successResult(passwords)
    }

    routing {
        route("/password") {
            get("/length/{length}/quantity/{quantity}") {
                call.respond(call.genPasswords())
            }
        }
    }
}