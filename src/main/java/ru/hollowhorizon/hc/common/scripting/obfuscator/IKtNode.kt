package ru.hollowhorizon.hc.common.scripting.obfuscator

interface IKtNode<T: IKtNode<T>> {
    fun replace(new: T)
}