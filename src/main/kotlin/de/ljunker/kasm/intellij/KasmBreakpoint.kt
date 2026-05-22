package de.ljunker.kasm.intellij

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.xdebugger.breakpoints.XBreakpointProperties
import com.intellij.xdebugger.breakpoints.XLineBreakpointType

class KasmLineBreakpointType :
    XLineBreakpointType<KasmBreakpointProperties>(
        "kasm-line",
        "KASM Line Breakpoints"
    ) {
    override fun canPutAt(
        file: VirtualFile,
        line: Int,
        project: Project
    ): Boolean =
        file.extension.equals("kasm", ignoreCase = true)

    override fun createBreakpointProperties(
        file: VirtualFile,
        line: Int
    ) = KasmBreakpointProperties()
}

class KasmBreakpointProperties :
    XBreakpointProperties<KasmBreakpointProperties>() {
    override fun getState(): KasmBreakpointProperties = this

    override fun loadState(state: KasmBreakpointProperties) = Unit
}
