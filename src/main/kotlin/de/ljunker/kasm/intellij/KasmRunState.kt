package de.ljunker.kasm.intellij

import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.util.execution.ParametersListUtil

class KasmRunState(
    environment: ExecutionEnvironment,
    private val configuration: KasmRunConfiguration
) : CommandLineState(environment) {

    override fun startProcess(): ProcessHandler {
        val commandLine = GeneralCommandLine()
            .withExePath(configuration.executablePath)
            .withParameters(configuration.programPath)

        if (configuration.programArgs.isNotBlank()) {
            commandLine.addParameters(ParametersListUtil.parse(configuration.programArgs))
        }

        val workDir = configuration.workingDirectory
        if (workDir.isNotBlank()) {
            commandLine.withWorkDirectory(workDir)
        }

        return OSProcessHandler(commandLine)
    }
}
