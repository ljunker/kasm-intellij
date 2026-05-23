package de.ljunker.kasm.intellij

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class KasmParameterInfoHandlerTest : BasePlatformTestCase() {
    fun testFindsCurrentOperandAfterLeadingLabel() {
        val source = "loop: LOAD R1, [buffer + R2]"

        val call = KasmInstructionCallFinder.find(source, source.length)

        assertNotNull(call)
        assertEquals("LOAD", call!!.mnemonic)
        assertEquals(1, call.operandIndex)
        assertEquals(source.indexOf("LOAD"), call.mnemonicStart)
    }

    fun testReferenceContainsMovFormsFromLanguageReference() {
        val signatures = KasmLanguageReference.formsFor("mov")
            .map { it.signature }
        val addressSignatures = KasmLanguageReference.formsFor("mova")
            .map { it.signature }

        assertSameElements(
            signatures,
            "MOV register, byte-value",
            "MOV register, register"
        )
        assertSameElements(
            addressSignatures,
            "MOVA address-register, address-value",
            "MOVA address-register, address-register"
        )
    }

    fun testReferenceContainsUpdatedInstructionForms() {
        val loadSignatures = KasmLanguageReference.formsFor("load")
            .map { it.signature }
        val storeSignatures = KasmLanguageReference.formsFor("store")
            .map { it.signature }
        val instructionSignatures = listOf(
            KasmLanguageReference.formsFor("addi").single().signature,
            KasmLanguageReference.formsFor("mul").single().signature,
            KasmLanguageReference.formsFor("neg").single().signature,
            KasmLanguageReference.formsFor("jge").single().signature,
            KasmLanguageReference.formsFor("nop").single().signature,
            KasmLanguageReference.formsFor("inca").single().signature,
            KasmLanguageReference.formsFor("printc").single().signature
        )

        assertSameElements(
            loadSignatures,
            "LOAD register, memory-address",
            "LOAD register, indexed-memory-address",
            "LOAD register, address-register-memory-address"
        )
        assertSameElements(
            storeSignatures,
            "STORE memory-address, register",
            "STORE indexed-memory-address, register",
            "STORE address-register-memory-address, register"
        )
        assertSameElements(
            instructionSignatures,
            "ADDI register, byte-value",
            "MUL register, register",
            "NEG register",
            "JGE jump-target",
            "NOP",
            "INCA address-register",
            "PRINTC register"
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
        val byteSignatures = KasmLanguageReference.directiveFormsFor(".byte")
            .map { it.signature }
        val incbinSignatures = KasmLanguageReference.directiveFormsFor(".incbin")
            .map { it.signature }

        assertSameElements(byteSignatures, ".byte expr, ...")
        assertSameElements(incbinSignatures, ".incbin string")
    }
}
