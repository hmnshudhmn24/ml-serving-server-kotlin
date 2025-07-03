
package com.example

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.serialization.*
import kotlinx.serialization.Serializable
import kotlinx.coroutines.*
import io.ktor.client.*
import io.ktor.client.request.*

@Serializable
data class PredictionRequest(val feature1: Double, val feature2: Double)

@Serializable
data class PredictionResponse(val prediction: String)

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json()
        }
        install(CORS) {
            anyHost()
        }
        routing {
            post("/predict/modelA") {
                val req = call.receive<PredictionRequest>()
                val prediction = callModelA(req)
                call.respond(prediction)
            }

            post("/predict/modelB") {
                val req = call.receive<PredictionRequest>()
                val prediction = callModelB(req)
                call.respond(prediction)
            }

            get("/") {
                call.respondText("Kotlin ML Model Serving Server is running!", ContentType.Text.Plain)
            }
        }
    }.start(wait = true)
}

suspend fun callModelA(request: PredictionRequest): PredictionResponse {
    val client = HttpClient()
    return try {
        val remotePrediction: String = client.post("http://localhost:5000/modelA/predict") {
            contentType(ContentType.Application.Json)
            body = request
        }
        PredictionResponse(prediction = remotePrediction)
    } catch (e: Exception) {
        PredictionResponse(prediction = "error")
    } finally {
        client.close()
    }
}

suspend fun callModelB(request: PredictionRequest): PredictionResponse {
    delay(100) // simulate latency
    return PredictionResponse(prediction = if (request.feature1 > 2) "positive" else "negative")
}
