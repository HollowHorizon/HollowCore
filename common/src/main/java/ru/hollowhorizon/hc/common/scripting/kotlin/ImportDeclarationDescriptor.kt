package ru.hollowhorizon.hc.common.scripting.kotlin

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptorVisitor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.Name

class ImportDeclarationDescriptor(private val name: Name) : DeclarationDescriptor {
    override fun getName() = name

    override fun getOriginal(): DeclarationDescriptor {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getContainingDeclaration(): DeclarationDescriptor? {
        return null
    }

    override fun <R : Any?, D : Any?> accept(visitor: DeclarationDescriptorVisitor<R, D>?, data: D): R {
        throw UnsupportedOperationException("not implemented")
    }

    override fun acceptVoid(visitor: DeclarationDescriptorVisitor<Void, Void>?) {
    }

    override val annotations: Annotations = Annotations.EMPTY
}

class ClassDeclarationDescriptor(private val name: Name) : DeclarationDescriptor {
    override fun getName() = name

    override fun getOriginal(): DeclarationDescriptor {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getContainingDeclaration(): DeclarationDescriptor? {
        return null
    }

    override fun <R : Any?, D : Any?> accept(visitor: DeclarationDescriptorVisitor<R, D>?, data: D): R {
        throw UnsupportedOperationException("not implemented")
    }

    override fun acceptVoid(visitor: DeclarationDescriptorVisitor<Void, Void>?) {
    }

    override val annotations: Annotations = Annotations.EMPTY
}