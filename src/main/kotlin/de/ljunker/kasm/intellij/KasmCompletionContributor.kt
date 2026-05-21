package de.ljunker.kasm.intellij

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext

class KasmCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    KasmLanguageReference.instructionNames.forEach { mnemonic ->
                        val forms = KasmLanguageReference.formsFor(mnemonic)
                        val hasOperands = forms.any { it.operands.isNotEmpty() }
                        val usage = forms.joinToString(" | ") { it.operandSummary }
                        val effects = forms
                            .map { it.effect }
                            .distinct()
                            .joinToString(" / ")

                        result.addElement(
                            LookupElementBuilder.create(mnemonic)
                                .withCaseSensitivity(false)
                                .withTailText(" $usage", true)
                                .withTypeText(effects, true)
                                .withInsertHandler(KasmInstructionInsertHandler(hasOperands))
                        )
                    }

                    KasmLanguageReference.registers.forEach {
                        result.addElement(
                            LookupElementBuilder.create(it)
                                .withCaseSensitivity(false)
                        )
                    }
                }
            }
        )
    }
}

private class KasmInstructionInsertHandler(
    private val hasOperands: Boolean
) : InsertHandler<LookupElement> {
    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        if (!hasOperands) {
            return
        }

        context.setLaterRunnable {
            AutoPopupController.getInstance(context.project)
                .autoPopupParameterInfo(context.editor, context.file)
        }
    }
}
