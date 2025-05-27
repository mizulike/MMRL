package com.dergoogler.mmrl.webui.util

import android.content.Context
import com.dergoogler.mmrl.webui.R
import kotlinx.html.b
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.i
import kotlinx.html.lang
import kotlinx.html.li
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.onClick
import kotlinx.html.span
import kotlinx.html.stream.appendHTML
import kotlinx.html.title
import kotlinx.html.ul

internal fun WebUIOptions.getRequireNewVersion(
    context: Context,
) = buildString {
    val rq = requireNewAppVersion
    val supportText = rq?.supportText
    val supportLink = rq?.supportLink
    val requiredCode = rq?.requiredCode
    val appName = rq?.packageInfo?.applicationInfo?.loadLabel(packageManager).toString()

    with(context) {
        appendHTML().html {
            lang = "en"
            head {
                meta {
                    name = "viewport"
                    content =
                        "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0"
                }
                link {
                    rel = "stylesheet"
                    href = "${domain}/internal/insets.css"
                }
                link {
                    rel = "stylesheet"
                    href = "${domain}/internal/colors.css"
                }
                link {
                    rel = "stylesheet"
                    href = "${domain}/internal/assets/webui/requireNewVersion.css"
                }
                title { +"New App Version Required" }
            }
            body {
                div(classes = "container") {
                    div(classes = "content") {
                        div(classes = "title") { +getString(R.string.requireNewVersion_cannot_load_webui) }
                        div {
                            b { +modId.id }
                            +" "
                            +getString(R.string.requireNewVersion_require_text, appName)
                            +" "
                            i { +requiredCode.toString() }
                        }
                        div(classes = "list") {
                            span { +getString(R.string.requireNewVersion_try_the_following) }
                            ul {
                                li {
                                    +getString(
                                        R.string.requireNewVersion_try_the_following_one,
                                        appName
                                    )
                                }
                                li { +getString(R.string.requireNewVersion_try_the_following_two) }
                            }
                        }
                        div(classes = "code") { +"ERR_NEW_WX_VERSION_REQUIRED" }
                        div(classes = "buttons") {
                            button(classes = "refresh") {
                                onClick = "location.reload();"
                                +getString(R.string.requireNewVersion_refresh)
                            }

                            if (supportLink != null && supportText != null) {
                                button(classes = "more") {
                                    attributes["onclick"] = "window.open('$supportLink');"
                                    +supportText
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}