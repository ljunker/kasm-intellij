package de.ljunker.kasm.intellij

import com.intellij.execution.configurations.ConfigurationType

class KasmRunConfigurationType : ConfigurationType {
    private val factory = KasmConfigurationFactory(this)

    override fun getDisplayName() = "KASM"
    override fun getConfigurationTypeDescription() = "Run KASM program"
    override fun getId() = "KASM_RUN_CONFIGURATION"
    override fun getIcon() = KasmIcons.FILE
    override fun getConfigurationFactories() = arrayOf(factory)
}