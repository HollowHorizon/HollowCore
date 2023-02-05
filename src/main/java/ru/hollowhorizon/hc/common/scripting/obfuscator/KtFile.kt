package ru.hollowhorizon.hc.common.scripting.obfuscator

import ru.hollowhorizon.hc.common.scripting.obfuscator.general.KtClass
import ru.hollowhorizon.hc.common.scripting.obfuscator.general.KtImport

class KtFile(
    val packageName: String,
    val imports: List<KtImport>,
    val classes: List<KtClass>
) : IKtNode<KtFile> {
    override fun replace(new: KtFile) {
        throw UnsupportedOperationException("You can't replace all file!")
    }

}