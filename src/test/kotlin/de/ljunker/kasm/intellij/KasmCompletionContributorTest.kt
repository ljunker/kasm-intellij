package de.ljunker.kasm.intellij

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class KasmCompletionContributorTest : BasePlatformTestCase() {
    fun testCompletesInstructionPrefixInKasmFile() {
        myFixture.configureByText(KasmFileType, "MO<caret>")

        myFixture.completeBasic()

        assertEquals("MOV", myFixture.editor.document.text)
    }

    fun testShowsInstructionsAndRegistersInKasmFile() {
        myFixture.configureByText(KasmFileType, "<caret>")

        val completions = myFixture.completeBasic()
            .map { it.lookupString }

        assertSame(KasmLanguage, myFixture.file.language)
        assertContainsElements(
            completions,
            "MOV",
            "JNZ",
            "LOAD",
            "RET",
            ".equ",
            ".byte",
            ".string",
            "R0"
        )
    }

    fun testCompletesDirectivePrefixInKasmFile() {
        myFixture.configureByText(KasmFileType, ".st<caret>")

        val completions = myFixture.completeBasic()
            .map { it.lookupString }

        assertContainsElements(completions, ".string")
    }
}
