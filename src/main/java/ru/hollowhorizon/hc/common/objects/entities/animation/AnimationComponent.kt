package ru.hollowhorizon.hc.common.objects.entities.animation

import net.minecraft.entity.Entity
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.ListNBT
import net.minecraft.nbt.StringNBT
import net.minecraftforge.common.util.Constants
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.fml.network.NetworkDirection
import net.minecraftforge.fml.network.PacketDistributor
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.models.core.animation.IPose
import ru.hollowhorizon.hc.client.models.core.animation.Pose
import ru.hollowhorizon.hc.common.network.NetworkHandler
import ru.hollowhorizon.hc.common.network.messages.EntityAnimationClientUpdatePacket
import ru.hollowhorizon.hc.common.objects.entities.IBTAnimatedEntity
import ru.hollowhorizon.hc.common.objects.entities.animation.messages.AnimationMessage
import ru.hollowhorizon.hc.common.objects.entities.animation.messages.layer.AnimationLayerMessage
import java.util.*
import java.util.function.BiConsumer


class AnimationComponent<T>(val entity: T) : INBTSerializable<CompoundNBT> where T : Entity, T : IBTAnimatedEntity<T> {
    var ticks = 0
        private set
    private val animationStates = HashMap<String, AnimationState<T>>()
    private val workFrame = Pose()
    private var lastPoseFetch = -1
    private var lastPartialTicks = 0f
    val stateStack = Stack<String>()
    private val syncQueue = ArrayList<AnimationMessage>()

    init {
        workFrame.jointCount = entity.skeleton.bones.size
    }

    fun addAnimationState(animState: AnimationState<T>) {
        animationStates[animState.name] = animState
    }

    fun removeAnimationState(name: String) {
        animationStates.remove(name)
    }

    fun startLayer(stateName: String, name: String) {
        animationStates[stateName]?.startLayer(name, ticks)
    }

    fun distributeLayerMessage(stateName: String, layerName: String, message: AnimationLayerMessage) {
        animationStates[stateName]?.consumeLayerMessage(layerName, message)
    }

    fun updateState(message: AnimationMessage) {
        if (!entity.level.isClientSide) {
            syncQueue.add(message)
        }
        messageHandlers[message.type]?.accept(this, message)
            ?: HollowCore.LOGGER.warn("AnimationMessage type {} not handled by {}", message.type, entity.toString())


    }

    fun stopLayer(stateName: String, name: String) {
        animationStates[stateName]?.stopLayer(name)
    }

    fun pushState(stateName: String) {
        if (currentStateName != INVALID_STATE) {
            animationStates[currentStateName]?.leaveState()
        }
        animationStates[stateName]?.apply {
            stateStack.push(stateName)
            enterState(ticks)
        } ?: run {
            stateStack.push(INVALID_STATE)
        }
    }

    fun popState() {
        if (currentStateName != INVALID_STATE) {
            animationStates[currentStateName]?.leaveState()
        }
        stateStack.pop()
        val newState = currentStateName
        if (newState != INVALID_STATE) {
            animationStates[newState]?.enterState(ticks) ?: run {
                stateStack.pop()
                stateStack.push(INVALID_STATE)
            }
        }
    }

    fun isSamePose(partialTicks: Float): Boolean {
        return ticks == lastPoseFetch && partialTicks == lastPartialTicks
    }

    val currentPose: IPose
        get() = getCurrentPose(0f)

    fun getCurrentPose(partialTicks: Float): IPose {
        if (isSamePose(partialTicks)) {
            return workFrame
        }
        if (currentStateName == INVALID_STATE) {
            HollowCore.LOGGER.warn("Animation for entity: {} currently in invalid state", entity.toString())
            return DEFAULT_FRAME
        }

        val state = animationStates[currentStateName]
        return if (state != null) {
            state.applyToPose(ticks, partialTicks, workFrame)
            lastPoseFetch = ticks
            lastPartialTicks = partialTicks
            workFrame
        } else {
            HollowCore.LOGGER.warn(
                "Animation for entity: {} state not found: {}",
                entity.toString(), currentStateName
            )
            DEFAULT_FRAME
        }
    }

    val currentStateName: String
        get() = if (stateStack.empty()) {
            INVALID_STATE
        } else stateStack.peek()


    fun update() {
        ticks++
        val world = entity.level
        if (!world.isClientSide) {
            if (syncQueue.size > 0) {
                val packet = EntityAnimationClientUpdatePacket().apply {
                    entityId = entity.id
                    messages = syncQueue
                }
                PacketDistributor.TRACKING_ENTITY.with { entity }
                    .send(
                        NetworkHandler.HollowCoreChannel.toVanillaPacket(packet, NetworkDirection.PLAY_TO_CLIENT)
                    )
                syncQueue.clear()
            }
        }
        animationStates[currentStateName]?.tickState(ticks)
    }

    override fun serializeNBT(): CompoundNBT {
        val tag = CompoundNBT()
        val stack = ListNBT()
        stateStack.forEach { stateName -> stack.add(StringNBT.valueOf(stateName)) }
        tag.put("stateStack", stack)
        return tag
    }

    override fun deserializeNBT(nbt: CompoundNBT) {
        if (nbt.contains("stateStack")) {
            stateStack.clear()
            val stack: ListNBT = nbt.getList("stateStack", Constants.NBT.TAG_STRING)
            stack.forEach { tag ->
                val stateName: String = tag.asString
                stateStack.push(stateName)
            }
            pushState(stateStack.pop())
        }
    }

    companion object {
        private val DEFAULT_FRAME: Pose = Pose()
        const val INVALID_STATE = "invalid"
        private val messageHandlers = HashMap<String, BiConsumer<AnimationComponent<*>, AnimationMessage>>()

        fun addMessageHandler(messageType: String, handler: BiConsumer<AnimationComponent<*>, AnimationMessage>) {
            messageHandlers[messageType] = handler
        }
    }
}