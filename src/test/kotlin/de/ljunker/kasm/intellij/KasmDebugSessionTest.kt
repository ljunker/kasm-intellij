package de.ljunker.kasm.intellij

import de.ljunker.kasm.Assembler
import junit.framework.TestCase
import java.nio.file.Files
import kotlin.io.path.writeText

class KasmDebugSessionTest : TestCase() {
    fun testStepIntoCallStopsInsideCallee() {
        val fixture = debugFixture(
            """
            |    CALL sub
            |    HALT
            |sub:
            |    NOP
            |    RET
            """.trimMargin()
        )

        val stop = fixture.session.stepInto()

        assertTrue(stop is KasmDebugStop.Stepped)
        assertEquals(4, stop.snapshot.nextLocation?.lineNumber)
    }

    fun testStepOverCallStopsAtReturnAddress() {
        val fixture = debugFixture(
            """
            |    CALL sub
            |    HALT
            |sub:
            |    NOP
            |    RET
            """.trimMargin()
        )

        val stop = fixture.session.stepOver()

        assertTrue(stop is KasmDebugStop.Stepped)
        assertEquals(2, stop.snapshot.nextLocation?.lineNumber)
    }

    fun testBreakpointsCanStopInIncludedFiles() {
        val baseDirectory = Files.createTempDirectory("kasm-debug-session")
        val mainFile = baseDirectory.resolve("main.kasm")
        val includeFile = baseDirectory.resolve("lib.kasm")
        includeFile.writeText(
            """
            |target:
            |    NOP
            |    HALT
            """.trimMargin()
        )
        val source = """
            |    JMP target
            |    HALT
            |    .include "lib.kasm"
            """.trimMargin()
        mainFile.writeText(source)

        val debugProgram = Assembler().assembleFileWithDebugInfo(mainFile)
        val session = KasmDebugSession(debugProgram)

        assertNotNull(session.setBreakpoint(includeFile.toString(), 2))
        val stop = session.run()

        assertTrue(stop is KasmDebugStop.BreakpointHit)
        assertEquals(includeFile.toAbsolutePath().normalize(), stop.snapshot.nextLocation?.sourcePath)
        assertEquals(2, stop.snapshot.nextLocation?.lineNumber)
    }

    private fun debugFixture(source: String): DebugFixture {
        val baseDirectory = Files.createTempDirectory("kasm-debug-session")
        val mainFile = baseDirectory.resolve("main.kasm")
        mainFile.writeText(source)
        val debugProgram = Assembler().assembleFileWithDebugInfo(mainFile)

        return DebugFixture(
            session = KasmDebugSession(debugProgram)
        )
    }

    private data class DebugFixture(
        val session: KasmDebugSession
    )
}
