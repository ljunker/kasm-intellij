package de.ljunker.kasm.intellij

import com.intellij.psi.tree.IElementType
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class KasmSyntaxHighlighterTest : BasePlatformTestCase() {
    fun testLexesReferenceTokensForHighlighting() {
        val tokenTypes = lex(
            """
            |.equ START_VALUE, (0x20 + 1)
            |loop:
            |LOAD R1, [message + R2] ; indexed memory
            |message: .string "count; down"
            |JNZ R1, loop
            """.trimMargin()
        )

        assertContainsElements(
            tokenTypes,
            KasmTokenTypes.DIRECTIVE,
            KasmTokenTypes.LABEL,
            KasmTokenTypes.INSTRUCTION,
            KasmTokenTypes.REGISTER,
            KasmTokenTypes.IDENTIFIER,
            KasmTokenTypes.NUMBER,
            KasmTokenTypes.STRING,
            KasmTokenTypes.LEFT_BRACKET,
            KasmTokenTypes.RIGHT_BRACKET,
            KasmTokenTypes.LEFT_PARENTHESIS,
            KasmTokenTypes.RIGHT_PARENTHESIS,
            KasmTokenTypes.OPERATOR,
            KasmTokenTypes.COMMENT
        )
    }

    fun testMapsInstructionTokenToHighlighterAttributes() {
        val keys = KasmSyntaxHighlighter()
            .getTokenHighlights(KasmTokenTypes.INSTRUCTION)
            .map { it.externalName }

        assertContainsElements(keys, "KASM_INSTRUCTION")
    }

    fun testMapsDirectiveAndStringTokensToHighlighterAttributes() {
        val highlighter = KasmSyntaxHighlighter()
        val directiveKeys = highlighter
            .getTokenHighlights(KasmTokenTypes.DIRECTIVE)
            .map { it.externalName }
        val stringKeys = highlighter
            .getTokenHighlights(KasmTokenTypes.STRING)
            .map { it.externalName }

        assertContainsElements(directiveKeys, "KASM_DIRECTIVE")
        assertContainsElements(stringKeys, "KASM_STRING")
    }

    private fun lex(source: String): List<IElementType> {
        val lexer = KasmLexer()
        lexer.start(source)
        val tokenTypes = mutableListOf<IElementType>()

        while (lexer.tokenType != null) {
            tokenTypes += lexer.tokenType!!
            lexer.advance()
        }

        return tokenTypes
    }
}
