package com.example.stackoverflow

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.apache.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket


/**
 * Minimal reproduction example:
 *   - create client
 *   - start fake server that produces a socket timeout
 *   - execute HTTP request
 */
fun reproduce(engineFactory: HttpClientEngineFactory<*>) {
    val client = HttpClient(engineFactory) {
        expectSuccess = false
    }

    val server = socketTimeoutThread()
    server.start()

    runBlocking {
        println("*** Executing request for ${engineFactory::class.simpleName}")
        client.get("http://localhost:$mockServerPort/")
    }
}


class OkHttpApp : RequestHandler<Any, Any> {
    override fun handleRequest(input: Any, context: Context) {
        reproduce(OkHttp)
    }
}

class ApacheApp : RequestHandler<Any, Any> {
    override fun handleRequest(input: Any, context: Context) {
        reproduce(Apache)
    }
}

fun main(args: Array<String>) {
    when {
        args.size == 1 && args[0] == "OkHttp" -> reproduce(OkHttp)
        args.size == 1 && args[0] == "Apache" -> reproduce(Apache)
        else -> error("Expected 'OkHttp' or 'Apache' as only argument")
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