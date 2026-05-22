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
                        result.addElement(statementLookup(mnemonic, forms))
                    }

                    KasmLanguageReference.directiveNames.forEach { directive ->
                        val forms = KasmLanguageReference.directiveFormsFor(directive)
                        result.addElement(statementLookup(directive, forms))
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

private fun statementLookup(
    name: String,
    forms: List<KasmStatementForm>
): LookupElement {
    val hasOperands = forms.any { it.operands.isNotEmpty() }
    val usage = forms.joinToString(" | ") { it.operandSummary }
    val effects = forms
        .map { it.effect }
        .distinct()
        .joinToString(" / ")

    var lookup = LookupElementBuilder.create(name)
        .withCaseSensitivity(false)
        .withTailText(" $usage", true)
        .withTypeText(effects, true)
        .withInsertHandler(
            KasmStatementInsertHandler(
                hasOperands = hasOperands,
                isDirective = name.startsWith(".")
            )
        )

    if (name.startsWith(".")) {
        lookup = lookup.withLookupString(name.removePrefix("."))
    }

    return lookup
}

private class KasmStatementInsertHandler(
    private val hasOperands: Boolean,
    private val isDirective: Boolean
) : InsertHandler<LookupElement> {
    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        if (isDirective) {
            removeDuplicateDirectiveDot(context)
        }

        if (!hasOperands) {
            return
        }

        context.setLaterRunnable {
            AutoPopupController.getInstance(context.project)
                .autoPopupParameterInfo(context.editor, context.file)
        }
    }

    private fun removeDuplicateDirectiveDot(context: InsertionContext) {
        val insertedStart = context.startOffset
        val document = context.document
        if (insertedStart == 0 || insertedStart >= document.textLength) {
            return
        }

        val text = document.charsSequence
        if (text[insertedStart - 1] == '.' && text[insertedStart] == '.') {
            document.deleteString(insertedStart - 1, insertedStart)
        }
    }
}
