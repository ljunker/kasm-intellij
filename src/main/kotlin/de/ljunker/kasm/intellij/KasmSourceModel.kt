package de.ljunker.kasm.intellij

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText

internal data class KasmSymbolDefinitionLocation(
    val name: String,
    val filePath: String,
    val lineNumber: Int,
    val startOffset: Int
)

internal object KasmSourceModel {
    fun collectSymbols(
        rootPath: Path,
        rootSource: String
    ): List<KasmSymbolDefinitionLocation> {
        val symbols = mutableListOf<KasmSymbolDefinitionLocation>()
        parseSource(
            source = rootSource,
            sourcePath = rootPath.toAbsolutePath().normalize(),
            includeStack = emptySet(),
            onSymbol = { name, filePath, lineNumber, startOffset ->
                symbols += KasmSymbolDefinitionLocation(
                    name = name,
                    filePath = filePath,
                    lineNumber = lineNumber,
                    startOffset = startOffset
                )
            }
        )
        return symbols
    }

    private fun parseSource(
        source: String,
        sourcePath: Path,
        includeStack: Set<Path>,
        onSymbol: (String, String, Int, Int) -> Unit
    ) {
        val normalizedSourcePath = sourcePath.toAbsolutePath().normalize()
        val sourceDirectory = normalizedSourcePath.parent ?: Path.of(".").toAbsolutePath().normalize()
        val filePath = normalizedSourcePath.toString()
        var lineStart = 0
        var lineNumber = 1

        while (lineStart <= source.length) {
            val lineEnd = lineEnd(source, lineStart)
            val rawLine = source.substring(lineStart, lineEnd)
            parseLine(
                rawLine = rawLine,
                lineStart = lineStart,
                lineNumber = lineNumber,
                filePath = filePath,
                sourceDirectory = sourceDirectory,
                includeStack = includeStack + normalizedSourcePath,
                onSymbol = onSymbol
            )

            if (lineEnd >= source.length) {
                break
            }
            lineStart = nextLineStart(source, lineEnd)
            lineNumber++
        }
    }

    private fun parseLine(
        rawLine: String,
        lineStart: Int,
        lineNumber: Int,
        filePath: String,
        sourceDirectory: Path,
        includeStack: Set<Path>,
        onSymbol: (String, String, Int, Int) -> Unit
    ) {
        val strippedLine = stripComment(rawLine)
        var statement = strippedLine.trim()
        var searchStart = rawLine.indexOf(statement).takeIf { it >= 0 } ?: 0

        while (statement.isNotBlank()) {
            val labelMatch = LABEL_REGEX.matchEntire(statement)
            if (labelMatch != null) {
                val labelName = labelMatch.groupValues[1]
                val labelOffset = rawLine.indexOf(labelName, searchStart)
                    .takeIf { it >= 0 }
                    ?: searchStart
                onSymbol(labelName, filePath, lineNumber, lineStart + labelOffset)

                statement = labelMatch.groupValues[2].trim()
                searchStart = if (statement.isBlank()) {
                    rawLine.length
                } else {
                    rawLine.indexOf(statement, labelOffset + labelName.length + 1)
                        .takeIf { it >= 0 }
                        ?: (labelOffset + labelName.length + 1)
                }
                continue
            }

            val parts = statement.split(Regex("\\s+"), limit = 2)
            val name = parts[0].uppercase()

            if (name == ".INCLUDE") {
                readIncludedSource(
                    argument = parts.getOrNull(1).orEmpty(),
                    sourceDirectory = sourceDirectory,
                    includeStack = includeStack,
                    onSymbol = onSymbol
                )
            } else if (name == ".EQU" || name == ".FILE") {
                parseArguments(parts.getOrNull(1).orEmpty())
                    .firstOrNull()
                    ?.takeIf(SYMBOL_NAME::matches)
                    ?.let { symbolName ->
                        val symbolOffset = rawLine.indexOf(symbolName, searchStart + parts[0].length)
                            .takeIf { it >= 0 }
                            ?: searchStart
                        onSymbol(symbolName, filePath, lineNumber, lineStart + symbolOffset)
                    }
            }

            statement = ""
        }
    }

    private fun readIncludedSource(
        argument: String,
        sourceDirectory: Path,
        includeStack: Set<Path>,
        onSymbol: (String, String, Int, Int) -> Unit
    ) {
        val sourcePath = parseStringLiteral(argument) ?: return
        val path = resolvePath(sourcePath, sourceDirectory)
        val stackPath = path.toAbsolutePath().normalize()
        if (stackPath in includeStack || !Files.exists(path)) {
            return
        }

        val includedSource = try {
            path.readText()
        } catch (_: IOException) {
            return
        }

        parseSource(
            source = includedSource,
            sourcePath = stackPath,
            includeStack = includeStack,
            onSymbol = onSymbol
        )
    }

    private fun parseArguments(value: String): List<String> {
        val arguments = mutableListOf<String>()
        val current = StringBuilder()
        var inString = false
        var escaped = false

        value.forEach { char ->
            when {
                escaped -> {
                    current.append(char)
                    escaped = false
                }

                inString && char == '\\' -> {
                    current.append(char)
                    escaped = true
                }

                char == '"' -> {
                    current.append(char)
                    inString = !inString
                }

                char == ',' && !inString -> {
                    arguments += current.toString().trim()
                    current.clear()
                }

                else ->
                    current.append(char)
            }
        }

        current.toString().trim()
            .takeIf(String::isNotBlank)
            ?.let(arguments::add)

        return arguments
    }

    private fun stripComment(line: String): String {
        var inString = false
        var escaped = false

        line.forEachIndexed { index, char ->
            when {
                escaped -> escaped = false
                inString && char == '\\' -> escaped = true
                char == '"' -> inString = !inString
                char == ';' && !inString -> return line.substring(0, index)
            }
        }

        return line
    }

    private fun parseStringLiteral(value: String): String? {
        val trimmed = value.trim()
        if (!trimmed.startsWith('"')) {
            return null
        }

        val decoded = StringBuilder()
        var index = 1
        var escaped = false

        while (index < trimmed.length) {
            val char = trimmed[index++]
            when {
                escaped -> {
                    decoded.append(
                        when (char) {
                            '0' -> '\u0000'
                            'n' -> '\n'
                            'r' -> '\r'
                            't' -> '\t'
                            '"' -> '"'
                            '\\' -> '\\'
                            else -> char
                        }
                    )
                    escaped = false
                }

                char == '\\' ->
                    escaped = true

                char == '"' ->
                    return decoded.toString()

                else ->
                    decoded.append(char)
            }
        }

        return null
    }

    private fun resolvePath(sourcePath: String, sourceDirectory: Path): Path {
        val path = Path.of(sourcePath)
        return if (path.isAbsolute) {
            path.normalize()
        } else {
            sourceDirectory.resolve(path).normalize()
        }
    }

    private fun lineEnd(source: String, start: Int): Int {
        var offset = start
        while (offset < source.length && source[offset] != '\n' && source[offset] != '\r') {
            offset++
        }
        return offset
    }

    private fun nextLineStart(source: String, lineEnd: Int): Int {
        var offset = lineEnd
        if (offset < source.length && source[offset] == '\r') {
            offset++
        }
        if (offset < source.length && source[offset] == '\n') {
            offset++
        }
        return offset
    }

    private val LABEL_REGEX =
        Regex("""^([A-Za-z_][A-Za-z0-9_]*):\s*(.*)$""")

    private val SYMBOL_NAME =
        Regex("""[A-Za-z_][A-Za-z0-9_]*""")
}
