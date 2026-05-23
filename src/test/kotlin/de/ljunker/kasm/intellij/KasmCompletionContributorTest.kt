package de.ljunker.kasm.intellij

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class KasmCompletionContributorTest : BasePlatformTestCase() {
    fun testCompletesInstructionPrefixInKasmFile() {
        myFixture.configureByText(KasmFileType, "HA<caret>")

        myFixture.completeBasic()

        assertEquals("HALT", myFixture.editor.document.text)
    }

    fun testShowsInstructionsAndRegistersInKasmFile() {
        myFixture.configureByText(KasmFileType, "<caret>")

        val completions = myFixture.completeBasic()
            .map { it.lookupString }

        assertSame(KasmLanguage, myFixture.file.language)
        assertContainsElements(
            completions,
            "MOV",
            "MOVA",
            "ADDI",
            "JNZ",
            "JGE",
            "LOAD",
            "INCA",
            "NOP",
            "PRINTC",
            "RET",
            ".equ",
            ".byte",
            ".string",
            ".incbin",
            "R0",
            "A0"
        )
    }

    fun testCompletesDirectivePrefixInKasmFile() {
        myFixture.configureByText(KasmFileType, ".st<caret>")

        val completions = myFixture.completeBasic()
            .map { it.lookupString }

        assertContainsElements(completions, ".string")
    }

    fun testCompletesIncbinDirectivePrefixInKasmFile() {
        myFixture.configureByText(KasmFileType, ".in<caret>")

        val completions = myFixture.completeBasic()
            .map { it.lookupString }

        assertContainsElements(completions, ".incbin")
    }
}
