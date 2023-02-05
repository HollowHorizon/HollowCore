package ru.hollowhorizon.hc.common.scripting.obfuscator.general

import ru.hollowhorizon.hc.common.scripting.obfuscator.IKtNode

class KtField(
    var name: String,
    var type: KtType,
    var value: IKtNode<*>?
) : IKtNode<KtField> {
    override fun replace(new: KtField) {
        name = new.name
        type = new.type
        value = new.value
    }
}