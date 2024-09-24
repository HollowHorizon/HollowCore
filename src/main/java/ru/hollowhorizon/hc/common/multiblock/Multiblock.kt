package ru.hollowhorizon.hc.common.multiblock

//? if >=1.20.1 {
/*import net.minecraft.core.registries.Registries
import net.minecraft.core.registries.BuiltInRegistries
*///?} else {
import net.minecraft.data.BuiltinRegistries
import net.minecraft.core.Registry
//?}

import ru.hollowhorizon.hc.client.utils.registryAccess
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.tags.TagKey
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.ColorResolver
import net.minecraft.world.level.Level
import net.minecraft.world.level.LightLayer
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.lighting.LevelLightEngine
import net.minecraft.world.level.material.FluidState
import ru.hollowhorizon.hc.client.utils.registryAccess


class Multiblock(block: Multiblock.() -> Unit) : BlockAndTintGetter {
    private val tileEntities = hashMapOf<BlockPos, BlockEntity>()
    var xSize: Int = 0
    var ySize: Int = 0
    var zSize: Int = 0
    val blocks = ArrayList<Matcher>()

    init {
        block()
    }

    fun size(xSize: Int, zSize: Int, ySize: Int) {
        this.xSize = xSize
        this.zSize = zSize
        this.ySize = ySize
    }

    fun pattern(vararg blocks: Matcher?) {
        assert(blocks.size != xSize * ySize * zSize) { "Blocks must have the same size" }

        this.blocks.clear()
        this.blocks.addAll(blocks.map { it ?: block(Blocks.AIR.defaultBlockState()) })
    }

    fun isValid(level: Level, basePos: BlockPos) =
        listOf(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST).any {
            checkStructureForDirection(level, basePos, it)
        }

    private fun checkStructureForDirection(level: Level, basePos: BlockPos, direction: Direction): Boolean {
        // Проверяем все возможные начальные позиции в рамках структуры
        for (offsetX in 0..<xSize) {
            for (offsetY in 0..<ySize) {
                for (offsetZ in 0..<zSize) {
                    if (checkFromBase(level, basePos, direction, offsetX, offsetY, offsetZ)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun checkFromBase(
        level: Level,
        basePos: BlockPos,
        direction: Direction,
        startX: Int,
        startY: Int,
        startZ: Int,
    ): Boolean {
        for (y in 0..<ySize) {
            for (z in 0..<zSize) {
                for (x in 0..<xSize) {
                    val expectedBlock = blocks[x + z * zSize + y * zSize * xSize]
                    if (expectedBlock.default().isAir) continue

                    // Смещаем блоки относительно выбранной стартовой точки
                    val rotatedPos = getRotatedPos(basePos, x - startX, y - startY, z - startZ, direction)
                    val currentBlock = level.getBlockState(rotatedPos)

                    if (!expectedBlock.matches(currentBlock)) return false
                }
            }
        }
        return true
    }

    private fun getRotatedPos(basePos: BlockPos, x: Int, y: Int, z: Int, direction: Direction): BlockPos {
        return when (direction) {
            Direction.NORTH -> basePos.offset(x, y, -z)
            Direction.SOUTH -> basePos.offset(-x, y, z) // Вращение на 180 градусов
            Direction.WEST -> basePos.offset(-z, y, -x)   // Поворот на 90 градусов против часовой стрелки
            Direction.EAST -> basePos.offset(z, y, x)   // Поворот на 90 градусов по часовой стрелке
            else -> basePos
        }
    }

    override fun getHeight(): Int {
        return ySize
    }

    override fun getMinBuildHeight(): Int {
        return 0
    }

    override fun getBrightness(type: LightLayer, pos: BlockPos) = 14


    override fun getRawBrightness(pos: BlockPos, ambientDarkening: Int) = 15 - ambientDarkening


    override fun getBlockEntity(pos: BlockPos): BlockEntity? {
        val state = getBlockState(pos)
        if (state.block is EntityBlock) {
            return tileEntities.computeIfAbsent(pos.immutable()) { p ->
                (state.block as EntityBlock).newBlockEntity(
                    pos,
                    state
                ) ?: throw IllegalStateException("Block does not have a BlockEntity")
            }
        }
        return null
    }

    override fun getBlockState(pos: BlockPos): BlockState {
        val id = pos.x + pos.z * zSize + pos.y * zSize * xSize
        if (id !in 0..<blocks.size) return Blocks.LIGHT.defaultBlockState()
        return blocks[id].default()
    }

    override fun getFluidState(pos: BlockPos): FluidState = getBlockState(pos).fluidState

    override fun getShade(direction: Direction, shade: Boolean): Float {
        return 1f
    }

    override fun getLightEngine(): LevelLightEngine? {
        return null
    }

    override fun getBlockTint(pos: BlockPos, color: ColorResolver): Int {
        //? if >=1.20.1 {
        /*return color.getColor(
            registryAccess.registry(Registries.BIOME).get().getOrThrow(Biomes.PLAINS),
            pos.x.toDouble(),
            pos.z.toDouble()
        )
        *///?} else {
        return color.getColor(BuiltinRegistries.BIOME.getOrThrow(Biomes.PLAINS), pos.x.toDouble(), pos.z.toDouble())
        //?}
    }

    interface Matcher {
        fun matches(block: BlockState): Boolean

        fun default(): BlockState
    }

    fun block(state: BlockState) = object : Matcher {
        override fun matches(block: BlockState): Boolean {
            return block == state
        }

        override fun default() = state
    }

    fun tag(tag: TagKey<Block>) = object : Matcher {
        override fun matches(block: BlockState) = block.`is`(tag)

        override fun default(): BlockState {
            //? if >=1.20.1 {
            /*return BuiltInRegistries.BLOCK
            *///?} else {
            return registryAccess.registry(Registry.BLOCK_REGISTRY).get()
            //?}
                .getTag(tag).get().firstOrNull()?.value()
                ?.defaultBlockState() ?: Blocks.AIR.defaultBlockState()
        }
    }
}