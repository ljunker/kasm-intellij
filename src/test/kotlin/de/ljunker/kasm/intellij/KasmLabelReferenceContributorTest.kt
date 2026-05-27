package de.ljunker.kasm.intellij

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.nio.file.Files
import kotlin.io.path.writeText

class KasmLabelReferenceContributorTest : BasePlatformTestCase() {
    fun testIdentifierReferenceResolvesToLabelDefinition() {
        myFixture.configureByText(
            KasmFileType,
            """
            |target:
            |    HALT
            |
            |start:
            |    JMP tar<caret>get
            """.trimMargin()
        )

        val target = myFixture.file
            .findReferenceAt(myFixture.caretOffset - 1)
            ?.resolve()

        assertNotNull(target)
        assertEquals("target", target!!.text)
    }

    fun testIdentifierReferenceResolvesToIncludedLabelDefinition() {
        val baseDirectory = Files.createTempDirectory("kasm-label-reference")
        VfsRootAccess.allowRootAccess(
            testRootDisposable,
            baseDirectory.toString(),
            baseDirectory.toRealPath().toString()
        )
        val mainFile = baseDirectory.resolve("main.kasm")
        val includeFile = baseDirectory.resolve("lib.kasm")
        includeFile.writeText(
            """
            |target:
            |    HALT
            """.trimMargin()
        )
        val mainSource = """
            |    .include "lib.kasm"
            |    JMP target
            """.trimMargin()
        mainFile.writeText(mainSource)

        val virtualFile = LocalFileSystem.getInstance()
            .refreshAndFindFileByNioFile(mainFile)
        assertNotNull(virtualFile)
        myFixture.configureFromExistingVirtualFile(virtualFile!!)

        val target = myFixture.file
            .findReferenceAt(mainSource.indexOf("target") + 2)
            ?.resolve()

        assertNotNull(target)
        assertEquals("target", target!!.text)
        assertEquals("lib.kasm", target.containingFile.virtualFile.name)
    }

    fun testLabelDefinitionIsNotReference() {
        myFixture.configureByText(
            KasmFileType,
            """
            |tar<caret>get:
            |    HALT
            """.trimMargin()
        )

        val reference = myFixture.file.findReferenceAt(myFixture.caretOffset)

        assertNull(reference)
    }
}
