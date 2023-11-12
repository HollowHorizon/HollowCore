package ru.hollowhorizon.hc.common.capabilities

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraftforge.common.util.INBTSerializable

class SyncableMapImpl<K : Any, V : Any>(val map: MutableMap<K, V>, val syncMethod: () -> Unit = {}) : MutableMap<K, V>,
    INBTSerializable<Tag> {
    override val entries = map.entries
    override val keys = map.keys
    override val size = map.size
    override val values = map.values

    override fun clear() {
        map.clear()
        syncMethod()
    }

    override fun isEmpty() = map.isEmpty()

    override fun remove(key: K) = map.remove(key).apply { syncMethod() }

    override fun putAll(from: Map<out K, V>) {
        map.putAll(from)
        syncMethod()
    }

    override fun put(key: K, value: V): V? {
        return map.put(key, value).apply {
            syncMethod()
        }
    }

    override fun get(key: K) = map[key]

    override fun containsValue(value: V) = map.containsValue(value)

    override fun containsKey(key: K) = map.containsKey(key)
    override fun serializeNBT(): Tag {
        return CompoundTag().apply {
            put("keys", SyncableListImpl(map.keys.toMutableList()).serializeNBT())
            put("values", SyncableListImpl(map.values.toMutableList()).serializeNBT())
        }
    }

    override fun deserializeNBT(nbt: Tag) {
        val tag = nbt as CompoundTag
        val keys = SyncableListImpl(ArrayList())
        keys.deserializeNBT(tag.get("keys")!!)
        val values = SyncableListImpl(ArrayList())
        values.deserializeNBT(tag.get("values")!!)
        map.clear()
        keys.forEachIndexed { index, key ->
            map[key as K] = values[index] as V
        }
    }

    override fun toString(): String {
        return map.toString()
    }
}