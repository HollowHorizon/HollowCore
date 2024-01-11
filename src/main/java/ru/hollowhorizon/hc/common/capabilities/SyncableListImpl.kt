package ru.hollowhorizon.hc.common.capabilities

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraftforge.common.util.INBTSerializable
import ru.hollowhorizon.hc.client.utils.nbt.NBTFormat
import ru.hollowhorizon.hc.client.utils.nbt.deserializeNoInline
import ru.hollowhorizon.hc.client.utils.nbt.serializeNoInline
import java.util.function.Predicate


class SyncableListImpl<T : Any>(val list: MutableList<T>, private val updateMethod: () -> Unit = {}) :
    MutableList<T>,
    INBTSerializable<Tag> {
    override val size = list.size
    override fun clear() {
        list.clear()
        updateMethod()
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val addedAny = list.addAll(elements)
        if (addedAny) updateMethod()
        return addedAny
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        val addedAny = list.addAll(index, elements)
        if (addedAny) updateMethod()
        return addedAny
    }

    fun addNoUpdate(index: Int, element: T) {
        list.add(index, element)
    }

    fun addNoUpdate(element: T) {
        list.add(element)
    }

    override fun add(index: Int, element: T) {
        list.add(index, element)
        updateMethod()
    }

    override fun add(element: T): Boolean {
        return list.add(element).apply {
            updateMethod()
        }
    }

    override fun get(index: Int) = list[index]

    override fun indexOf(element: T) = list.indexOf(element)

    override fun containsAll(elements: Collection<T>) = list.containsAll(elements)

    override fun isEmpty() = list.isEmpty()

    override fun contains(element: T) = list.contains(element)

    override fun iterator(): MutableIterator<T> {
        return Itr(list.iterator())
    }

    override fun listIterator(): MutableListIterator<T> {
        return ListItr(list.listIterator())
    }

    override fun listIterator(index: Int): MutableListIterator<T> {
        return ListItr(list.listIterator(index))
    }

    override fun removeAt(index: Int): T {
        return list.removeAt(index).apply {
            updateMethod()
        }
    }

    fun removeAtNoUpdate(index: Int) {
        list.removeAt(index)
    }

    fun removeIfNoUpdate(filter: Predicate<T>): Boolean {
        val each = Itr(list.iterator())
        var removed = false

        while (each.hasNext()) {
            if (filter.test(each.next())) {
                each.removeNoUpdate()
                removed = true
            }
        }

        return removed
    }

    override fun set(index: Int, element: T): T {
        return list.set(index, element).apply {
            updateMethod()
        }
    }

    fun setNoUpdate(index: Int, element: T) {
        list.set(index, element)
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        val changedAny = list.retainAll(elements)
        if (changedAny) updateMethod()
        return changedAny
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        val changedAny = list.removeAll(elements)
        if (changedAny) updateMethod()
        return changedAny
    }

    override fun remove(element: T): Boolean {
        val changedAny = list.remove(element)
        if (changedAny) updateMethod()
        return changedAny
    }

    fun removeNoUpdate(element: T) {
        list.remove(element)
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> {
        return list.subList(fromIndex, toIndex)
    }

    override fun lastIndexOf(element: T) = list.lastIndexOf(element)

    private open inner class Itr<U>(val internalIterator: MutableIterator<U>) : MutableIterator<U> {
        override fun hasNext() = internalIterator.hasNext()

        override fun next(): U = internalIterator.next()

        override fun remove() {
            internalIterator.remove()
            updateMethod()
        }

        fun removeNoUpdate() {
            internalIterator.remove()
        }
    }

    private inner class ListItr(internalIterator: MutableListIterator<T>) : Itr<T>(internalIterator),
        MutableListIterator<T> {
        private fun internalIterator(): MutableListIterator<T> {
            return internalIterator as MutableListIterator<T>
        }

        override fun add(element: T) {
            internalIterator().add(element)
            updateMethod()
        }

        override fun hasPrevious() = internalIterator().hasPrevious()

        override fun nextIndex() = internalIterator().nextIndex()

        override fun previous() = internalIterator().previous()

        override fun previousIndex() = internalIterator().previousIndex()

        override fun set(element: T) {
            internalIterator().set(element)
            updateMethod()
        }

    }

    override fun serializeNBT(): Tag {
        return ListTag().apply {
            list.forEach { element ->
                add(CompoundTag().apply {
                    putString("class", element::class.java.name)
                    put("nbt", NBTFormat.serializeNoInline(element, element::class.java as Class<T>))
                })
            }
        }
    }

    override fun deserializeNBT(nbt: Tag) {
        if (nbt is ListTag) {
            list.clear()
            nbt.forEach { element ->
                if (element is CompoundTag) {
                    list.add(
                        NBTFormat.deserializeNoInline(
                            element.get("nbt")!!,
                            Class.forName(element.getString("class"))
                        ) as T
                    )
                }
            }
        }
    }

    override fun toString(): String {
        return list.toString()
    }
}
