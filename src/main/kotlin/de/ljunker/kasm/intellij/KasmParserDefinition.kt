package de.ljunker.kasm.intellij

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

class KasmParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?): Lexer = KasmLexer()

    override fun createParser(project: Project?): PsiParser = PsiParser { root, builder ->
        val marker = builder.mark()
        while (!builder.eof()) {
            builder.advanceLexer()
        }
        marker.done(root)
        builder.treeBuilt
    }

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getWhitespaceTokens(): TokenSet = TokenSet.create(TokenType.WHITE_SPACE)

    override fun getCommentTokens(): TokenSet = TokenSet.create(KasmTokenTypes.COMMENT)

    override fun getStringLiteralElements(): TokenSet = TokenSet.create(KasmTokenTypes.STRING)

    override fun createElement(node: ASTNode): PsiElement = ASTWrapperPsiElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = KasmFile(viewProvider)

    override fun spaceExistenceTypeBetweenTokens(
        left: ASTNode?,
        right: ASTNode?
    ): ParserDefinition.SpaceRequirements = ParserDefinition.SpaceRequirements.MAY

    companion object {
        private val FILE = IFileElementType(KasmLanguage)
    }
}
