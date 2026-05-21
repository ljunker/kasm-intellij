package de.ljunker.kasm.intellij

import com.intellij.lexer.LexerBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

class KasmLexer : LexerBase() {
    private var buffer: CharSequence = ""
    private var bufferEnd: Int = 0
    private var tokenStart: Int = 0
    private var tokenEnd: Int = 0
    private var tokenType: IElementType? = null

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        bufferEnd = endOffset
        tokenStart = startOffset
        locateToken()
    }

    override fun getState(): Int = 0

    override fun getTokenType(): IElementType? = tokenType

    override fun getTokenStart(): Int = tokenStart

    override fun getTokenEnd(): Int = tokenEnd

    override fun advance() {
        tokenStart = tokenEnd
        locateToken()
    }

    override fun getBufferSequence(): CharSequence = buffer

    override fun getBufferEnd(): Int = bufferEnd

    private fun locateToken() {
        if (tokenStart >= bufferEnd) {
            tokenEnd = tokenStart
            tokenType = null
            return
        }

        val char = buffer[tokenStart]
        tokenType = when {
            char.isWhitespace() -> {
                tokenEnd = consumeWhile(tokenStart) { it.isWhitespace() }
                TokenType.WHITE_SPACE
            }

            char == ';' -> {
                tokenEnd = consumeWhile(tokenStart) { it != '\n' && it != '\r' }
                KasmTokenTypes.COMMENT
            }

            isIdentifierStart(char) -> {
                tokenEnd = consumeWhile(tokenStart, ::isIdentifierPart)
                identifierTokenType()
            }

            char.isDigit() -> {
                tokenEnd = consumeWhile(tokenStart) { it.isLetterOrDigit() || it == 'x' || it == 'b' }
                KasmTokenTypes.NUMBER
            }

            char == ',' -> {
                tokenEnd = tokenStart + 1
                KasmTokenTypes.COMMA
            }

            char == ':' -> {
                tokenEnd = tokenStart + 1
                KasmTokenTypes.COLON
            }

            char == '[' -> {
                tokenEnd = tokenStart + 1
                KasmTokenTypes.LEFT_BRACKET
            }

            char == ']' -> {
                tokenEnd = tokenStart + 1
                KasmTokenTypes.RIGHT_BRACKET
            }

            else -> {
                tokenEnd = tokenStart + 1
                KasmTokenTypes.OTHER
            }
        }
    }

    private fun consumeWhile(start: Int, predicate: (Char) -> Boolean): Int {
        var offset = start
        while (offset < bufferEnd && predicate(buffer[offset])) {
            offset++
        }
        return offset
    }

    private fun isIdentifierStart(char: Char): Boolean =
        char in 'A'..'Z' || char in 'a'..'z' || char == '_'

    private fun isIdentifierPart(char: Char): Boolean =
        isIdentifierStart(char) || char in '0'..'9'

    private fun identifierTokenType(): IElementType {
        val value = buffer.subSequence(tokenStart, tokenEnd).toString()

        return when {
            tokenEnd < bufferEnd && buffer[tokenEnd] == ':' -> KasmTokenTypes.LABEL
            KasmLanguageReference.isRegister(value) -> KasmTokenTypes.REGISTER
            KasmLanguageReference.isInstruction(value) -> KasmTokenTypes.INSTRUCTION
            else -> KasmTokenTypes.IDENTIFIER
        }
    }
}
