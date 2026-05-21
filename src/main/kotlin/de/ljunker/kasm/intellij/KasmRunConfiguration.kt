package de.ljunker.kasm.intellij

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project

class KasmRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<KasmRunConfigurationOptions>(project, factory, name) {

    override fun getOptions(): KasmRunConfigurationOptions {
        return super.getOptions() as KasmRunConfigurationOptions
    }

    var executablePath: String
        get() = options.executablePath ?: "kasm"
        set(value) {
            options.executablePath = value
        }

    var programPath: String
        get() = options.programPath ?: ""
        set(value) {
            options.programPath = value
        }

    var programArgs: String
        get() = options.programArgs ?: ""
        set(value) {
            options.programArgs = value
        }

    var workingDirectory: String
        get() = options.workingDirectory ?: ""
        set(value) {
            options.workingDirectory = value
        }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return KasmSettingsEditor(project)
    }

    override fun checkConfiguration() {
        if (executablePath.isBlank()) {
            throw RuntimeConfigurationException("No KASM executable configured")
        }

        if (programPath.isBlank()) {
            throw RuntimeConfigurationException("No .kasm file selected")
        }

        if (!programPath.endsWith(".kasm")) {
            throw RuntimeConfigurationWarning("Program file does not end with .kasm")
        }
    }

    override fun getState(
        executor: Executor,
        environment: ExecutionEnvironment
    ): RunProfileState {
        return KasmRunState(environment, this)
    }
}
