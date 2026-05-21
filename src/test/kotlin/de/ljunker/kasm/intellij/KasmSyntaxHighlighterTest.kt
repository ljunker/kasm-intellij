package de.ljunker.kasm.intellij

import com.intellij.psi.tree.IElementType
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class KasmSyntaxHighlighterTest : BasePlatformTestCase() {
    fun testLexesReferenceTokensForHighlighting() {
        val tokenTypes = lex(
            """
            |loop:
            |LOAD R1, [0x28] ; direct memory
            |JNZ R1, loop
            """.trimMargin()
        )

        assertContainsElements(
            tokenTypes,
            KasmTokenTypes.LABEL,
            KasmTokenTypes.INSTRUCTION,
            KasmTokenTypes.REGISTER,
            KasmTokenTypes.NUMBER,
            KasmTokenTypes.LEFT_BRACKET,
            KasmTokenTypes.RIGHT_BRACKET,
            KasmTokenTypes.COMMENT
        )
    }

    fun testMapsInstructionTokenToHighlighterAttributes() {
        val keys = KasmSyntaxHighlighter()
            .getTokenHighlights(KasmTokenTypes.INSTRUCTION)
            .map { it.externalName }

        assertContainsElements(keys, "KASM_INSTRUCTION")
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
