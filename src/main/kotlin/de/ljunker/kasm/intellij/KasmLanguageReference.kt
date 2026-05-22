package de.ljunker.kasm.intellij

data class KasmStatementForm(
    val name: String,
    val operands: List<KasmOperandType>,
    val effect: String,
    val variadic: Boolean = false
) {
    val operandSummary: String =
        if (operands.isEmpty()) {
            "none"
        } else {
            buildString {
                append(operands.joinToString(", ") { it.displayName })
                if (variadic) {
                    append(", ...")
                }
            }
        }

    val signature: String =
        if (operands.isEmpty()) {
            name
        } else {
            "$name ${operandSummary}"
        }

    fun operandRange(index: Int): IntRange? {
        val operandIndex = when {
            index in operands.indices -> index
            variadic && operands.isNotEmpty() -> operands.lastIndex
            else -> return null
        }
        if (operandIndex !in operands.indices) {
            return null
        }

        var start = name.length + 1
        operands.take(operandIndex).forEach { operand ->
            start += operand.displayName.length + OPERAND_SEPARATOR.length
        }
        return start until start + operands[operandIndex].displayName.length
    }

    companion object {
        private const val OPERAND_SEPARATOR = ", "
    }
}

typealias KasmInstructionForm = KasmStatementForm

enum class KasmOperandType(val displayName: String) {
    REGISTER("register"),
    BYTE_VALUE("byte-value"),
    JUMP_TARGET("jump-target"),
    MEMORY_ADDRESS("memory-address"),
    SYMBOL("symbol"),
    EXPRESSION("expr"),
    STRING("string")
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
            "Add into the target register with an 8-bit wrapped result."
        },
        instruction("SUB", KasmOperandType.REGISTER, KasmOperandType.REGISTER) {
            "Subtract into the target register with an 8-bit wrapped result."
        },
        instruction("INC", KasmOperandType.REGISTER) {
            "Increment one register with an 8-bit wrapped result."
        },
        instruction("DEC", KasmOperandType.REGISTER) {
            "Decrement one register with an 8-bit wrapped result."
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
            "Signed jump when the last flagged result was greater than zero."
        },
        instruction("JL", KasmOperandType.JUMP_TARGET) {
            "Signed jump when the last flagged result was less than zero."
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

    val directiveForms = listOf(
        directive(".equ", KasmOperandType.SYMBOL, KasmOperandType.EXPRESSION) {
            "Define a symbol whose value is computed from an expression."
        },
        directive(".org", KasmOperandType.EXPRESSION) {
            "Move the data-memory layout cursor."
        },
        directive(".byte", KasmOperandType.EXPRESSION, variadic = true) {
            "Initialize one data-memory cell per byte expression."
        },
        directive(".ascii", KasmOperandType.STRING) {
            "Initialize ASCII bytes without a terminator."
        },
        directive(".string", KasmOperandType.STRING) {
            "Initialize ASCII bytes followed by one zero byte."
        }
    )

    val registers = listOf("R0", "R1", "R2", "R3")

    val instructionNames: List<String> = instructionForms
        .map { it.name }
        .distinct()

    val directiveNames: List<String> = directiveForms
        .map { it.name }
        .distinct()

    private val instructionFormsByName = instructionForms.groupBy { it.name.uppercase() }
    private val directiveFormsByName = directiveForms.groupBy { it.name.uppercase() }
    private val statementFormsByName = (instructionForms + directiveForms)
        .groupBy { it.name.uppercase() }

    fun formsFor(mnemonic: String): List<KasmInstructionForm> =
        instructionFormsByName[mnemonic.uppercase()].orEmpty()

    fun directiveFormsFor(name: String): List<KasmStatementForm> =
        directiveFormsByName[name.uppercase()].orEmpty()

    fun statementFormsFor(name: String): List<KasmStatementForm> =
        statementFormsByName[name.uppercase()].orEmpty()

    fun isInstruction(value: String): Boolean =
        value.uppercase() in instructionFormsByName

    fun isDirective(value: String): Boolean =
        value.uppercase() in directiveFormsByName

    fun isRegister(value: String): Boolean =
        registers.any { it.equals(value, ignoreCase = true) }

    private fun instruction(
        mnemonic: String,
        vararg operands: KasmOperandType,
        effect: () -> String
    ): KasmInstructionForm =
        KasmInstructionForm(mnemonic, operands.toList(), effect())

    private fun directive(
        name: String,
        vararg operands: KasmOperandType,
        variadic: Boolean = false,
        effect: () -> String
    ): KasmStatementForm =
        KasmStatementForm(name, operands.toList(), effect(), variadic)
}
