package de.ljunker.kasm.intellij

import com.intellij.lang.parameterInfo.CreateParameterInfoContext
import com.intellij.lang.parameterInfo.ParameterInfoHandler
import com.intellij.lang.parameterInfo.ParameterInfoUIContext
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext
import com.intellij.psi.PsiElement

class KasmParameterInfoHandler : ParameterInfoHandler<PsiElement, KasmInstructionForm> {
    override fun findElementForParameterInfo(context: CreateParameterInfoContext): PsiElement? {
        val call = KasmInstructionCallFinder.find(context.file.text, context.offset)
            ?: return null
        val forms = KasmLanguageReference.formsFor(call.mnemonic)
        if (forms.isEmpty()) {
            return null
        }

        context.itemsToShow = forms.toTypedArray()
        return context.file
    }

    override fun showParameterInfo(element: PsiElement, context: CreateParameterInfoContext) {
        val call = KasmInstructionCallFinder.find(context.file.text, context.offset)
            ?: return

        context.showHint(element, call.mnemonicStart, this)
    }

    override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): PsiElement? {
        KasmInstructionCallFinder.find(context.file.text, context.offset)
            ?: return null

        return context.file
    }

    override fun updateParameterInfo(parameterOwner: PsiElement, context: UpdateParameterInfoContext) {
        val call = KasmInstructionCallFinder.find(context.file.text, context.offset)
            ?: return context.removeHint()

        context.parameterOwner = parameterOwner
        context.setCurrentParameter(call.operandIndex)
    }

    override fun updateUI(parameter: KasmInstructionForm, context: ParameterInfoUIContext) {
        val range = parameter.operandRange(context.currentParameterIndex)
        val presentation = "${parameter.signature}    ${parameter.effect}"

        context.setupUIComponentPresentation(
            presentation,
            range?.first ?: NO_HIGHLIGHT,
            range?.last?.plus(1) ?: NO_HIGHLIGHT,
            false,
            false,
            false,
            context.defaultParameterColor
        )
    }

    override fun isWhitespaceSensitive(): Boolean = true

    companion object {
        private const val NO_HIGHLIGHT = -1
    }
}

internal data class KasmInstructionCall(
    val mnemonic: String,
    val mnemonicStart: Int,
    val operandIndex: Int
)

internal object KasmInstructionCallFinder {
    fun find(text: String, caretOffset: Int): KasmInstructionCall? {
        val caret = caretOffset.coerceIn(0, text.length)
        val lineStart = lineStart(text, caret)
        val codeEnd = codeEnd(text, lineStart, caret) ?: return null
        var offset = skipIndent(text, lineStart, codeEnd)

        while (offset < codeEnd) {
            val labelEnd = identifierEnd(text, offset, codeEnd)
            if (labelEnd == offset || labelEnd >= codeEnd || text[labelEnd] != ':') {
                break
            }

            offset = skipIndent(text, labelEnd + 1, codeEnd)
        }

        val mnemonicEnd = identifierEnd(text, offset, codeEnd)
        if (mnemonicEnd == offset) {
            return null
        }

        val mnemonic = text.substring(offset, mnemonicEnd)
        val forms = KasmLanguageReference.formsFor(mnemonic)
        if (forms.isEmpty()) {
            return null
        }

        return KasmInstructionCall(
            mnemonic = forms.first().mnemonic,
            mnemonicStart = offset,
            operandIndex = operandIndex(text, mnemonicEnd, codeEnd, forms)
        )
    }

    private fun codeEnd(text: String, lineStart: Int, caret: Int): Int? {
        val comment = text.indexOf(';', startIndex = lineStart)
        if (comment >= 0 && comment < caret && commentBeforeLineEnd(text, comment, caret)) {
            return null
        }
        return caret
    }

    private fun commentBeforeLineEnd(text: String, comment: Int, caret: Int): Boolean {
        for (offset in comment until caret) {
            if (text[offset].isLineBreak()) {
                return false
            }
        }
        return true
    }

    private fun operandIndex(
        text: String,
        mnemonicEnd: Int,
        codeEnd: Int,
        forms: List<KasmInstructionForm>
    ): Int {
        if (forms.all { it.operands.isEmpty() }) {
            return NO_OPERAND
        }

        var index = 0
        for (offset in mnemonicEnd until codeEnd) {
            if (text[offset] == ',') {
                index++
            }
        }
        return index
    }

    private fun lineStart(text: String, caret: Int): Int {
        var offset = caret
        while (offset > 0 && !text[offset - 1].isLineBreak()) {
            offset--
        }
        return offset
    }

    private fun skipIndent(text: String, start: Int, end: Int): Int {
        var offset = start
        while (offset < end && text[offset].isIndentWhitespace()) {
            offset++
        }
        return offset
    }

    private fun identifierEnd(text: String, start: Int, end: Int): Int {
        if (start >= end || !text[start].isIdentifierStart()) {
            return start
        }

        var offset = start + 1
        while (offset < end && text[offset].isIdentifierPart()) {
            offset++
        }
        return offset
    }

    private fun Char.isIdentifierStart(): Boolean =
        this in 'A'..'Z' || this in 'a'..'z' || this == '_'

    private fun Char.isIdentifierPart(): Boolean =
        isIdentifierStart() || this in '0'..'9'

    private fun Char.isIndentWhitespace(): Boolean = this == ' ' || this == '\t'

    private fun Char.isLineBreak(): Boolean = this == '\n' || this == '\r'

    private const val NO_OPERAND = -1
}
