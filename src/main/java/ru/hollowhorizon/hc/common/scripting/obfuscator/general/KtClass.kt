package ru.hollowhorizon.hc.common.scripting.obfuscator.general

import org.jetbrains.kotlin.psi.KtFunction
import ru.hollowhorizon.hc.common.scripting.obfuscator.IKtNode

class KtClass(
    var name: String,
    var constructors: List<KtFunction>,
    var fields: List<KtField>,
    var methods: List<KtMethod>,
    var innerClasses: List<KtClass>,
    var parent: KtClass?
) : IKtNode<KtClass> {
    override fun replace(new: KtClass) {
        name = new.name
        fields = new.fields
        methods = new.methods
        innerClasses = new.innerClasses
        parent = new.parent
    }

}