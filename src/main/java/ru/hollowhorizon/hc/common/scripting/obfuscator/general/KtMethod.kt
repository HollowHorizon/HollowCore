package ru.hollowhorizon.hc.common.scripting.obfuscator.general

import ru.hollowhorizon.hc.common.scripting.obfuscator.IKtNode

class KtMethod(
    var name: String,
    var returnType: KtType,
    var parameters: List<KtType>,
    var body: IKtNode<*>
): IKtNode<KtMethod> {
    override fun replace(new: KtMethod) {
        name = new.name
        returnType = new.returnType
        parameters = new.parameters
        body = new.body
    }
}