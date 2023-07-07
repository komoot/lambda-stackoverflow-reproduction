package de.komoot

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler


class App : RequestHandler<Any, Any> {
    override fun handleRequest(input: Any, context: Context): Any {

        println("Been called with: $input")

        return input
    }
}