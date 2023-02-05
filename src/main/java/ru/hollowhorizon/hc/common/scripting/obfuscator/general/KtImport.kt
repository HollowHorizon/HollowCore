package ru.hollowhorizon.hc.common.scripting.obfuscator.general

import ru.hollowhorizon.hc.common.scripting.obfuscator.IKtNode

class KtImport(var import: String) : IKtNode<KtImport> {
    var className: String = import.substringAfterLast(".")

    override fun replace(new: KtImport) {
        import = new.import
    }
}