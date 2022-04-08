package ch16.randomGen

import io.ktor.application.*
import io.ktor.html.respondHtml
import io.ktor.routing.*
import kotlinx.html.*
import kotlin.random.Random

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

private const val FROM_KEY = "from"
private const val TO_KEY = "to"
private const val COUNT_KEY = "count"
private const val GENERATE_KEY = "generate"

private suspend fun ApplicationCall.randomGeneratorForm() {
    respondHtml {
        val parameters = request.queryParameters

        val isGenerate = parameters.contains(GENERATE_KEY)
        var from: Int? = null
        var to: Int? = null
        var count: Int? = null

        val errors = HashMap<String, String>()

        if (isGenerate) {
            from = parameters[FROM_KEY]?.toIntOrNull()
            to = parameters[TO_KEY]?.toIntOrNull()
            count = parameters[COUNT_KEY]?.toIntOrNull()

            if (from == null) {
                errors[FROM_KEY] = "An integer is expected"
            }
            if (to == null) {
                errors[TO_KEY] = "An integer is expected"
            } else if (from != null && from > to) {
                errors[TO_KEY] = "'To' may not be less than 'From'"
            }
            if (count == null || count <= 0) {
                errors[COUNT_KEY] = "A positive integer is expected"
            }
        }

        fun FlowContent.appendError(key: String) {
            if (!isGenerate) return
            errors[key]?.let { strong { +" $it" } }
        }

        head { title("Random number generator") }
        body {
            h1 { +"Generate random numbers" }
            form(action = "/", method = FormMethod.get) {
                p { +"From: " }
                p {
                    numberInput(name = FROM_KEY) {
                        value = from?.toString() ?: "1"
                    }
                    appendError(FROM_KEY)
                }
                p { +"To: " }
                p {
                    numberInput(name = TO_KEY) {
                        value = to?.toString() ?: "100"
                    }
                    appendError(TO_KEY)
                }
                p { +"How many: " }
                p {
                    numberInput(name = COUNT_KEY) {
                        value = count?.toString() ?: "10"
                    }
                    appendError(COUNT_KEY)
                }
                p { hiddenInput(name = GENERATE_KEY) { value = "" } }
                p { submitInput { value = "Generate" } }
            }
            if (isGenerate && errors.isEmpty()) {
                h2 { +"Results:" }
                p {
                    repeat(count!!) {
                        +"${Random.nextInt(from!!, to!! + 1)} "
                    }
                }
            }
        }
    }
}

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    routing {
        get("/") { call.randomGeneratorForm() }
    }
}