package ru.hollowhorizon.hc.common.objects.entities

import net.minecraft.entity.EntityType
import net.minecraft.entity.MobEntity
import net.minecraft.potion.EffectInstance
import net.minecraft.world.World
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.common.capabilities.Capability
import ru.hollowhorizon.hc.client.gltf.GlTFModelManager
import ru.hollowhorizon.hc.client.gltf.IAnimatedEntity
import ru.hollowhorizon.hc.client.gltf.RenderedGltfModel
import ru.hollowhorizon.hc.client.gltf.animation.AnimationTypes
import ru.hollowhorizon.hc.client.gltf.animation.GLTFAnimation
import ru.hollowhorizon.hc.client.gltf.animation.loadAnimations
import ru.hollowhorizon.hc.common.capabilities.*

class TestEntity(type: EntityType<TestEntity>, world: World) : MobEntity(type, world), IAnimatedEntity,
    ICapabilitySyncer {


    init {
        this.getCapability<AnimatedEntityCapability>().syncEntity(this)
    }

    override fun onEffectAdded(effect: EffectInstance) {
        super.onEffectAdded(effect)
    }

    override fun tick() {
        this.navigation.moveTo(8.0, 56.0, 6.0, 0.3)

        super.tick()
    }

    override fun onCapabilitySync(capability: Capability<*>) {
        if (capability == HollowCapabilityV2.get<AnimatedEntityCapability>() && level.isClientSide) {
            val animCapability = this.getCapability<AnimatedEntityCapability>()

            renderedGltfModel = GlTFModelManager.getOrCreate(this, animCapability)
            animationList = renderedGltfModel!!.loadAnimations()

            AnimationTypes.values().forEach { type ->
                tryAddAnimation(type, animCapability, animationList)
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private fun tryAddAnimation(
        type: AnimationTypes,
        capability: AnimatedEntityCapability,
        animationList: List<GLTFAnimation>
    ) {
        when (type) {
            AnimationTypes.IDLE -> capability.animations[type] = animationList.find { it.name.contains("idle") }?.name
                ?: ""

            AnimationTypes.IDLE_SNEAKED -> capability.animations[type] =
                animationList.find { it.name.contains("idle") && it.name.contains("sneak") }?.name
                    ?: capability.animations[AnimationTypes.IDLE] ?: ""

            AnimationTypes.WALK -> capability.animations[type] = animationList
                .filter {
                    it.name.contains("walk") || it.name.contains("go") || it.name.contains("run") || it.name.contains("move")
                }.minByOrNull {
                    when {
                        it.name.contains("walk") -> 0
                        it.name.contains("go") -> 1
                        it.name.contains("run") -> 2
                        it.name.contains("move") -> 3
                        else -> 4
                    }
                }?.name ?: ""

            AnimationTypes.WALK_SNEAKED -> capability.animations[type] =
                animationList.find { it.name.contains("walk") && it.name.contains("sneak") }?.name
                    ?: capability.animations[AnimationTypes.WALK] ?: ""

            AnimationTypes.RUN -> capability.animations[type] = animationList.find { it.name.contains("run") }?.name
                ?: capability.animations[AnimationTypes.WALK] ?: ""

            AnimationTypes.SWIM -> capability.animations[type] = animationList.find { it.name.contains("swim") }?.name
                ?: capability.animations[AnimationTypes.WALK] ?: ""

            AnimationTypes.FALL -> capability.animations[type] = animationList.find { it.name.contains("fall") }?.name
                ?: capability.animations[AnimationTypes.IDLE] ?: ""

            AnimationTypes.FLY -> capability.animations[type] = animationList.find { it.name.contains("fly") }?.name
                ?: capability.animations[AnimationTypes.IDLE] ?: ""

            AnimationTypes.SIT -> capability.animations[type] = animationList.find { it.name.contains("sit") }?.name
                ?: capability.animations[AnimationTypes.IDLE] ?: ""

            AnimationTypes.SLEEP -> capability.animations[type] = animationList.find { it.name.contains("sleep") }?.name
                ?: capability.animations[AnimationTypes.SLEEP] ?: ""

            AnimationTypes.SWING -> capability.animations[type] =
                animationList.find { it.name.contains("attack") || it.name.contains("swing") }?.name ?: ""

            AnimationTypes.DEATH -> capability.animations[type] =
                animationList.find { it.name.contains("death") }?.name ?: ""

        }
    }

    override var renderedGltfModel: RenderedGltfModel? = null

    override var animationList: List<GLTFAnimation> = ArrayList()

}