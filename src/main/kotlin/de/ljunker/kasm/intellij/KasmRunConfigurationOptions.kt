package de.ljunker.kasm.intellij

import com.intellij.execution.configurations.RunConfigurationOptions

class KasmRunConfigurationOptions : RunConfigurationOptions() {

    private val executablePathProperty =
        string("kasm").provideDelegate(this, "executablePath")

    private val programPathProperty =
        string("").provideDelegate(this, "programPath")

    private val programArgsProperty =
        string("").provideDelegate(this, "programArgs")

    private val workingDirectoryProperty =
        string("").provideDelegate(this, "workingDirectory")

    var executablePath: String?
        get() = executablePathProperty.getValue(this)
        set(value) {
            executablePathProperty.setValue(this, value)
        }

    var programPath: String?
        get() = programPathProperty.getValue(this)
        set(value) {
            programPathProperty.setValue(this, value)
        }

    var programArgs: String?
        get() = programArgsProperty.getValue(this)
        set(value) {
            programArgsProperty.setValue(this, value)
        }

    var workingDirectory: String?
        get() = workingDirectoryProperty.getValue(this)
        set(value) {
            workingDirectoryProperty.setValue(this, value)
        }
}
