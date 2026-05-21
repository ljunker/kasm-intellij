package de.ljunker.kasm.intellij

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor

class KasmPostFormatProcessor : PostFormatProcessor {
    override fun processElement(source: PsiElement, settings: CodeStyleSettings): PsiElement {
        val file = source.containingFile ?: source as? PsiFile ?: return source
        if (file is KasmFile) {
            reindent(file, source.textRange, settings)
        }
        return source
    }

    override fun processText(
        source: PsiFile,
        rangeToReformat: TextRange,
        settings: CodeStyleSettings
    ): TextRange {
        if (source !is KasmFile) {
            return rangeToReformat
        }
        return reindent(source, rangeToReformat, settings)
    }

    override fun isWhitespaceOnly(): Boolean = true

    private fun reindent(
        file: KasmFile,
        range: TextRange,
        settings: CodeStyleSettings
    ): TextRange {
        val document = PsiDocumentManager.getInstance(file.project).getDocument(file)
            ?: return range
        if (document.textLength == 0) {
            return range
        }

        val boundedStart = range.startOffset.coerceIn(0, document.textLength)
        val boundedEnd = range.endOffset.coerceIn(boundedStart, document.textLength)
        val lastOffset = if (boundedEnd > boundedStart) boundedEnd - 1 else boundedEnd
        val firstLine = document.getLineNumber(boundedStart)
        val lastLine = document.getLineNumber(lastOffset)
        val instructionIndent = instructionIndent(settings)
        var totalDelta = 0

        for (line in lastLine downTo firstLine) {
            totalDelta += reindentLine(document, line, instructionIndent)
        }

        return TextRange(
            boundedStart,
            (boundedEnd + totalDelta).coerceIn(boundedStart, document.textLength)
        )
    }

    private fun reindentLine(
        document: Document,
        line: Int,
        instructionIndent: String
    ): Int {
        val lineStart = document.getLineStartOffset(line)
        val lineEnd = document.getLineEndOffset(line)
        val lineText = document.charsSequence.subSequence(lineStart, lineEnd).toString()
        val contentStart = lineText.indexOfFirst { !it.isIndentWhitespace() }
        if (contentStart < 0) {
            return 0
        }

        val content = lineText.substring(contentStart)
        val indent = if (isRootLine(content)) "" else instructionIndent
        val originalIndent = lineText.substring(0, contentStart)
        if (indent == originalIndent) {
            return 0
        }

        document.replaceString(lineStart, lineStart + contentStart, indent)
        return indent.length - contentStart
    }

    private fun instructionIndent(settings: CodeStyleSettings): String {
        val options = settings.getIndentOptions(KasmFileType)
        if (options.USE_TAB_CHARACTER) {
            return "\t"
        }
        return " ".repeat(options.INDENT_SIZE)
    }

    private fun isRootLine(content: String): Boolean =
        content.startsWith(';') || LABEL_LINE.matchesAt(content, 0)

    private fun Char.isIndentWhitespace(): Boolean = this == ' ' || this == '\t'

    companion object {
        private val LABEL_LINE = Regex("""[A-Za-z_][A-Za-z0-9_]*:""")
    }
}
