package de.ljunker.kasm.intellij

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.GenericProgramRunner
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugProcessStarter
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManager
import de.ljunker.kasm.Assembler
import de.ljunker.kasm.AssemblyException
import java.nio.file.Path

class KasmDebugRunner : GenericProgramRunner<RunnerSettings>() {
    override fun getRunnerId() = "KasmDebugRunner"

    override fun canRun(executorId: String, profile: RunProfile): Boolean =
        executorId == DefaultDebugExecutor.EXECUTOR_ID &&
                profile is KasmRunConfiguration

    override fun doExecute(
        state: RunProfileState,
        environment: ExecutionEnvironment
    ): RunContentDescriptor {
        val configuration = environment.runProfile as? KasmRunConfiguration
            ?: throw ExecutionException("KASM debug runner requires a KASM run configuration")

        FileDocumentManager.getInstance().saveAllDocuments()

        val sourceFile = LocalFileSystem.getInstance()
            .findFileByPath(configuration.programPath)
            ?: throw ExecutionException("KASM program file not found: ${configuration.programPath}")

        val sourcePath = Path.of(configuration.programPath).toAbsolutePath().normalize()
        val debugProgram = try {
            Assembler().assembleFileWithDebugInfo(sourcePath)
        } catch (error: AssemblyException) {
            throw ExecutionException("KASM assembly failed: ${error.message}", error)
        }

        val debugSession = XDebuggerManager.getInstance(environment.project)
            .startSession(
                environment,
                object : XDebugProcessStarter() {
                    override fun start(session: XDebugSession): XDebugProcess =
                        KasmDebugProcess(
                            session = session,
                            debugProgram = debugProgram
                        )
                }
            )

        return debugSession.runContentDescriptor
    }
}
