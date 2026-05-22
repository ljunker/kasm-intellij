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

    fun testFindsDirectiveOperandsAndIgnoresStringCommas() {
        val byteSource = "data: .byte 1, buffer + 1"
        val byteCall = KasmInstructionCallFinder.find(byteSource, byteSource.length)
        val stringSource = ".ascii \"a,b\""
        val stringCall = KasmInstructionCallFinder.find(stringSource, stringSource.length)

        assertNotNull(byteCall)
        assertEquals(".byte", byteCall!!.mnemonic)
        assertEquals(1, byteCall.operandIndex)
        assertNotNull(stringCall)
        assertEquals(".ascii", stringCall!!.mnemonic)
        assertEquals(0, stringCall.operandIndex)
    }

    fun testReferenceContainsDataDirectiveForms() {
        val signatures = KasmLanguageReference.directiveFormsFor(".byte")
            .map { it.signature }

        assertSameElements(signatures, ".byte expr, ...")
    }
}
