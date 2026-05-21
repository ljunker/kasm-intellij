package de.ljunker.kasm.intellij

data class KasmInstructionForm(
    val mnemonic: String,
    val operands: List<KasmOperandType>,
    val effect: String
) {
    val signature: String =
        if (operands.isEmpty()) {
            mnemonic
        } else {
            "$mnemonic ${operands.joinToString(", ") { it.displayName }}"
        }

    val operandSummary: String =
        if (operands.isEmpty()) {
            "none"
        } else {
            operands.joinToString(", ") { it.displayName }
        }

    fun operandRange(index: Int): IntRange? {
        if (index !in operands.indices) {
            return null
        }

        var start = mnemonic.length + 1
        operands.take(index).forEach { operand ->
            start += operand.displayName.length + OPERAND_SEPARATOR.length
        }
        return start until start + operands[index].displayName.length
    }

    companion object {
        private const val OPERAND_SEPARATOR = ", "
    }
}

enum class KasmOperandType(val displayName: String) {
    REGISTER("register"),
    BYTE_VALUE("byte-value"),
    JUMP_TARGET("jump-target"),
    MEMORY_ADDRESS("memory-address")
}

object KasmLanguageReference {
    // Mirrors ../Kasm/docs/language-reference.md so editor features share one source.
    val instructionForms = listOf(
        instruction("MOV", KasmOperandType.REGISTER, KasmOperandType.BYTE_VALUE) {
            "Copy a value into a register."
        },
        instruction("MOV", KasmOperandType.REGISTER, KasmOperandType.REGISTER) {
            "Copy one register into another register."
        },
        instruction("ADD", KasmOperandType.REGISTER, KasmOperandType.REGISTER) {
            "Add the source register to the target register."
        },
        instruction("SUB", KasmOperandType.REGISTER, KasmOperandType.REGISTER) {
            "Subtract the source register from the target register."
        },
        instruction("INC", KasmOperandType.REGISTER) {
            "Increment one register."
        },
        instruction("DEC", KasmOperandType.REGISTER) {
            "Decrement one register."
        },
        instruction("CMP", KasmOperandType.REGISTER, KasmOperandType.REGISTER) {
            "Compare two registers by updating result flags."
        },
        instruction("JMP", KasmOperandType.JUMP_TARGET) {
            "Jump unconditionally."
        },
        instruction("JZ", KasmOperandType.REGISTER, KasmOperandType.JUMP_TARGET) {
            "Jump when the register value is zero."
        },
        instruction("JNZ", KasmOperandType.REGISTER, KasmOperandType.JUMP_TARGET) {
            "Jump when the register value is non-zero."
        },
        instruction("JE", KasmOperandType.JUMP_TARGET) {
            "Jump when the Zero flag is set."
        },
        instruction("JNE", KasmOperandType.JUMP_TARGET) {
            "Jump when the Zero flag is clear."
        },
        instruction("JG", KasmOperandType.JUMP_TARGET) {
            "Jump when the last flagged result was greater than zero."
        },
        instruction("JL", KasmOperandType.JUMP_TARGET) {
            "Jump when the last flagged result was less than zero."
        },
        instruction("LOAD", KasmOperandType.REGISTER, KasmOperandType.MEMORY_ADDRESS) {
            "Load one data-memory cell into a register."
        },
        instruction("STORE", KasmOperandType.MEMORY_ADDRESS, KasmOperandType.REGISTER) {
            "Store a register value into one data-memory cell."
        },
        instruction("PUSH", KasmOperandType.REGISTER) {
            "Push a register value on the stack."
        },
        instruction("POP", KasmOperandType.REGISTER) {
            "Pop the stack top into a register."
        },
        instruction("CALL", KasmOperandType.JUMP_TARGET) {
            "Push the return address and jump to a function."
        },
        instruction("RET") {
            "Pop a return address and jump back to it."
        },
        instruction("PRINT", KasmOperandType.REGISTER) {
            "Print the register value as one output line."
        },
        instruction("HALT") {
            "Stop the VM."
        }
    )

    val registers = listOf("R0", "R1", "R2", "R3")

    val instructionNames: List<String> = instructionForms
        .map { it.mnemonic }
        .distinct()

    private val formsByMnemonic = instructionForms.groupBy { it.mnemonic }

    fun formsFor(mnemonic: String): List<KasmInstructionForm> =
        formsByMnemonic[mnemonic.uppercase()].orEmpty()

    fun isInstruction(value: String): Boolean =
        value.uppercase() in formsByMnemonic

    fun isRegister(value: String): Boolean =
        registers.any { it.equals(value, ignoreCase = true) }

    private fun instruction(
        mnemonic: String,
        vararg operands: KasmOperandType,
        effect: () -> String
    ): KasmInstructionForm =
        KasmInstructionForm(mnemonic, operands.toList(), effect())
}
