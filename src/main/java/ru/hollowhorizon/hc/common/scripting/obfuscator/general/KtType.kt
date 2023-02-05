package ru.hollowhorizon.hc.common.scripting.obfuscator.general

import ru.hollowhorizon.hc.common.scripting.obfuscator.IKtNode

class KtType(
    var name: String,
    var parent: KtType?
): IKtNode<KtType> {
    override fun replace(new: KtType) {
        name = new.name
        parent = new.parent
    }
}