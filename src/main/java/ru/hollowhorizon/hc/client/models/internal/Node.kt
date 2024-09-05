package ru.hollowhorizon.hc.client.models.internal

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ArmorItem
import org.joml.Matrix4f
import org.joml.Quaternionf
import ru.hollowhorizon.hc.client.models.gltf.hasFirstPersonModel
import ru.hollowhorizon.hc.client.utils.getArmorTexture
import ru.hollowhorizon.hc.client.utils.toTexture
import ru.hollowhorizon.hc.client.utils.use

//? if <=1.19.2 {
import ru.hollowhorizon.hc.client.utils.toMc
//?}

class Node(
    val index: Int,
    val children: List<Node>,
    val transform: Transformation,
    val mesh: Mesh? = null,
    val skin: Skin? = null,
    val name: String? = null,
) {
    val baseTransform = transform.copy()
    var isHovered = false

    fun isAllHovered(): Boolean = isHovered || parent?.isAllHovered() == true

    val isArmor = name?.contains("armor", ignoreCase = true) == true
    val isHelmet = isArmor && name?.contains("helmet", ignoreCase = true) == true
    val isChestplate = isArmor && name?.contains("chestplate", ignoreCase = true) == true
    val isLeggings = isArmor && name?.contains("leggings", ignoreCase = true) == true
    val isBoots = isArmor && name?.contains("boots", ignoreCase = true) == true

    fun render(
        stack: PoseStack,
        nodeRenderer: NodeRenderer,
        data: ModelData,
        consumer: (ResourceLocation) -> Int,
        light: Int,
    ) {
        val entity = data.entity
        var changedTexture = consumer
        if (isArmor) {
            if (entity == null) return
            when {
                !entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty && isHelmet -> {
                    val armorItem = entity.getItemBySlot(EquipmentSlot.HEAD)
                    if (armorItem.item is ArmorItem) {
                        val texture = armorItem.getArmorTexture(entity, EquipmentSlot.HEAD)
                        changedTexture = { texture.toTexture().id }
                    }
                }

                !entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty && isChestplate -> {
                    val armorItem = entity.getItemBySlot(EquipmentSlot.CHEST)
                    if (armorItem.item is ArmorItem) {
                        val texture = armorItem.getArmorTexture(entity, EquipmentSlot.CHEST)
                        changedTexture = { texture.toTexture().id }
                    }
                }

                !entity.getItemBySlot(EquipmentSlot.LEGS).isEmpty && isLeggings -> {
                    val armorItem = entity.getItemBySlot(EquipmentSlot.LEGS)
                    if (armorItem.item is ArmorItem) {
                        val texture = armorItem.getArmorTexture(entity, EquipmentSlot.LEGS)
                        changedTexture = { texture.toTexture().id }
                    }
                }

                !entity.getItemBySlot(EquipmentSlot.FEET).isEmpty && isBoots -> {
                    val armorItem = entity.getItemBySlot(EquipmentSlot.FEET)
                    if (armorItem.item is ArmorItem) {
                        val texture = armorItem.getArmorTexture(entity, EquipmentSlot.FEET)
                        changedTexture = { texture.toTexture().id }
                    }
                }

                else -> return
            }
        }

        if (hasFirstPersonModel && /*dev.tr7zw.firstperson.api.FirstPersonAPI.isRenderingPlayer() &&*/ name?.contains(
                "head",
                ignoreCase = true
            ) == true
        ) return

        stack.use {
            //? if >=1.21 {
            /*mulPose(localMatrix)
            *///?} elif >=1.20.1 {
            /*mulPoseMatrix(localMatrix)
            *///?} else {
            mulPoseMatrix(localMatrix.toMc())
            //?}

            mesh?.render(this@Node, stack, changedTexture)
            children.forEach { it.render(stack, nodeRenderer, data, changedTexture, light) }
        }
    }

    fun renderDecorations(
        stack: PoseStack,
        nodeRenderer: NodeRenderer,
        data: ModelData,
        source: MultiBufferSource,
        light: Int,
    ) {
        stack.use {
            //? if >=1.21 {
            /*mulPose(localMatrix)
            last().normal().mul(normalMatrix)
            *///?} elif >=1.20.1 {
            /*mulPoseMatrix(localMatrix)
            last().normal().mul(normalMatrix)
            *///?} else {
            mulPoseMatrix(localMatrix.toMc())
            last().normal().mul(normalMatrix.toMc())
            //?}

            data.entity?.let {
                nodeRenderer(it, stack, this@Node, source, light)
            }

            children.forEach { it.renderDecorations(stack, nodeRenderer, data, source, light) }
        }
    }

    fun transformSkinning(stack: PoseStack) {
        mesh?.transformSkinning(this@Node, stack)
        children.forEach { it.transformSkinning(stack) }

    }

    fun clearTransform() = transform.set(baseTransform)


    fun toLocal(transform: Transformation): Transformation {
        return baseTransform.copy().apply { sub(transform) }
    }

    fun fromLocal(transform: Transformation): Transformation {
        return baseTransform.copy().apply { add(transform) }
    }


    var parent: Node? = null
    val isHead: Boolean get() = name?.lowercase()?.contains("head") == true && parent?.isHead == false

    val globalMatrix: Matrix4f
        get() {
            val matrix = parent?.globalMatrix ?: return localMatrix
            return matrix.mul(localMatrix)
        }

    val globalRotation: Quaternionf
        get() {
            val rotation = parent?.globalRotation ?: return transform.rotation
            transform.apply {
                rotation.mulLeft(this.rotation)
            }
            return rotation
        }

    val localMatrix get() = transform.getMatrix()
    val normalMatrix get() = transform.getNormalMatrix()
}