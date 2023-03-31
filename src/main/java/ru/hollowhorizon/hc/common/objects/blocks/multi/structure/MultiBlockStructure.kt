package ru.hollowhorizon.hc.common.objects.blocks.multi.structure

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.HorizontalFaceBlock
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.util.math.vector.Vector3i
import net.minecraft.world.World
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.common.objects.blocks.multi.entity.MultiCoreBlockEntity
import ru.hollowhorizon.hc.common.objects.blocks.multi.entity.MultiModuleBlockEntity

class MultiBlockStructure(val structure: HashMap<BlockPos, Block>, val config: MultiBlockBuilder.Config) {
    fun tryBuild(level: World, pos: BlockPos, front: Direction, state: BlockState): Boolean {
        if (!this.hasBlock(level.getBlockState(pos).block)) return false

        if (canPlaceAt(level, pos, front)) {
            place(level, pos, front, state)
            return true
        } else {
            //Получился какой-то говнокод, но я не знаю как сделать лучше
            if (canPlaceAt(level, pos, Direction.NORTH)) {
                place(level, pos, Direction.NORTH, state)
                return true
            } else if (canPlaceAt(level, pos, Direction.EAST)) {
                place(level, pos, Direction.EAST, state)
                return true
            } else if (canPlaceAt(level, pos, Direction.SOUTH)) {
                place(level, pos, Direction.SOUTH, state)
                return true
            } else if (canPlaceAt(level, pos, Direction.WEST)) {
                place(level, pos, Direction.WEST, state)
                return true
            }
        }

        return false
    }

    private fun place(level: World, pos: BlockPos, front: Direction, state: BlockState) {
        val cornerPos = getCorner(level, pos, front)

        level.setBlockAndUpdate(
            cornerPos, state.setValue(
                HorizontalFaceBlock.FACING, front
            )
        )

        forEachBlock(cornerPos, front) { modulePos ->
            level.setBlockAndUpdate(modulePos, state)

            level.getBlockEntity(modulePos)?.let { (it as MultiModuleBlockEntity).corePos = cornerPos }
            level.getBlockEntity(cornerPos)?.let { (it as MultiCoreBlockEntity).modules.add(modulePos) }
        }

        level.getBlockEntity(cornerPos)?.let {
            val core = it as MultiCoreBlockEntity;
            core.isDestroying = false
            core.name = config.modelName
            core.offset = config.offset
            core.onOpen = config.onOpen
            core.setChanged()
        }
    }

    fun getCorner(level: World, pos: BlockPos, front: Direction): BlockPos {
        val offset = OffsetBlockPos(getWidth(), getHeight(), getDepth(), front)

        while (true) {
            if (checkStructure(level, pos.offset(offset.pos()), front)) {
                return pos.offset(offset.pos())
            }

            if (!offset.next()) break
        }

        HollowCore.LOGGER.warn("Can't find corner of structure at $pos")
        return pos
    }

    fun canPlaceAt(level: World, pos: BlockPos, front: Direction): Boolean {
        val offset = OffsetBlockPos(getWidth(), getHeight(), getDepth(), front)

        while (true) {
            if (checkStructure(level, pos.offset(offset.pos()), front)) {
                return true
            }

            if (!offset.next()) break
        }

        return false
    }

    fun checkStructure(level: World, cornerPos: BlockPos, facing: Direction): Boolean {
        when (facing) {
            Direction.EAST -> {
                for ((blockPos, block) in structure) {
                    val checkBlock = level.getBlockState(blockPos.offset(cornerPos)).block
                    if (checkBlock != block) return false
                }
            }

            Direction.WEST -> {
                for ((blockPos, block) in structure) {
                    val checkPos = cornerPos.offset(-blockPos.x, blockPos.y, -blockPos.z)

                    val checkBlock = level.getBlockState(checkPos).block
                    if (checkBlock != block) return false
                }
            }

            Direction.NORTH -> {
                for ((blockPos, block) in structure) {
                    val checkPos = cornerPos.offset(blockPos.z, blockPos.y, -blockPos.x)

                    val checkBlock = level.getBlockState(checkPos).block
                    if (checkBlock != block) return false
                }
            }

            Direction.SOUTH -> {
                for ((blockPos, block) in structure) {
                    val checkPos = cornerPos.offset(-blockPos.z, blockPos.y, blockPos.x)

                    val checkBlock = level.getBlockState(checkPos).block
                    if (checkBlock != block) return false
                }
            }

            else -> return false
        }
        return true
    }

    fun hasBlock(pos: Block): Boolean {
        return structure.containsValue(pos)
    }

    fun getWidth(): Int {
        var maxX = 0

        for (pos in structure.keys) {
            if (pos.x > maxX) maxX = pos.x
        }

        return maxX
    }

    fun getHeight(): Int {
        var maxY = 0

        for (pos in structure.keys) {
            if (pos.y > maxY) maxY = pos.y
        }

        return maxY
    }

    fun getDepth(): Int {
        var maxZ = 0

        for (pos in structure.keys) {
            if (pos.z > maxZ) maxZ = pos.z
        }

        return maxZ
    }

    fun forEachBlock(cornerPos: BlockPos, direction: Direction, action: (BlockPos) -> Unit) {
        when (direction) {
            Direction.EAST -> {
                for (blockPos in structure.keys) {
                    val pos = blockPos.offset(cornerPos)
                    if (pos == cornerPos || structure[pos] == Blocks.AIR) continue

                    action(pos)
                }
            }

            Direction.WEST -> {
                for (blockPos in structure.keys) {
                    val pos = cornerPos.offset(-blockPos.x, blockPos.y, -blockPos.z)
                    if (pos == cornerPos || structure[pos] == Blocks.AIR) continue

                    action(pos)
                }
            }

            Direction.NORTH -> {
                for (blockPos in structure.keys) {
                    val pos = cornerPos.offset(blockPos.z, blockPos.y, -blockPos.x)
                    if (pos == cornerPos || structure[pos] == Blocks.AIR) continue

                    action(pos)
                }
            }

            Direction.SOUTH -> {
                for (blockPos in structure.keys) {
                    val pos = cornerPos.offset(-blockPos.z, blockPos.y, blockPos.x)
                    if (pos == cornerPos || structure[pos] == Blocks.AIR) continue

                    action(pos)
                }
            }

            else -> {
                HollowCore.LOGGER.warn("Can't iterate structure with direction $direction")
            }
        }
    }

    override fun toString(): String {
        val builder: StringBuilder = StringBuilder()
        builder.append("\n")

        for (y in 0..getHeight()) {
            for (z in 0..getDepth()) {
                for (x in 0..getWidth()) {
                    val pos = BlockPos(x, y, z)
                    val block = structure[pos]

                    if (block != null) {
                        builder.append(block.name.string + "\t")
                    } else {
                        builder.append("_\t")
                    }
                }

                builder.append("\n")
            }

            builder.append("\n")
        }

        return builder.toString()
    }
}

object MultiBlockStorage {
    val multiBlockActions: HashMap<String, (PlayerEntity, BlockPos) -> Unit> = HashMap()
}

private fun Vector3i.north(): Vector3i {
    return Vector3i(this.x, this.y, -this.z)
}

private fun Vector3i.south(): Vector3i {
    return Vector3i(-this.x, this.y, this.z)
}

private operator fun Vector3i.unaryMinus(): Vector3i {
    return Vector3i(-this.x, -this.y, -this.z)
}

class OffsetBlockPos(val maxX: Int, val maxY: Int, val maxZ: Int, val direction: Direction) {
    var x: Int = 0
    var y: Int = 0
    var z: Int = 0

    fun next(): Boolean {
        when (direction) {
            Direction.EAST -> {
                x--
                if (-x > maxX) {
                    x = 0
                    y--
                    if (-y > maxY) {
                        y = 0
                        z--
                        if (-z > maxZ) {
                            return false
                        }
                    }
                }
                return true
            }

            Direction.WEST -> {
                x++
                if (x > maxX) {
                    x = 0
                    y--
                    if (-y > maxY) {
                        y = 0
                        z++
                        if (z > maxZ) {
                            return false
                        }
                    }
                }
                return true
            }

            Direction.NORTH -> {
                x--
                if (-x > maxZ) {
                    x = 0
                    y--
                    if (-y > maxY) {
                        y = 0
                        z++
                        if (z > maxX) {
                            return false
                        }
                    }
                }
                return true
            }

            Direction.SOUTH -> {
                x++
                if (x > maxZ) {
                    x = 0
                    y--
                    if (-y > maxY) {
                        y = 0
                        z--
                        if (-z > maxX) {
                            return false
                        }
                    }
                }
                return true
            }

            else -> return false
        }
    }

    fun pos(): Vector3i {
        return Vector3i(x, y, z)
    }
}

class MultiBlockBuilder {
    private lateinit var config: Config
    private val layers = ArrayList<Layer.() -> Unit>()
    private val structure = HashMap<BlockPos, Block>()
    var yIndex = 0

    fun build(): MultiBlockStructure {
        for (layer in layers.reversed()) {
            val layerImpl = Layer(yIndex);
            layer.invoke(layerImpl)
            structure.putAll(layerImpl.layer)

            yIndex++
        }
        MultiBlockStorage.multiBlockActions[config.modelName] = config.onOpen
        return MultiBlockStructure(structure, config)
    }

    fun layer(function: Layer.() -> Unit): MultiBlockBuilder {
        this.layers.add(function)
        return this
    }

    fun configure(config: Config.() -> Unit): MultiBlockBuilder {
        this.config = Config().apply(config)
        return this
    }

    class Config {
        var modelName = ""
        var offset = Vector3d(0.0, 0.0, 0.0)
        var onOpen: (PlayerEntity, BlockPos) -> Unit = { player, blockPos -> }
    }

    class Layer(val yIndex: Int) {
        val layer = HashMap<BlockPos, Block>()
        var zIndex = 0

        fun line(vararg blocks: Block) {
            for (i in blocks.indices) {
                layer[BlockPos(zIndex, yIndex, i)] = blocks[i]
            }
            zIndex++
        }
    }
}