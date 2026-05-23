package de.ljunker.kasm.intellij

import junit.framework.TestCase
import java.nio.file.Path

class KasmDebugRunnerTest : TestCase() {
    fun testUsesProgramParentAsAssemblerBaseDirectory() {
        val baseDirectory = Path.of("tmp", "kasm", "examples")
            .toAbsolutePath()
            .normalize()
        val programPath = baseDirectory.resolve("aoc.kasm")

        assertEquals(
            baseDirectory,
            kasmBaseDirectoryForProgram(programPath.toString())
        )
    }
}
