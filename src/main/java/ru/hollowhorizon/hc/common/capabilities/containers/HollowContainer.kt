package ru.hollowhorizon.hc.common.capabilities.containers

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.world.SimpleContainer
import net.minecraft.world.item.ItemStack
import ru.hollowhorizon.hc.client.utils.nbt.INBTSerializable
import ru.hollowhorizon.hc.client.utils.readItem
import ru.hollowhorizon.hc.client.utils.registryAccess
import ru.hollowhorizon.hc.client.utils.save
import ru.hollowhorizon.hc.common.capabilities.CapabilityInstance
import ru.hollowhorizon.hc.common.capabilities.CapabilityProperty
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityV2
import ru.hollowhorizon.hc.common.objects.entities.TestEntity

open class HollowContainer(val capability: CapabilityInstance, val size: Int, private val outputSlots: IntArray) :
    SimpleContainer(size), INBTSerializable {
    override fun setChanged() {
        capability.isChanged = true
    }

    override fun canPlaceItem(slot: Int, stack: ItemStack): Boolean {
        return slot !in outputSlots
    }

    override fun serialize() =
        ListTag().apply { addAll(items.map { if (!it.isEmpty) it.save() else CompoundTag() }) }

    override fun deserialize(tag: Tag) {
        items.clear()
        (tag as ListTag).forEachIndexed { index, tag ->
            if ((tag as CompoundTag).isEmpty) return@forEachIndexed

            items[index] = tag.readItem()
        }
    }
}

fun CapabilityInstance.container(
    size: Int,
    vararg outputSlots: Int,
): CapabilityProperty<CapabilityInstance, HollowContainer> {
    val container = HollowContainer(this, size, outputSlots)
    containers.add(container)
    return syncable(container).apply {
        defaultName = "container${containers.indexOf(container)}"
        defaultType = container.javaClass
    }
}

fun CapabilityInstance.container(container: HollowContainer): CapabilityProperty<CapabilityInstance, HollowContainer> {
    containers.add(container)
    return syncable(container).apply {
        defaultName = "container${containers.indexOf(container)}"
        defaultType = container.javaClass
    }
}

@HollowCapabilityV2(TestEntity::class)
class TestEntityCapability : CapabilityInstance() {
    val slots by container(27)
}