package de.ljunker.kasm.intellij

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange

class KasmFormattingModelBuilder : FormattingModelBuilder {
    override fun createModel(formattingContext: FormattingContext): FormattingModel =
        FormattingModelProvider.createFormattingModelForPsiFile(
            formattingContext.containingFile,
            KasmFileBlock(
                node = formattingContext.node,
                instructionIndentSize = formattingContext.codeStyleSettings
                    .getIndentOptions(KasmFileType)
                    .INDENT_SIZE
            ),
            formattingContext.codeStyleSettings
        )
}

private class KasmFileBlock(
    private val node: ASTNode,
    private val instructionIndentSize: Int
) : Block {
    override fun getTextRange(): TextRange = node.textRange

    override fun getSubBlocks(): List<Block> = buildLineBlocks()

    override fun getWrap(): Wrap? = null

    override fun getIndent(): Indent? = null

    override fun getAlignment() = null

    override fun getSpacing(child1: Block?, child2: Block): Spacing? = null

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes =
        ChildAttributes(Indent.getNormalIndent(), null)

    override fun isIncomplete(): Boolean = false

    override fun isLeaf(): Boolean = false

    private fun buildLineBlocks(): List<Block> {
        val source = node.text
        val baseOffset = node.textRange.startOffset
        val blocks = mutableListOf<Block>()
        var lineStart = 0

        while (lineStart < source.length) {
            var lineEnd = lineStart
            while (lineEnd < source.length && !source[lineEnd].isLineBreak()) {
                lineEnd++
            }

            val contentStart = firstContentOffset(source, lineStart, lineEnd)
            if (contentStart != null) {
                val line = source.substring(contentStart, lineEnd)
                blocks += KasmLineBlock(
                    textRange = TextRange(baseOffset + contentStart, baseOffset + lineEnd),
                    indent = indentFor(line)
                )
            }

            lineStart = nextLineStart(source, lineEnd)
        }

        return blocks
    }

    private fun firstContentOffset(source: String, start: Int, end: Int): Int? {
        for (offset in start until end) {
            if (!source[offset].isIndentWhitespace()) {
                return offset
            }
        }
        return null
    }

    private fun indentFor(line: String): Indent {
        if (line.startsWith(';') || LABEL_LINE.matchesAt(line, 0)) {
            return Indent.getAbsoluteNoneIndent()
        }

        return Indent.getSpaceIndent(instructionIndentSize)
    }

    private fun nextLineStart(source: String, lineEnd: Int): Int {
        var offset = lineEnd
        if (offset < source.length && source[offset] == '\r') {
            offset++
        }
        if (offset < source.length && source[offset] == '\n') {
            offset++
        }
        return offset
    }

    private fun Char.isIndentWhitespace(): Boolean = this == ' ' || this == '\t'

    private fun Char.isLineBreak(): Boolean = this == '\n' || this == '\r'

    companion object {
        private val LABEL_LINE = Regex("""[A-Za-z_][A-Za-z0-9_]*:""")
    }
}

private class KasmLineBlock(
    private val textRange: TextRange,
    private val indent: Indent
) : Block {
    override fun getTextRange(): TextRange = textRange

    override fun getSubBlocks(): List<Block> = emptyList()

    override fun getWrap(): Wrap? = null

    override fun getIndent(): Indent = indent

    override fun getAlignment() = null

    override fun getSpacing(child1: Block?, child2: Block): Spacing? = null

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes =
        ChildAttributes(Indent.getNoneIndent(), null)

    override fun isIncomplete(): Boolean = false

    override fun isLeaf(): Boolean = true
}
