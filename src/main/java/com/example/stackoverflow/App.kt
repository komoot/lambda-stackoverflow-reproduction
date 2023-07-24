package com.example.stackoverflow

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket


class App : RequestHandler<Any, Any> {
    override fun handleRequest(input: Any, context: Context): Unit {
        reproduce()
    }
}

fun main() {
    reproduce()
}

fun reproduce() {
    val client = HttpClient(Apache) {
        expectSuccess = false
        engine {
            // this is just so we can debug faster
            socketTimeout = 500
        }
    }

    val server = socketTimeoutThread()
    server.start()

    runBlocking {
        println("Executing request")
        client.get("http://localhost:$mockServerPort/")
    }
}

private const val mockServerPort = 15467


/**
 * A one-thread-one-request socket server the reliable produces a socket timeout
 */
fun socketTimeoutThread() = Thread {
    val serverSocket = ServerSocket(mockServerPort)
    val clientSocket = serverSocket.accept()

    val inputStream = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
    val outputStream = clientSocket.getOutputStream()

    var line: String?

    while (inputStream.readLine().also { line = it } != null) {
        if (line!!.isEmpty()) {
            break
        }
    }

    // write partial response and then hang
    outputStream.write("""HTTP/1.1 200 OK
            Content-Type: application/json
            
            {""".trimIndent().toByteArray())
    outputStream.flush()
    try {
        Thread.sleep(2000)
    } catch (e: InterruptedException) {
        // exit
    }
}