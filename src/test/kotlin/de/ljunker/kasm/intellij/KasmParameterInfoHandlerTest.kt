package de.ljunker.kasm.intellij

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class KasmParameterInfoHandlerTest : BasePlatformTestCase() {
    fun testFindsCurrentOperandAfterLeadingLabel() {
        val source = "loop: LOAD R1, [40]"

        val call = KasmInstructionCallFinder.find(source, source.length)

        assertNotNull(call)
        assertEquals("LOAD", call!!.mnemonic)
        assertEquals(1, call.operandIndex)
        assertEquals(source.indexOf("LOAD"), call.mnemonicStart)
    }

    fun testReferenceContainsMovFormsFromLanguageReference() {
        val signatures = KasmLanguageReference.formsFor("mov")
            .map { it.signature }

        assertSameElements(
            signatures,
            "MOV register, byte-value",
            "MOV register, register"
        )
    }
}
