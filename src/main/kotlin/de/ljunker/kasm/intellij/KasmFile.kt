package de.ljunker.kasm.intellij

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiReference

class KasmFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, KasmLanguage) {
    override fun getFileType(): FileType = KasmFileType

    override fun findReferenceAt(offset: Int): PsiReference? =
        findElementAt(offset)?.let(::kasmLabelReference)
            ?: super.findReferenceAt(offset)

    override fun toString(): String = "KASM File"
}
