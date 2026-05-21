package de.ljunker.kasm.intellij

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class KasmFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, KasmLanguage) {
    override fun getFileType(): FileType = KasmFileType

    override fun toString(): String = "KASM File"
}
