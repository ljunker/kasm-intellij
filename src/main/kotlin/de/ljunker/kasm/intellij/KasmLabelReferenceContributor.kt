package de.ljunker.kasm.intellij

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.ProcessingContext
import com.intellij.openapi.util.TextRange
import java.nio.file.Path

class KasmLabelReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(KasmTokenTypes.IDENTIFIER).withLanguage(KasmLanguage),
            KasmLabelReferenceProvider
        )
    }
}

private object KasmLabelReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext
    ): Array<PsiReference> =
        kasmLabelReference(element)?.let { arrayOf(it) }
            ?: PsiReference.EMPTY_ARRAY
}

internal fun kasmLabelReference(element: PsiElement): PsiReference? {
    if (element.node?.elementType != KasmTokenTypes.IDENTIFIER) {
        return null
    }

    val labelName = element.text
    if (!LABEL_NAME.matches(labelName) || isEquDefinition(element)) {
        return null
    }

    return KasmLabelReference(element, labelName)
}

private fun isEquDefinition(element: PsiElement): Boolean {
    val fileText = element.containingFile.text
    val startOffset = element.textRange.startOffset
    val lineStart = fileText.lastIndexOf('\n', startOffset - 1)
        .let { if (it < 0) 0 else it + 1 }
    val prefix = fileText.substring(lineStart, startOffset)

    return EQU_DEFINITION_PREFIX.matches(prefix)
}

private val LABEL_NAME = Regex("""[A-Za-z_][A-Za-z0-9_]*""")

private val EQU_DEFINITION_PREFIX =
    Regex("""^\s*(?:[A-Za-z_][A-Za-z0-9_]*:\s*)*\.equ\s+""", RegexOption.IGNORE_CASE)

private class KasmLabelReference(
    element: PsiElement,
    private val labelName: String
) : PsiReferenceBase<PsiElement>(
    element,
    TextRange(0, element.textLength),
    false
) {
    override fun resolve(): PsiElement? {
        val file = element.containingFile ?: return null
        val virtualFile = file.virtualFile ?: return null
        val rootPath = Path.of(virtualFile.path)
        val label = KasmSourceModel.collectLabels(rootPath, file.text)
            .firstOrNull { it.name == labelName }
            ?: return null

        val targetFile = if (label.filePath == rootPath.toAbsolutePath().normalize().toString()) {
            file
        } else {
            val targetVirtualFile = LocalFileSystem.getInstance()
                .findFileByPath(label.filePath)
                ?: return null
            PsiManager.getInstance(element.project).findFile(targetVirtualFile)
                ?: return null
        }

        return targetFile.findElementAt(label.startOffset)
            ?: targetFile
    }

    override fun getVariants(): Array<Any> = emptyArray()
}
