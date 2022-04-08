package ch16.sessionDemo

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlinx.html.*

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

data class Stat(val viewCount: Int)

private const val STAT_KEY = "STAT"

private suspend fun ApplicationCall.rootPage() {
    val stat = sessions.getOrSet { Stat(0) }
    sessions.set(stat.copy(viewCount = stat.viewCount + 1))
    respondHtml {
        body {
            h2 { +"You have viewed this page ${stat.viewCount} time(s)" }
            a("/clearStat") { +"Clear statistics" }
        }
    }
}

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    install(Sessions) {
        cookie<Stat>(STAT_KEY)
    }

    routing {
        get("/") {
            call.rootPage()
        }
        get("/clearStat") {
            call.sessions.clear(STAT_KEY)
            call.respondRedirect("/")
        }
    }
}