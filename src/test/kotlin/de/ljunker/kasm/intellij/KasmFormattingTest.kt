package de.ljunker.kasm.intellij

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class KasmFormattingTest : BasePlatformTestCase() {
    fun testKeepsLabelsAtFileIndentAndIndentsInstructions() {
        myFixture.configureByText(
            KasmFileType,
            """
            |MOV R0, 4
            |  loop:
            |DEC R0
            |     JNZ R0, loop
            |  LOAD R1, [40]
            |HALT
            """.trimMargin()
        )

        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformat(myFixture.file)
        }

        assertEquals(
            """
            |    MOV R0, 4
            |loop:
            |    DEC R0
            |    JNZ R0, loop
            |    LOAD R1, [40]
            |    HALT
            """.trimMargin(),
            myFixture.editor.document.text
        )
    }
}
