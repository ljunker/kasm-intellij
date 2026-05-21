package de.ljunker.kasm.intellij

import com.intellij.lang.Language

object KasmLanguage : Language("KASM") {
    private fun readResolve(): Any = KasmLanguage
}