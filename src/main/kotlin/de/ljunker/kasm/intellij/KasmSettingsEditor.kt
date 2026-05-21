package de.ljunker.kasm.intellij

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.RawCommandLineEditor
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class KasmSettingsEditor(
    project: Project
) : SettingsEditor<KasmRunConfiguration>() {

    private val executableField = TextFieldWithBrowseButton()
    private val programField = TextFieldWithBrowseButton()
    private val argumentsField = RawCommandLineEditor()
    private val workingDirectoryField = TextFieldWithBrowseButton()

    private val panel: JPanel = FormBuilder.createFormBuilder()
        .addLabeledComponent("KASM executable:", executableField)
        .addLabeledComponent("Program file:", programField)
        .addLabeledComponent("Arguments:", argumentsField)
        .addLabeledComponent("Working directory:", workingDirectoryField)
        .panel

    init {
        executableField.addBrowseFolderListener(
            project,
            FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
        )

        programField.addBrowseFolderListener(
            project,
            FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
        )

        workingDirectoryField.addBrowseFolderListener(
            project,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        )
    }

    override fun resetEditorFrom(configuration: KasmRunConfiguration) {
        executableField.text = configuration.executablePath
        programField.text = configuration.programPath
        argumentsField.text = configuration.programArgs
        workingDirectoryField.text = configuration.workingDirectory
    }

    override fun applyEditorTo(configuration: KasmRunConfiguration) {
        configuration.executablePath = executableField.text.trim()
        configuration.programPath = programField.text.trim()
        configuration.programArgs = argumentsField.text.trim()
        configuration.workingDirectory = workingDirectoryField.text.trim()
    }

    override fun createEditor(): JComponent {
        return panel
    }
}
