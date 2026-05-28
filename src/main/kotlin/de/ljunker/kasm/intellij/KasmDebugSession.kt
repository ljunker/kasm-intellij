package de.ljunker.kasm.intellij

import de.ljunker.kasm.DebugProgram
import de.ljunker.kasm.DebugSnapshot
import de.ljunker.kasm.Opcode
import de.ljunker.kasm.VirtualMachine
import de.ljunker.kasm.VmException
import java.nio.file.Path

internal class KasmDebugSession(
    private val debugProgram: DebugProgram,
    output: (String) -> Unit = {}
) {
    private val vm = VirtualMachine(output)
    private val breakpointsByAddress = mutableMapOf<Int, KasmSourceBreakpoint>()
    private var stoppedAtBreakpoint = false

    init {
        vm.load(debugProgram.program)
    }

    fun setBreakpoint(filePath: String, lineNumber: Int): KasmSourceBreakpoint? {
        val sourcePath = Path.of(filePath)
        val address = debugProgram.sourceMap.addressForLocation(sourcePath, lineNumber)
            ?: return null
        val breakpoint = KasmSourceBreakpoint(
            sourcePath = sourcePath.toAbsolutePath().normalize(),
            lineNumber = lineNumber,
            address = address
        )

        breakpointsByAddress[address] = breakpoint
        return breakpoint
    }

    fun removeBreakpoint(filePath: String, lineNumber: Int): Boolean {
        val address = debugProgram.sourceMap.addressForLocation(Path.of(filePath), lineNumber)
            ?: return false

        return breakpointsByAddress.remove(address) != null
    }

    fun snapshot(): DebugSnapshot {
        val vmSnapshot = vm.snapshot()

        return DebugSnapshot(
            vm = vmSnapshot,
            nextLocation = debugProgram.sourceMap.locationForAddress(vm.instructionPointer),
            symbols = debugProgram.symbols.snapshot(vmSnapshot.memory)
        )
    }

    fun run(): KasmDebugStop {
        if (!vm.isRunning) {
            return KasmDebugStop.Halted(snapshot())
        }

        if (stoppedAtBreakpoint) {
            stoppedAtBreakpoint = false

            stepVm()?.let { stop ->
                return stop
            }
        }

        while (vm.isRunning) {
            val breakpoint = breakpointsByAddress[vm.instructionPointer]
            if (breakpoint != null) {
                stoppedAtBreakpoint = true
                return KasmDebugStop.BreakpointHit(
                    breakpoint = breakpoint,
                    snapshot = snapshot()
                )
            }

            stepVm()?.let { stop ->
                return stop
            }
        }

        return KasmDebugStop.Halted(snapshot())
    }

    fun stepInto(): KasmDebugStop {
        if (!vm.isRunning) {
            return KasmDebugStop.Halted(snapshot())
        }

        stoppedAtBreakpoint = false

        stepVm()?.let { stop ->
            return stop
        }

        return if (vm.isRunning) {
            KasmDebugStop.Stepped(snapshot())
        } else {
            KasmDebugStop.Halted(snapshot())
        }
    }

    fun stepOver(): KasmDebugStop {
        if (!vm.isRunning) {
            return KasmDebugStop.Halted(snapshot())
        }

        stoppedAtBreakpoint = false
        val returnAddress = callReturnAddress(vm.instructionPointer)
            ?: return stepInto()

        stepVm()?.let { stop ->
            return stop
        }

        while (vm.isRunning) {
            if (vm.instructionPointer == returnAddress) {
                return KasmDebugStop.Stepped(snapshot())
            }

            val breakpoint = breakpointsByAddress[vm.instructionPointer]
            if (breakpoint != null) {
                stoppedAtBreakpoint = true
                return KasmDebugStop.BreakpointHit(
                    breakpoint = breakpoint,
                    snapshot = snapshot()
                )
            }

            stepVm()?.let { stop ->
                return stop
            }
        }

        return KasmDebugStop.Halted(snapshot())
    }

    private fun stepVm(): KasmDebugStop? =
        try {
            vm.step()

            if (vm.isRunning) {
                null
            } else {
                KasmDebugStop.Halted(snapshot())
            }
        } catch (error: VmException) {
            KasmDebugStop.VmError(
                error = error,
                snapshot = snapshot()
            )
        }

    private fun callReturnAddress(instructionPointer: Int): Int? {
        val opcode = debugProgram.program.bytes
            .getOrNull(instructionPointer)
            ?.let(Opcode::fromCode)

        if (opcode != Opcode.CALL) {
            return null
        }

        return instructionPointer + 1 + opcode.operandByteCount
    }
}

internal data class KasmSourceBreakpoint(
    val sourcePath: Path,
    val lineNumber: Int,
    val address: Int
)

internal sealed interface KasmDebugStop {
    val snapshot: DebugSnapshot

    data class BreakpointHit(
        val breakpoint: KasmSourceBreakpoint,
        override val snapshot: DebugSnapshot
    ) : KasmDebugStop

    data class Stepped(
        override val snapshot: DebugSnapshot
    ) : KasmDebugStop

    data class Halted(
        override val snapshot: DebugSnapshot
    ) : KasmDebugStop

    data class VmError(
        val error: VmException,
        override val snapshot: DebugSnapshot
    ) : KasmDebugStop
}
