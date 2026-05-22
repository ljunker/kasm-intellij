package de.ljunker.kasm.intellij

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class KasmLineBreakpointTypeTest : BasePlatformTestCase() {
    fun testAllowsBreakpointsOnlyInKasmFiles() {
        val breakpointType = KasmLineBreakpointType()
        val kasmFile = myFixture.configureByText(
            KasmFileType,
            "HALT"
        ).virtualFile
        val textFile = myFixture.configureByText(
            "notes.txt",
            "HALT"
        ).virtualFile

        assertTrue(breakpointType.canPutAt(kasmFile, 0, project))
        assertFalse(breakpointType.canPutAt(textFile, 0, project))
    }
}
