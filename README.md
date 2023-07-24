# Minimal reproduction example for java.lang.StackOverflowError in lambda runtime

This demonstrates a `java.lang.StackOverflowError` error in the AWS Lambda runtime for Java 11 that is caused by 
a `SocketTimeoutException` within in the `ktor-client-apache` package.

The minimal reproducing code can be found in `com.example.stackoverflow.AppKt.reproduce`. 

If you execute the code via the accompanied main function will see a stack trace printed in both cases. However, if the
code is executed inside a lambda handler function you will see that it does not produce a stack trace for the version
that uses the apache implementation for the ktor client. It is not necessary to deploy the function, it can be reproduced 
locally. Running `sam local invoke Apache` results in:

```
Invoking com.example.stackoverflow.ApacheApp::handleRequest (java11)
[...]
*** Executing request for Apache
java.lang.StackOverflowError: java.lang.StackOverflowError
java.lang.StackOverflowError
	at java.base/java.lang.Class.getComponentType(Unknown Source)
	at java.base/java.util.Arrays.copyOf(Unknown Source)
	at java.base/java.util.ArrayList.toArray(Unknown Source)
	at java.base/java.lang.Throwable.getSuppressed(Unknown Source)

{"errorMessage":"java.lang.StackOverflowError","errorType":"java.lang.StackOverflowError","stackTrace":["java.base/java.lang.Class.getComponentType(Unknown Source)","java.base/java.util.Arrays.copyOf(Unknown Source)","java.base/java.util.ArrayList.toArray(Unknown Source)","java.base/java.lang.Throwable.getSuppressed(Unknown Source)"]}END RequestId: 95ee7a54-5819-4d0a-ab4c-42dffc1b782c
REPORT RequestId: 95ee7a54-5819-4d0a-ab4c-42dffc1b782c	Init Duration: 0.99 ms	Duration: 12012.26 ms	Billed Duration: 12013 ms	Memory Size: 512 MB	Max Memory Used: 512 MB
```

Using the OkHttp based client with `sam local invoke OkHttp` works:

```
Invoking com.example.stackoverflow.OkHttpApp::handleRequest (java11)
[...]
*** Executing request for OkHttp
{"errorMessage":"Socket timeout has expired [url=http://localhost:15467/, socket_timeout=unknown] ms","errorType":"io.ktor.client.network.sockets.SocketTimeoutException","stackTrace":["io.ktor.client.plugins.HttpTimeoutKt.SocketTimeoutException(HttpTimeout.kt:239)","io.ktor.client.engine.okhttp.OkUtilsKt.mapOkHttpException(OkUtils.kt:80)","io.ktor.client.engine.okhttp.OkUtilsKt.access$mapOkHttpException(OkUtils.kt:1)","io.ktor.client.engine.okhttp.OkHttpCallback.onFailure(OkUtils.kt:39)","okhttp3.internal.connection.RealCall$AsyncCall.run(RealCall.kt:525)","java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source)","java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source)","java.base/java.lang.Thread.run(Unknown Source)"],"cause":{"errorMessage":"timeout","errorType":"java.net.SocketTimeoutException","stackTrace":["okio.SocketAsyncTimeout.newTimeoutException(JvmOkio.kt:147)","okio.AsyncTimeout.access$newTimeoutException(AsyncTimeout.kt:158)","okio.AsyncTimeout$source$1.read(AsyncTimeout.kt:337)","okio.RealBufferedSource.indexOf(RealBufferedSource.kt:427)","okio.RealBufferedSource.readUtf8LineStrict(RealBufferedSource.kt:320)","okhttp3.internal.http1.HeadersReader.readLine(HeadersReader.kt:29)","okhttp3.internal.http1.HeadersReader.readHeaders(HeadersReader.kt:38)","okhttp3.internal.http1.Http1ExchangeCodec.readResponseHeaders(Http1ExchangeCodec.kt:186)","okhttp3.internal.connection.Exchange.readResponseHeaders(Exchange.kt:110)","okhttp3.internal.http.CallServerInterceptor.intercept(CallServerInterceptor.kt:93)","okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)","okhttp3.internal.connection.ConnectInterceptor.intercept(ConnectInterceptor.kt:34)","okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)","okhttp3.internal.cache.CacheInterceptor.intercept(CacheInterceptor.kt:95)","okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)","okhttp3.internal.http.BridgeInterceptor.intercept(BridgeInterceptSocket timeout has expired [url=http://localhost:15467/, socket_timeout=unknown] ms: io.ktor.client.network.sockets.SocketTimeoutException
io.ktor.client.network.sockets.SocketTimeoutException: Socket timeout has expired [url=http://localhost:15467/, socket_timeout=unknown] ms
	at io.ktor.client.plugins.HttpTimeoutKt.SocketTimeoutException(HttpTimeout.kt:239)
	at io.ktor.client.engine.okhttp.OkUtilsKt.mapOkHttpException(OkUtils.kt:80)
	at io.ktor.client.engine.okhttp.OkUtilsKt.access$mapOkHttpException(OkUtils.kt:1)
	at io.ktor.client.engine.okhttp.OkHttpCallback.onFailure(OkUtils.kt:39)
	at okhttp3.internal.connection.RealCall$AsyncCall.run(RealCall.kt:525)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source)
	at java.base/java.lang.Thread.run(Unknown Source)
Caused by: java.net.SocketTimeoutException: timeout
	at okio.SocketAsyncTimeout.newTimeoutException(JvmOkio.kt:147)
	at okio.AsyncTimeout.access$newTimeoutException(AsyncTimeout.kt:158)
	at okio.AsyncTimeout$source$1.read(AsyncTimeout.kt:337)
	at okio.RealBufferedSource.indexOf(RealBufferedSource.kt:427)
	at okio.RealBufferedSource.readUtf8LineStrict(RealBufferedSource.kt:320)
	at okhttp3.internal.http1.HeadersReader.readLine(HeadersReader.kt:29)
	at okhttp3.internal.http1.HeadersReader.readHeaders(HeadersReader.kt:38)
	at okhttp3.internal.http1.Http1ExchangeCodec.readResponseHeaders(Http1ExchangeCodec.kt:186)
	at okhttp3.internal.connection.Exchange.readResponseHeaders(Exchange.kt:110)
	at okhttp3.internal.http.CallServerInterceptor.intercept(CallServerInterceptor.kt:93)
	at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
	at okhttp3.internal.connection.ConnectInterceptor.intercept(ConnectInterceptor.kt:34)
	at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
	at okhttp3.internal.cache.CacheInterceptor.intercept(CacheInterceptor.kt:95)
	at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
	at okhttp3.internal.http.BridgeInterceptor.intercept(BridgeInterceptor.kt:83)
	at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
	at okhttp3.internal.http.RetryAndFollowUpInterceptor.intercept(RetryAndFollowUpInterceptor.kt:76)
	at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
	at okhttp3.internal.connection.RealCall.getResponseWithInterceptorChain$okhttp(RealCall.kt:201)
	at okhttp3.internal.connection.RealCall$AsyncCall.run(RealCall.kt:517)
	... 3 more
Caused by: java.net.SocketException: Socket closed
	at java.base/java.net.SocketInputStream.read(Unknown Source)
	at java.base/java.net.SocketInputStream.read(Unknown Source)
	at okio.InputStreamSource.read(JvmOkio.kt:94)
	at okio.AsyncTimeout$source$1.read(AsyncTimeout.kt:125)
	... 21 more

END RequestId: 1cab91fa-4292-474c-ae0e-7c777580cc30
```

This is clearly somehow tied to `ktor-client-apache-jvm` however, I don't think it should be possible to caused stack 
overflow error inside the lambda runtime just by throwing an exception.

Crucially this has also implication for things integrating with Lambda. In our case we can not correctly handle socket
timeouts in lambda functions that are executed as part of AWS Step Functions, since the step function runtime is not 
passed back the actual error, so we can not define a retry configuration for this.

## Prerequisites

This application uses SAM. 

- Java 11
- Apache Maven
- [AWS SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html)
- Docker
