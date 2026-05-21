package de.ljunker.kasm.intellij

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object KasmFileType : LanguageFileType(KasmLanguage) {
    override fun getName(): String = "KASM"
    override fun getDescription(): String = "KASM assembly file"
    override fun getDefaultExtension(): String = "kasm"
    override fun getIcon(): Icon = KasmIcons.FILE
}