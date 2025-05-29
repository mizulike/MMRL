package com.dergoogler.mmrl.webui

import android.webkit.WebResourceResponse
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dergoogler.mmrl.platform.file.SuFile
import java.io.IOException

enum class ResponseStatus(val code: Int, val reasonPhrase: String) {
    OK(200, "OK"),
    NOT_FOUND(404, "Not Found"),
    FORBIDDEN(403, "Forbidden"),
}

private fun SuFile.checkStatus(): ResponseStatus {
    val dir = getCanonicalDirPath()

    for (forbiddenPath in listOf("/data/data", "/data/system")) {
        if (dir.startsWith(forbiddenPath)) {
            return ResponseStatus.FORBIDDEN
        }
    }

    if (!exists()) return ResponseStatus.NOT_FOUND

    return ResponseStatus.OK
}

const val encoding = "UTF-8"

val headers
    get() = mapOf(
        "Client-Via" to "MMRL WebUI",
        "Access-Control-Allow-Origin" to "*",
    )

enum class InjectionType {
    HEAD, BODY
}

fun MutableList<Injection>.addInjection(
    type: InjectionType = InjectionType.HEAD,
    code: StringBuilder.() -> Unit,
) = addInjection(buildString(code), type)

fun MutableList<Injection>.addInjection(
    code: String,
    type: InjectionType = InjectionType.HEAD,
) = add(Injection(type, code))

data class Injection(
    val type: InjectionType,
    val code: String,
)

@Throws(IOException::class)
fun SuFile.asResponse(injects: List<Injection>? = null): WebResourceResponse {
    val mimeType = MimeUtil.getMimeFromFileName(path)
    val status = checkStatus()

    val err = WebResourceResponse(
        null,
        encoding,
        status.code,
        status.reasonPhrase,
        headers,
        null
    )

    return when (status) {
        ResponseStatus.FORBIDDEN -> err

        ResponseStatus.OK -> {
            var stream by mutableStateOf(newInputStream())

            if (injects != null) {
                for (inject in injects) {
                    stream = when (inject.type) {
                        InjectionType.HEAD -> stream.headInject(inject.code)
                        InjectionType.BODY -> stream.bodyInject(inject.code)
                    }
                }
            }

            val `is` = handleSvgzStream(stream)

            return WebResourceResponse(
                mimeType,
                encoding,
                status.code,
                status.reasonPhrase,
                headers,
                `is`
            )
        }

        ResponseStatus.NOT_FOUND -> err
    }
}

val notFoundResponse = WebResourceResponse(
    null,
    encoding,
    ResponseStatus.NOT_FOUND.code,
    ResponseStatus.NOT_FOUND.reasonPhrase,
    headers,
    null
)

val forbiddenResponse = WebResourceResponse(
    null,
    encoding,
    ResponseStatus.FORBIDDEN.code,
    ResponseStatus.FORBIDDEN.reasonPhrase,
    headers,
    null
)