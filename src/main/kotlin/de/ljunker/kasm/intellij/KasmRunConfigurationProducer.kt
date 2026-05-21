package de.ljunker.kasm.intellij

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement

class KasmRunConfigurationProducer :
    LazyRunConfigurationProducer<KasmRunConfiguration>() {

    override fun getConfigurationFactory(): ConfigurationFactory {
        val type = ConfigurationTypeUtil.findConfigurationType(
            KasmRunConfigurationType::class.java
        )

        return type.configurationFactories.first()
    }

    override fun setupConfigurationFromContext(
        configuration: KasmRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val file = context.psiLocation
            ?.containingFile
            ?.virtualFile
            ?: context.location
                ?.virtualFile
            ?: return false

        if (file.extension != "kasm") {
            return false
        }

        configuration.name = file.nameWithoutExtension
        configuration.programPath = file.path
        configuration.workingDirectory =
            file.parent?.path ?: context.project.basePath.orEmpty()

        return true
    }

    override fun isConfigurationFromContext(
        configuration: KasmRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val file = context.psiLocation
            ?.containingFile
            ?.virtualFile
            ?: context.location
                ?.virtualFile
            ?: return false

        if (file.extension != "kasm") {
            return false
        }

        return configuration.programPath == file.path
    }
}