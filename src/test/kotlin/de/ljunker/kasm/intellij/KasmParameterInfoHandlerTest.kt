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
            KasmLanguageReference.formsFor("adc").single().signature,
            KasmLanguageReference.formsFor("sbc").single().signature,
            KasmLanguageReference.formsFor("mul").single().signature,
            KasmLanguageReference.formsFor("neg").single().signature,
            KasmLanguageReference.formsFor("jge").single().signature,
            KasmLanguageReference.formsFor("nop").single().signature,
            KasmLanguageReference.formsFor("inca").single().signature,
            KasmLanguageReference.formsFor("pushi").single().signature,
            KasmLanguageReference.formsFor("pushf").single().signature,
            KasmLanguageReference.formsFor("popf").single().signature,
            KasmLanguageReference.formsFor("printc").single().signature
        )
        val pushaSignatures = KasmLanguageReference.formsFor("pusha")
            .map { it.signature }
        val dropSignatures = KasmLanguageReference.formsFor("drop")
            .map { it.signature }
        val peekSignatures = KasmLanguageReference.formsFor("peek")
            .map { it.signature }
        val peekaSignatures = KasmLanguageReference.formsFor("peeka")
            .map { it.signature }

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
            "ADC register, register",
            "SBC register, register",
            "MUL register, register",
            "NEG register",
            "JGE jump-target",
            "NOP",
            "INCA address-register",
            "PUSHI byte-value",
            "PUSHF",
            "POPF",
            "PRINTC register"
        )
        assertSameElements(
            pushaSignatures,
            "PUSHA address-value",
            "PUSHA address-register"
        )
        assertSameElements(
            dropSignatures,
            "DROP byte-value",
            "DROP register"
        )
        assertSameElements(
            peekSignatures,
            "PEEK register, byte-value",
            "PEEK register, register"
        )
        assertSameElements(
            peekaSignatures,
            "PEEKA address-register, byte-value",
            "PEEKA address-register, register"
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
        val num64Signatures = KasmLanguageReference.directiveFormsFor(".num64")
            .map { it.signature }
        val incbinSignatures = KasmLanguageReference.directiveFormsFor(".incbin")
            .map { it.signature }
        val includeSignatures = KasmLanguageReference.directiveFormsFor(".include")
            .map { it.signature }

        assertSameElements(byteSignatures, ".byte expr, ...")
        assertSameElements(num64Signatures, ".num64 expr")
        assertSameElements(incbinSignatures, ".incbin string")
        assertSameElements(includeSignatures, ".include string")
    }
}
