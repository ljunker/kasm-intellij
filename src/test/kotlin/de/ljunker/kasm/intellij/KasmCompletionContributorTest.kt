package de.ljunker.kasm.intellij

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class KasmCompletionContributorTest : BasePlatformTestCase() {
    fun testCompletesInstructionPrefixInKasmFile() {
        myFixture.configureByText(KasmFileType, "HA<caret>")

        assertContainsElements(completionResults(), "HALT")
    }

    fun testShowsInstructionsAndRegistersInKasmFile() {
        myFixture.configureByText(KasmFileType, "<caret>")

        val completions = completionResults()

        assertSame(KasmLanguage, myFixture.file.language)
        assertContainsElements(
            completions,
            "MOV",
            "MOVA",
            "ADC",
            "ADDI",
            "JNZ",
            "JGE",
            "LOAD",
            "INCA",
            "NOP",
            "PUSHF",
            "PEEKA",
            "PRINTC",
            "RET",
            ".equ",
            ".byte",
            ".num64",
            ".string",
            ".incbin",
            ".include",
            "R0",
            "A0"
        )
    }

    fun testCompletesDirectivePrefixInKasmFile() {
        myFixture.configureByText(KasmFileType, ".st<caret>")

        val completions = completionResults()

        assertContainsElements(completions, ".string")
    }

    fun testCompletesIncbinDirectivePrefixInKasmFile() {
        myFixture.configureByText(KasmFileType, ".in<caret>")

        val completions = completionResults()

        assertContainsElements(completions, ".incbin", ".include")
    }

    fun testCompletesNum64DirectivePrefixInKasmFile() {
        myFixture.configureByText(KasmFileType, ".nu<caret>")

        val completions = completionResults()

        assertContainsElements(completions, ".num64")
    }

    private fun completionResults(): List<String> =
        myFixture.completeBasic()
            ?.map { it.lookupString }
            ?: listOf(myFixture.editor.document.text)
}
