package de.ljunker.kasm.intellij

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType

class KasmSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer = KasmLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> =
        pack(TOKEN_HIGHLIGHTS[tokenType])

    companion object {
        private val INSTRUCTION = TextAttributesKey.createTextAttributesKey(
            "KASM_INSTRUCTION",
            DefaultLanguageHighlighterColors.KEYWORD
        )
        private val REGISTER = TextAttributesKey.createTextAttributesKey(
            "KASM_REGISTER",
            DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL
        )
        private val LABEL = TextAttributesKey.createTextAttributesKey(
            "KASM_LABEL",
            DefaultLanguageHighlighterColors.LABEL
        )
        private val NUMBER = TextAttributesKey.createTextAttributesKey(
            "KASM_NUMBER",
            DefaultLanguageHighlighterColors.NUMBER
        )
        private val COMMENT = TextAttributesKey.createTextAttributesKey(
            "KASM_COMMENT",
            DefaultLanguageHighlighterColors.LINE_COMMENT
        )
        private val COMMA = TextAttributesKey.createTextAttributesKey(
            "KASM_COMMA",
            DefaultLanguageHighlighterColors.COMMA
        )
        private val COLON = TextAttributesKey.createTextAttributesKey(
            "KASM_COLON",
            DefaultLanguageHighlighterColors.OPERATION_SIGN
        )
        private val BRACKET = TextAttributesKey.createTextAttributesKey(
            "KASM_BRACKET",
            DefaultLanguageHighlighterColors.BRACKETS
        )

        private val TOKEN_HIGHLIGHTS = mapOf(
            KasmTokenTypes.INSTRUCTION to INSTRUCTION,
            KasmTokenTypes.REGISTER to REGISTER,
            KasmTokenTypes.LABEL to LABEL,
            KasmTokenTypes.NUMBER to NUMBER,
            KasmTokenTypes.COMMENT to COMMENT,
            KasmTokenTypes.COMMA to COMMA,
            KasmTokenTypes.COLON to COLON,
            KasmTokenTypes.LEFT_BRACKET to BRACKET,
            KasmTokenTypes.RIGHT_BRACKET to BRACKET
        )
    }
}
