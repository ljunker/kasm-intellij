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
    ADDRESS_REGISTER("address-register"),
    BYTE_VALUE("byte-value"),
    ADDRESS_VALUE("address-value"),
    JUMP_TARGET("jump-target"),
    MEMORY_ADDRESS("memory-address"),
    INDEXED_MEMORY_ADDRESS("indexed-memory-address"),
    ADDRESS_REGISTER_MEMORY_ADDRESS("address-register-memory-address"),
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
        instruction("MOVA", KasmOperandType.ADDRESS_REGISTER, KasmOperandType.ADDRESS_VALUE) {
            "Copy a 16-bit address into an address register."
        },
        instruction("MOVA", KasmOperandType.ADDRESS_REGISTER, KasmOperandType.ADDRESS_REGISTER) {
            "Copy one address register into another."
        },
        instruction("ADD", KasmOperandType.REGISTER, KasmOperandType.REGISTER) {
            "Add into the target register with an 8-bit wrapped result."
        },
        instruction("ADC", KasmOperandType.REGISTER, KasmOperandType.REGISTER) {
            "Add source plus the current Carry flag into the target register."
        },
        instruction("ADDI", KasmOperandType.REGISTER, KasmOperandType.BYTE_VALUE) {
            "Add an immediate byte value into the target register."
        },
        instruction("SUB", KasmOperandType.REGISTER, KasmOperandType.REGISTER) {
            "Subtract into the target register with an 8-bit wrapped result."
        },
        instruction("SBC", KasmOperandType.REGISTER, KasmOperandType.REGISTER) {
            "Subtract source plus the current Carry flag from the target register."
        },
        instruction("SUBI", KasmOperandType.REGISTER, KasmOperandType.BYTE_VALUE) {
            "Subtract an immediate byte value from the target register."
        },
        instruction("INC", KasmOperandType.REGISTER) {
            "Increment one register with an 8-bit wrapped result."
        },
        instruction("DEC", KasmOperandType.REGISTER) {
            "Decrement one register with an 8-bit wrapped result."
        },
        instruction("MUL", KasmOperandType.REGISTER, KasmOperandType.REGISTER) {
            "Multiply into the target register with an 8-bit wrapped result."
        },
        instruction("DIV", KasmOperandType.REGISTER, KasmOperandType.REGISTER) {
            "Divide the target register by the source register."
        },
        instruction("MOD", KasmOperandType.REGISTER, KasmOperandType.REGISTER) {
            "Store the target modulo the source register."
        },
        instruction("NEG", KasmOperandType.REGISTER) {
            "Two's-complement negate one register."
        },
        instruction("AND", KasmOperandType.REGISTER, KasmOperandType.REGISTER) {
            "Bitwise AND into the target register."
        },
        instruction("OR", KasmOperandType.REGISTER, KasmOperandType.REGISTER) {
            "Bitwise OR into the target register."
        },
        instruction("XOR", KasmOperandType.REGISTER, KasmOperandType.REGISTER) {
            "Bitwise XOR into the target register."
        },
        instruction("NOT", KasmOperandType.REGISTER) {
            "Bitwise invert one register."
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
            "Signed jump when the last comparison was greater."
        },
        instruction("JGE", KasmOperandType.JUMP_TARGET) {
            "Signed jump when the last comparison was greater or equal."
        },
        instruction("JL", KasmOperandType.JUMP_TARGET) {
            "Signed jump when the last comparison was less."
        },
        instruction("JLE", KasmOperandType.JUMP_TARGET) {
            "Signed jump when the last comparison was less or equal."
        },
        instruction("LOAD", KasmOperandType.REGISTER, KasmOperandType.MEMORY_ADDRESS) {
            "Load one data-memory cell into a register."
        },
        instruction("LOAD", KasmOperandType.REGISTER, KasmOperandType.INDEXED_MEMORY_ADDRESS) {
            "Load one indexed data-memory cell into a register."
        },
        instruction("LOAD", KasmOperandType.REGISTER, KasmOperandType.ADDRESS_REGISTER_MEMORY_ADDRESS) {
            "Load through an address-register pointer into a register."
        },
        instruction("STORE", KasmOperandType.MEMORY_ADDRESS, KasmOperandType.REGISTER) {
            "Store a register value into one data-memory cell."
        },
        instruction("STORE", KasmOperandType.INDEXED_MEMORY_ADDRESS, KasmOperandType.REGISTER) {
            "Store a register value into one indexed data-memory cell."
        },
        instruction("STORE", KasmOperandType.ADDRESS_REGISTER_MEMORY_ADDRESS, KasmOperandType.REGISTER) {
            "Store a register value through an address-register pointer."
        },
        instruction("INCA", KasmOperandType.ADDRESS_REGISTER) {
            "Increment one address register with 16-bit wrapping."
        },
        instruction("DECA", KasmOperandType.ADDRESS_REGISTER) {
            "Decrement one address register with 16-bit wrapping."
        },
        instruction("PUSH", KasmOperandType.REGISTER) {
            "Push a register value on the stack."
        },
        instruction("PUSHI", KasmOperandType.BYTE_VALUE) {
            "Push an immediate byte value on the stack."
        },
        instruction("PUSHA", KasmOperandType.ADDRESS_VALUE) {
            "Push a 16-bit address value as two stack bytes."
        },
        instruction("PUSHA", KasmOperandType.ADDRESS_REGISTER) {
            "Push a 16-bit address register value as two stack bytes."
        },
        instruction("POP", KasmOperandType.REGISTER) {
            "Pop the stack top into a register."
        },
        instruction("DROP", KasmOperandType.BYTE_VALUE) {
            "Pop and discard a fixed number of stack bytes."
        },
        instruction("DROP", KasmOperandType.REGISTER) {
            "Pop and discard the register value's number of stack bytes."
        },
        instruction("PEEK", KasmOperandType.REGISTER, KasmOperandType.BYTE_VALUE) {
            "Copy a stack byte at an offset from SP into a register."
        },
        instruction("PEEK", KasmOperandType.REGISTER, KasmOperandType.REGISTER) {
            "Copy a stack byte at a register offset from SP into a register."
        },
        instruction("PEEKA", KasmOperandType.ADDRESS_REGISTER, KasmOperandType.BYTE_VALUE) {
            "Copy two stack bytes at an offset from SP into an address register."
        },
        instruction("PEEKA", KasmOperandType.ADDRESS_REGISTER, KasmOperandType.REGISTER) {
            "Copy two stack bytes at a register offset from SP into an address register."
        },
        instruction("PUSHF") {
            "Push Zero, Sign, Carry, and Overflow as one flag byte."
        },
        instruction("POPF") {
            "Pop one flag byte and restore Zero, Sign, Carry, and Overflow."
        },
        instruction("CALL", KasmOperandType.JUMP_TARGET) {
            "Push the return address and jump to a function."
        },
        instruction("RET") {
            "Pop a return address and jump back to it."
        },
        instruction("CLR", KasmOperandType.REGISTER) {
            "Clear one register to zero."
        },
        instruction("NOP") {
            "Do nothing."
        },
        instruction("PRINT", KasmOperandType.REGISTER) {
            "Print the register value as one output line."
        },
        instruction("PRINTC", KasmOperandType.REGISTER) {
            "Print the register value as one ASCII character."
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
        directive(".num64", KasmOperandType.EXPRESSION) {
            "Initialize eight little-endian cells from an unsigned 64-bit expression."
        },
        directive(".ascii", KasmOperandType.STRING) {
            "Initialize ASCII bytes without a terminator."
        },
        directive(".string", KasmOperandType.STRING) {
            "Initialize ASCII bytes followed by one zero byte."
        },
        directive(".incbin", KasmOperandType.STRING) {
            "Initialize one data-memory cell per byte read from a binary file."
        }
    )

    val byteRegisters = listOf("R0", "R1", "R2", "R3")

    val addressRegisters = listOf("A0", "A1")

    val registers = byteRegisters + addressRegisters

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
