package de.ljunker.kasm.intellij

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.project.Project

class KasmConfigurationFactory(
    type: ConfigurationType
) : ConfigurationFactory(type) {

    override fun getId(): String = "KASM"

    override fun getOptionsClass(): Class<out RunConfigurationOptions> {
        return KasmRunConfigurationOptions::class.java
    }

    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return KasmRunConfiguration(
            project = project,
            factory = this,
            name = "KASM"
        )
    }
}