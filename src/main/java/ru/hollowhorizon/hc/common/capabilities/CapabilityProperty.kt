package ru.hollowhorizon.hc.common.capabilities

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.EndTag
import net.minecraft.nbt.Tag
import net.minecraftforge.common.util.INBTSerializable
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.JavaHacks
import ru.hollowhorizon.hc.client.utils.nbt.NBTFormat
import ru.hollowhorizon.hc.client.utils.nbt.deserializeNoInline
import ru.hollowhorizon.hc.client.utils.nbt.serializeNoInline
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.javaType

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalStdlibApi::class)
open class CapabilityProperty<T : CapabilityInstance, V : Any?>(var value: V) : ReadWriteProperty<T, V> {
    var defaultName = ""
    private var defaultType: Class<out V>? = null
    override fun getValue(thisRef: T, property: KProperty<*>): V {
        if (defaultName.isEmpty()) {
            defaultName = property.name
            defaultType = if(value == null) property.returnType.javaType as Class<out V> else value!!.javaClass
            if(property.name !in thisRef.notUsedTags) return value

            val tag = thisRef.notUsedTags.get(property.name) ?: return value
            if (tag is EndTag) return value
            if (value is INBTSerializable<*>) {
                (value as INBTSerializable<Tag>).deserializeNBT(tag)
                return value
            }

            value = NBTFormat.deserializeNoInline(tag, property.returnType.javaType as Class<out V>) as V
        }

        return value
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        if (defaultName.isEmpty()) defaultName = property.name
        this.value = value
        if (defaultType == null) defaultType = if(this.value == null) property.returnType.javaType as Class<out V> else this.value!!.javaClass
        thisRef.sync()
    }

    fun serialize(tag: CompoundTag) {
        if (defaultName.isNotEmpty() && defaultType != null) {
            when (value) {
                null -> tag.put(defaultName, EndTag.INSTANCE)
                is INBTSerializable<*> -> tag.put(defaultName, (value as INBTSerializable<Tag>).serializeNBT())
                else -> tag.put(
                    defaultName,
                    NBTFormat.serializeNoInline(JavaHacks.forceCast(value), this.defaultType!!)
                )
            }
        }
    }

    fun deserialize(tag: CompoundTag): Boolean {
        if (defaultName.isNotEmpty() && defaultType != null && defaultName in tag) {
            try {
                if (tag[defaultName] is EndTag) value = null as V
                else if (value is INBTSerializable<*>) (value as INBTSerializable<Tag>).deserializeNBT(tag[defaultName]!!)
                else value = NBTFormat.deserializeNoInline(tag[defaultName]!!, this.defaultType!!)
                return true
            } catch (e: Exception) {
                HollowCore.LOGGER.error("Error while deserializing {}: {}", defaultType, tag[defaultName], e)
            }
        }
        return false
    }
}