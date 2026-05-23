package de.ljunker.kasm.intellij

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.breakpoints.XBreakpointHandler
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import com.intellij.xdebugger.frame.*
import com.intellij.xdebugger.impl.XSourcePositionImpl
import de.ljunker.kasm.*

class KasmDebugProcess(
    session: XDebugSession,
    private val sourceFile: VirtualFile,
    debugProgram: DebugProgram
) : XDebugProcess(session) {
    private val kasmSession = DebugSession(
        debugProgram = debugProgram,
        output = ::printProgramOutput
    )
    private val breakpointHandler = KasmBreakpointHandler()
    private val operationLock = Any()

    @Volatile
    private var stopped = false

    override fun getEditorsProvider(): XDebuggerEditorsProvider =
        KasmDebuggerEditorsProvider

    override fun getBreakpointHandlers(): Array<XBreakpointHandler<*>> =
        arrayOf(breakpointHandler)

    override fun sessionInitialized() {
        session.setPauseActionSupported(false)
        suspendAt(kasmSession.snapshot())
    }

    override fun resume(context: XSuspendContext?) {
        execute {
            kasmSession.run()
        }
    }

    override fun startStepOver(context: XSuspendContext?) {
        step()
    }

    override fun startStepInto(context: XSuspendContext?) {
        step()
    }

    override fun startStepOut(context: XSuspendContext?) {
        step()
    }

    override fun stop() {
        stopped = true
    }

    private fun step() {
        execute {
            kasmSession.step()
        }
    }

    private fun execute(operation: () -> DebugStop) {
        if (stopped) {
            return
        }

        session.sessionResumed()

        ApplicationManager.getApplication().executeOnPooledThread {
            val stop = synchronized(operationLock) {
                if (stopped) {
                    null
                } else {
                    operation()
                }
            }

            if (stop != null && !stopped) {
                handleStop(stop)
            }
        }
    }

    private fun handleStop(stop: DebugStop) {
        when (stop) {
            is DebugStop.BreakpointHit ->
                suspendAt(stop.snapshot)

            is DebugStop.Stepped ->
                suspendAt(stop.snapshot)

            is DebugStop.Halted -> {
                printSystemOutput("Program halted.")
                session.stop()
            }

            is DebugStop.VmError -> {
                session.reportMessage(
                    "KASM VM error: ${stop.error.message}",
                    MessageType.ERROR
                )
                suspendAt(stop.snapshot)
            }
        }
    }

    private fun suspendAt(snapshot: DebugSnapshot) {
        session.positionReached(
            KasmSuspendContext(
                frame = KasmStackFrame(
                    snapshot = snapshot,
                    sourceFile = sourceFile
                )
            )
        )
    }

    private fun printProgramOutput(line: String) {
        session.consoleView.print(
            "$line\n",
            ConsoleViewContentType.NORMAL_OUTPUT
        )
    }

    private fun printSystemOutput(line: String) {
        session.consoleView.print(
            "$line\n",
            ConsoleViewContentType.SYSTEM_OUTPUT
        )
    }

    private inner class KasmBreakpointHandler :
        XBreakpointHandler<XLineBreakpoint<KasmBreakpointProperties>>(
            KasmLineBreakpointType::class.java
        ) {
        override fun registerBreakpoint(
            breakpoint: XLineBreakpoint<KasmBreakpointProperties>
        ) {
            val sourcePosition = breakpoint.sourcePosition

            if (sourcePosition == null || sourcePosition.file.path != sourceFile.path) {
                return
            }

            val breakpointLine = breakpoint.line + 1
            val kasmBreakpoint = kasmSession.setBreakpoint(breakpointLine)

            if (kasmBreakpoint == null) {
                session.setBreakpointInvalid(
                    breakpoint,
                    "Line $breakpointLine has no executable KASM instruction"
                )
            } else {
                session.setBreakpointVerified(breakpoint)
            }
        }

        override fun unregisterBreakpoint(
            breakpoint: XLineBreakpoint<KasmBreakpointProperties>,
            temporary: Boolean
        ) {
            val sourcePosition = breakpoint.sourcePosition

            if (sourcePosition == null || sourcePosition.file.path != sourceFile.path) {
                return
            }

            kasmSession.removeBreakpoint(breakpoint.line + 1)
        }
    }
}

private object KasmDebuggerEditorsProvider : XDebuggerEditorsProvider() {
    override fun getFileType(): FileType = KasmFileType
}

private class KasmSuspendContext(
    frame: KasmStackFrame
) : XSuspendContext() {
    private val executionStack = KasmExecutionStack(frame)

    override fun getActiveExecutionStack(): XExecutionStack = executionStack
}

private class KasmExecutionStack(
    private val frame: KasmStackFrame
) : XExecutionStack("KASM VM") {
    override fun getTopFrame(): XStackFrame = frame

    override fun computeStackFrames(
        firstFrameIndex: Int,
        container: XStackFrameContainer
    ) {
        val frames = if (firstFrameIndex == 0) listOf(frame) else emptyList()
        container.addStackFrames(frames, true)
    }
}

private class KasmStackFrame(
    private val snapshot: DebugSnapshot,
    private val sourceFile: VirtualFile
) : XStackFrame() {
    override fun getSourcePosition(): XSourcePosition? {
        val location = snapshot.nextLocation ?: return null
        return XSourcePositionImpl.create(sourceFile, location.lineNumber - 1)
    }

    override fun computeChildren(node: XCompositeNode) {
        val vm = snapshot.vm
        val children = XValueChildrenList()

        children.add("IP", KasmValue("address", vm.instructionPointer))
        children.add("SP", KasmValue("address", vm.stackPointer))
        children.add("running", KasmValue("boolean", vm.running))

        vm.registers.forEachIndexed { register, value ->
            children.add("R$register", KasmValue("word", value))
        }

        vm.addressRegisters.forEachIndexed { register, value ->
            children.add("A$register", KasmValue("address", value))
        }

        children.add("Z", KasmValue("flag", vm.zeroFlag))
        children.add("S", KasmValue("flag", vm.signFlag))
        children.add("C", KasmValue("flag", vm.carryFlag))
        children.add("O", KasmValue("flag", vm.overflowFlag))

        addStackValues(children, vm)
        addMemoryValues(children, vm)

        node.addChildren(children, true)
    }

    private fun addStackValues(children: XValueChildrenList, vm: VmSnapshot) {
        (vm.stackPointer until vm.memory.size).forEach { address ->
            children.add(
                "stack[$address]",
                KasmValue("word", vm.memory[address])
            )
        }
    }

    private fun addMemoryValues(children: XValueChildrenList, vm: VmSnapshot) {
        vm.memory.indices
            .filter { address ->
                address < vm.stackPointer && vm.memory[address] != 0
            }
            .forEach { address ->
                children.add(
                    "memory[$address]",
                    KasmValue("word", vm.memory[address])
                )
            }
    }
}

private class KasmValue(
    private val type: String,
    private val value: Any
) : XValue() {
    override fun computePresentation(node: XValueNode, place: XValuePlace) {
        node.setPresentation(
            null,
            type,
            value.toString(),
            false
        )
    }
}
