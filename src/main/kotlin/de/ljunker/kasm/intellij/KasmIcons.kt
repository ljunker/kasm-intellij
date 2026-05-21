package de.ljunker.kasm.intellij

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object KasmIcons {
    @JvmField
    val FILE: Icon = IconLoader.getIcon("/icons/kasm.svg", KasmIcons::class.java)

    @JvmField
    val RUN: Icon = FILE
}
