package com.dergoogler.mmrl.ui.activity.terminal

internal class ActionCommand private constructor(val command: String) {
    val properties = mutableMapOf<String, String>()
    var data: String = ""

    companion object {
        private const val PREFIX = "##["
        private const val COMMAND_KEY = "::"

        private val escapeMappings = listOf(
            EscapeMapping(";", "%3B"),
            EscapeMapping("\r", "%0D"),
            EscapeMapping("\n", "%0A"),
            EscapeMapping("]", "%5D"),
            EscapeMapping("%", "%25")
        )

        private val escapeDataMappings = listOf(
            EscapeMapping("\r", "%0D"),
            EscapeMapping("\n", "%0A"),
            EscapeMapping("%", "%25")
        )

        private val escapePropertyMappings = listOf(
            EscapeMapping("\r", "%0D"),
            EscapeMapping("\n", "%0A"),
            EscapeMapping(":", "%3A"),
            EscapeMapping(",", "%2C"),
            EscapeMapping("%", "%25")
        )

        fun tryParseV2(message: String?, registeredCommands: Set<String>): ActionCommand? {
            if (message.isNullOrBlank()) return null

            try {
                val trimmed = message.trimStart()
                if (!trimmed.startsWith(COMMAND_KEY)) return null

                val endIndex = trimmed.indexOf(COMMAND_KEY, COMMAND_KEY.length)
                if (endIndex < 0) return null

                val cmdInfo = trimmed.substring(COMMAND_KEY.length, endIndex)
                val spaceIndex = cmdInfo.indexOf(' ')
                val commandName = if (spaceIndex < 0) cmdInfo else cmdInfo.substring(0, spaceIndex)

                if (!registeredCommands.contains(commandName)) return null

                val cmd = ActionCommand(commandName)

                if (spaceIndex > 0) {
                    val propertiesStr = cmdInfo.substring(spaceIndex + 1).trim()
                    val splitProperties = propertiesStr.split(",").filter { it.isNotEmpty() }
                    for (propertyStr in splitProperties) {
                        val pair = propertyStr.split("=", limit = 2)
                        if (pair.size == 2) {
                            cmd.properties[pair[0]] = unescapeProperty(pair[1])
                        }
                    }
                }

                cmd.data = unescapeData(trimmed.substring(endIndex + COMMAND_KEY.length))
                return cmd
            } catch (e: Exception) {
                return null
            }
        }

        fun tryParse(message: String?, registeredCommands: Set<String>): ActionCommand? {
            if (message.isNullOrBlank()) return null

            try {
                val prefixIndex = message.indexOf(PREFIX)
                if (prefixIndex < 0) return null

                val rbIndex = message.indexOf(']', prefixIndex)
                if (rbIndex < 0) return null

                val cmdInfo = message.substring(prefixIndex + PREFIX.length, rbIndex)
                val spaceIndex = cmdInfo.indexOf(' ')
                val commandName = if (spaceIndex < 0) cmdInfo else cmdInfo.substring(0, spaceIndex)

                if (!registeredCommands.contains(commandName)) return null

                val cmd = ActionCommand(commandName)

                if (spaceIndex > 0) {
                    val propertiesStr = cmdInfo.substring(spaceIndex + 1)
                    val splitProperties = propertiesStr.split(";").filter { it.isNotEmpty() }
                    for (propertyStr in splitProperties) {
                        val pair = propertyStr.split("=", limit = 2)
                        if (pair.size == 2) {
                            cmd.properties[pair[0]] = unescape(pair[1])
                        }
                    }
                }

                cmd.data = unescape(message.substring(rbIndex + 1))
                return cmd
            } catch (e: Exception) {
                return null
            }
        }

        private fun unescape(escaped: String?): String {
            var unescaped = escaped ?: return ""
            for (mapping in escapeMappings) {
                unescaped = unescaped.replace(mapping.replacement, mapping.token)
            }
            return unescaped
        }

        private fun unescapeProperty(escaped: String?): String {
            var unescaped = escaped ?: return ""
            for (mapping in escapePropertyMappings) {
                unescaped = unescaped.replace(mapping.replacement, mapping.token)
            }
            return unescaped
        }

        private fun unescapeData(escaped: String?): String {
            var unescaped = escaped ?: return ""
            for (mapping in escapeDataMappings) {
                unescaped = unescaped.replace(mapping.replacement, mapping.token)
            }
            return unescaped
        }

        private data class EscapeMapping(val token: String, val replacement: String)
    }
}
