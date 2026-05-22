package de.ljunker.kasm.intellij

import com.intellij.psi.tree.IElementType

object KasmTokenTypes {
    val IDENTIFIER = IElementType("KASM_IDENTIFIER", KasmLanguage)
    val DIRECTIVE = IElementType("KASM_DIRECTIVE", KasmLanguage)
    val INSTRUCTION = IElementType("KASM_INSTRUCTION", KasmLanguage)
    val LABEL = IElementType("KASM_LABEL", KasmLanguage)
    val REGISTER = IElementType("KASM_REGISTER", KasmLanguage)
    val NUMBER = IElementType("KASM_NUMBER", KasmLanguage)
    val STRING = IElementType("KASM_STRING", KasmLanguage)
    val COMMENT = IElementType("KASM_COMMENT", KasmLanguage)
    val COMMA = IElementType("KASM_COMMA", KasmLanguage)
    val COLON = IElementType("KASM_COLON", KasmLanguage)
    val LEFT_BRACKET = IElementType("KASM_LEFT_BRACKET", KasmLanguage)
    val RIGHT_BRACKET = IElementType("KASM_RIGHT_BRACKET", KasmLanguage)
    val LEFT_PARENTHESIS = IElementType("KASM_LEFT_PARENTHESIS", KasmLanguage)
    val RIGHT_PARENTHESIS = IElementType("KASM_RIGHT_PARENTHESIS", KasmLanguage)
    val OPERATOR = IElementType("KASM_OPERATOR", KasmLanguage)
    val OTHER = IElementType("KASM_OTHER", KasmLanguage)
}
