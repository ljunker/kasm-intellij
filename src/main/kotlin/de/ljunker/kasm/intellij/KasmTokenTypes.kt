package de.ljunker.kasm.intellij

import com.intellij.psi.tree.IElementType

object KasmTokenTypes {
    val IDENTIFIER = IElementType("KASM_IDENTIFIER", KasmLanguage)
    val INSTRUCTION = IElementType("KASM_INSTRUCTION", KasmLanguage)
    val LABEL = IElementType("KASM_LABEL", KasmLanguage)
    val REGISTER = IElementType("KASM_REGISTER", KasmLanguage)
    val NUMBER = IElementType("KASM_NUMBER", KasmLanguage)
    val COMMENT = IElementType("KASM_COMMENT", KasmLanguage)
    val COMMA = IElementType("KASM_COMMA", KasmLanguage)
    val COLON = IElementType("KASM_COLON", KasmLanguage)
    val LEFT_BRACKET = IElementType("KASM_LEFT_BRACKET", KasmLanguage)
    val RIGHT_BRACKET = IElementType("KASM_RIGHT_BRACKET", KasmLanguage)
    val OTHER = IElementType("KASM_OTHER", KasmLanguage)
}
