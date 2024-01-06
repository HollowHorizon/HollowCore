package ru.hollowhorizon.hc.client.render.particles

import com.mojang.math.Vector3d
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleType
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraftforge.registries.ForgeRegistries
import ru.hollowhorizon.hc.client.utils.math.Interpolation
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.network.NetworkHandler
import ru.hollowhorizon.hc.common.network.packets.SpawnParticlesPacket
import java.util.function.Supplier
import kotlin.math.cos
import kotlin.math.sin


open class HollowParticleBuilder protected constructor(val level: Level, val type: ParticleType<*>) {
    val options: HollowParticleOptions = HollowParticleOptions(type)
    var xMotion = 0.0
    var yMotion = 0.0
    var zMotion = 0.0
    var maxXSpeed = 0.0
    var maxYSpeed = 0.0
    var maxZSpeed = 0.0
    var maxXOffset = 0.0
    var maxYOffset = 0.0
    var maxZOffset = 0.0

    fun color(
        r1: Float,
        g1: Float,
        b1: Float,
        r2: Float,
        g2: Float,
        b2: Float,
        colorCoefficient: Float = 1.0f,
        colorCurveEasing: Interpolation = Interpolation.LINEAR,
    ): HollowParticleBuilder {
        options.colorData = ParticleColor(r1, g1, b1, r2, g2, b2, colorCoefficient, colorCurveEasing)
        return this
    }

    fun scale(
        startingValue: Float,
        middleValue: Float,
        endingValue: Float = -1f,
        coefficient: Float = 1.0f,
        startToMiddleEasing: Interpolation = Interpolation.LINEAR,
        middleToEndEasing: Interpolation = Interpolation.LINEAR,
    ): HollowParticleBuilder {
        options.scaleData =
            GenericData(startingValue, middleValue, endingValue, coefficient, startToMiddleEasing, middleToEndEasing)
        return this
    }

    fun transparency(
        startingValue: Float,
        middleValue: Float,
        endingValue: Float = -1f,
        coefficient: Float = 1.0f,
        startToMiddleEasing: Interpolation = Interpolation.LINEAR,
        middleToEndEasing: Interpolation = Interpolation.LINEAR,
    ): HollowParticleBuilder {
        options.transparencyData =
            GenericData(startingValue, middleValue, endingValue, coefficient, startToMiddleEasing, middleToEndEasing)
        return this
    }

    fun spin(
        startingValue: Float,
        middleValue: Float,
        endingValue: Float = -1f,
        coefficient: Float = 1.0f,
        startToMiddleEasing: Interpolation = Interpolation.LINEAR,
        middleToEndEasing: Interpolation = Interpolation.LINEAR,
    ): HollowParticleBuilder {
        options.spinData =
            GenericData(startingValue, middleValue, endingValue, coefficient, startToMiddleEasing, middleToEndEasing)
        return this
    }

    var gravity: Float
        get() = options.gravity
        set(value) {
            options.gravity = value
        }
    var noClip: Boolean
        get() = options.noClip
        set(value) {
            options.noClip = value
        }

    var spritePicker: SpritePicker
        get() = options.spritePicker
        set(value) {
            options.spritePicker = value
        }

    var discardType: DiscardType
        get() = options.discardType
        set(value) {
            options.discardType = value
        }

    var lifetime: Int
        get() = options.lifetime
        set(value) {
            options.lifetime = value
        }

    fun randomMotion(maxSpeed: Double): HollowParticleBuilder {
        return randomMotion(maxSpeed, maxSpeed, maxSpeed)
    }

    fun randomMotion(maxHSpeed: Double, maxVSpeed: Double): HollowParticleBuilder {
        return randomMotion(maxHSpeed, maxVSpeed, maxHSpeed)
    }

    fun randomMotion(maxXSpeed: Double, maxYSpeed: Double, maxZSpeed: Double): HollowParticleBuilder {
        this.maxXSpeed = maxXSpeed
        this.maxYSpeed = maxYSpeed
        this.maxZSpeed = maxZSpeed
        return this
    }

    fun addMotion(vx: Double, vy: Double, vz: Double): HollowParticleBuilder {
        xMotion += vx
        yMotion += vy
        zMotion += vz
        return this
    }

    fun setMotion(vx: Double, vy: Double, vz: Double): HollowParticleBuilder {
        xMotion = vx
        yMotion = vy
        zMotion = vz
        return this
    }

    fun randomOffset(maxDistance: Double): HollowParticleBuilder {
        return randomOffset(maxDistance, maxDistance, maxDistance)
    }

    fun randomOffset(maxHDist: Double, maxVDist: Double): HollowParticleBuilder {
        return randomOffset(maxHDist, maxVDist, maxHDist)
    }

    fun randomOffset(maxXDist: Double, maxYDist: Double, maxZDist: Double): HollowParticleBuilder {
        maxXOffset = maxXDist
        maxYOffset = maxYDist
        maxZOffset = maxZDist
        return this
    }

    fun spawn(x: Double, y: Double, z: Double): HollowParticleBuilder {
        val yaw: Double = RANDOM.nextFloat().toDouble() * Math.PI * 2.0
        val pitch: Double = RANDOM.nextFloat().toDouble() * Math.PI - 1.5707963267948966
        val xSpeed: Double = RANDOM.nextFloat().toDouble() * maxXSpeed
        val ySpeed: Double = RANDOM.nextFloat().toDouble() * maxYSpeed
        val zSpeed: Double = RANDOM.nextFloat().toDouble() * maxZSpeed
        xMotion += sin(yaw) * cos(pitch) * xSpeed
        yMotion += sin(pitch) * ySpeed
        zMotion += cos(yaw) * cos(pitch) * zSpeed
        val yaw2: Double = RANDOM.nextFloat().toDouble() * Math.PI * 2.0
        val pitch2: Double = RANDOM.nextFloat().toDouble() * Math.PI - 1.5707963267948966
        val xDist: Double = RANDOM.nextFloat().toDouble() * maxXOffset
        val yDist: Double = RANDOM.nextFloat().toDouble() * maxYOffset
        val zDist: Double = RANDOM.nextFloat().toDouble() * maxZOffset
        val xPos = sin(yaw2) * cos(pitch2) * xDist
        val yPos = sin(pitch2) * yDist
        val zPos = cos(yaw2) * cos(pitch2) * zDist
        NetworkHandler.sendMessageToClientTrackingChunk(
            SpawnParticlesPacket(options, x + xPos, y + yPos, z + zPos, xMotion, yMotion, zMotion),
            level, BlockPos((x + xPos).toInt(), (y + yPos).toInt(), (z + zPos).toInt())
        )
        return this
    }

    fun repeat(x: Double, y: Double, z: Double, n: Int): HollowParticleBuilder {
        for (i in 0 until n) {
            spawn(x, y, z)
        }
        return this
    }

    fun surroundBlock(pos: BlockPos, vararg dirs: Direction): HollowParticleBuilder {
        var directions = dirs
        if (directions.isEmpty()) directions = Direction.values()
        for (direction in directions) {
            val yaw: Double = RANDOM.nextFloat().toDouble() * Math.PI * 2.0
            val pitch: Double =
                RANDOM.nextFloat().toDouble() * Math.PI - 1.5707963267948966
            val xSpeed: Double = RANDOM.nextFloat().toDouble() * maxXSpeed
            val ySpeed: Double = RANDOM.nextFloat().toDouble() * maxYSpeed
            val zSpeed: Double = RANDOM.nextFloat().toDouble() * maxZSpeed
            xMotion += sin(yaw) * cos(pitch) * xSpeed
            yMotion += sin(pitch) * ySpeed
            zMotion += cos(yaw) * cos(pitch) * zSpeed
            val axis = direction.axis
            val d0 = 0.5625
            val xPos =
                if (axis == Direction.Axis.X) 0.5 + d0 * direction.stepX.toDouble() else RANDOM.nextDouble()
            val yPos =
                if (axis == Direction.Axis.Y) 0.5 + d0 * direction.stepY.toDouble() else RANDOM.nextDouble()
            val zPos =
                if (axis == Direction.Axis.Z) 0.5 + d0 * direction.stepZ.toDouble() else RANDOM.nextDouble()

            NetworkHandler.sendMessageToClientTrackingChunk(
                SpawnParticlesPacket(options, pos.x + xPos, pos.y + yPos, pos.z + zPos, xMotion, yMotion, zMotion),
                level, BlockPos((pos.x + xPos).toInt(), (pos.y + yPos).toInt(), (pos.z + zPos).toInt())
            )
        }
        return this
    }

    fun repeatSurroundBlock(pos: BlockPos, n: Int): HollowParticleBuilder {
        for (i in 0 until n) {
            surroundBlock(pos)
        }
        return this
    }

    fun repeatSurroundBlock(pos: BlockPos, n: Int, vararg directions: Direction): HollowParticleBuilder {
        for (i in 0 until n) {
            surroundBlock(pos, *directions)
        }
        return this
    }

    fun surroundVoxelShape(pos: BlockPos, voxelShape: VoxelShape, max: Int): HollowParticleBuilder {
        val c = IntArray(1)
        val perBoxMax = max / voxelShape.toAabbs().size
        val r = Supplier {
            c[0]++
            if (c[0] >= perBoxMax) {
                c[0] = 0
                return@Supplier true
            } else {
                return@Supplier false
            }
        }
        val v = Vec3(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
        voxelShape.forAllBoxes { x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double ->
            val b = v.add(x1, y1, z1)
            val e = v.add(x2, y2, z2)
            val runs = ArrayList<() -> Unit>()
            runs += { spawnLine(b, v.add(x1, y2, z1)) }
            runs += { spawnLine(b, v.add(x2, y1, z1)) }
            runs += { spawnLine(b, v.add(x1, y1, z2)) }
            runs += { spawnLine(v.add(x1, y2, z1), v.add(x2, y2, z1)) }
            runs += { spawnLine(v.add(x1, y2, z1), v.add(x1, y2, z2)) }
            runs += { spawnLine(e, v.add(x2, y2, z1)) }
            runs += { spawnLine(e, v.add(x1, y2, z2)) }
            runs += { spawnLine(e, v.add(x2, y1, z2)) }
            runs += { spawnLine(v.add(x2, y1, z1), v.add(x2, y1, z2)) }
            runs += { spawnLine(v.add(x1, y1, z2), v.add(x2, y1, z2)) }
            runs += { spawnLine(v.add(x2, y1, z1), v.add(x2, y2, z1)) }
            runs += { spawnLine(v.add(x1, y1, z2), v.add(x1, y2, z2)) }
            runs.shuffle()
            for (runnable in runs) {
                runnable()
                if (r.get()) {
                    break
                }
            }
        }
        return this
    }

    fun surroundVoxelShape(pos: BlockPos, state: BlockState, max: Int): HollowParticleBuilder {
        var voxelShape = state.getShape(level, pos)
        if (voxelShape.isEmpty) voxelShape = Shapes.block()
        return this.surroundVoxelShape(pos, voxelShape, max)
    }

    fun spawnAtRandomFace(pos: BlockPos): HollowParticleBuilder {
        val direction = Direction.values().random()
        val yaw: Double = RANDOM.nextFloat().toDouble() * Math.PI * 2.0
        val pitch: Double = RANDOM.nextFloat().toDouble() * Math.PI - 1.5707963267948966
        val xSpeed: Double = RANDOM.nextFloat().toDouble() * maxXSpeed
        val ySpeed: Double = RANDOM.nextFloat().toDouble() * maxYSpeed
        val zSpeed: Double = RANDOM.nextFloat().toDouble() * maxZSpeed
        xMotion += sin(yaw) * cos(pitch) * xSpeed
        yMotion += sin(pitch) * ySpeed
        zMotion += cos(yaw) * cos(pitch) * zSpeed
        val axis = direction.axis
        val d0 = 0.5625
        val xPos =
            if (axis === Direction.Axis.X) 0.5 + d0 * direction.stepX.toDouble() else RANDOM.nextDouble()
        val yPos =
            if (axis === Direction.Axis.Y) 0.5 + d0 * direction.stepY.toDouble() else RANDOM.nextDouble()
        val zPos =
            if (axis === Direction.Axis.Z) 0.5 + d0 * direction.stepZ.toDouble() else RANDOM.nextDouble()

        NetworkHandler.sendMessageToClientTrackingChunk(
            SpawnParticlesPacket(options, pos.x + xPos, pos.y + yPos, pos.z + zPos, xMotion, yMotion, zMotion),
            level, BlockPos((pos.x + xPos).toInt(), (pos.y + yPos).toInt(), (pos.z + zPos).toInt())
        )
        return this
    }

    fun repeatRandomFace(pos: BlockPos, n: Int): HollowParticleBuilder {
        for (i in 0 until n) {
            spawnAtRandomFace(pos)
        }
        return this
    }

    fun createCircle(
        x: Double,
        y: Double,
        z: Double,
        distance: Double,
        currentCount: Double,
        totalCount: Double,
    ): HollowParticleBuilder {
        val xSpeed: Double = RANDOM.nextFloat().toDouble() * maxXSpeed
        val ySpeed: Double = RANDOM.nextFloat().toDouble() * maxYSpeed
        val zSpeed: Double = RANDOM.nextFloat().toDouble() * maxZSpeed
        val theta = 6.283185307179586 / totalCount
        val finalAngle = currentCount / totalCount + theta * currentCount
        val dx2 = distance * cos(finalAngle)
        val dz2 = distance * sin(finalAngle)
        val vector2f = Vector3d(dx2, 0.0, dz2)
        xMotion = vector2f.x * xSpeed
        zMotion = vector2f.z * zSpeed
        val yaw2: Double = RANDOM.nextFloat().toDouble() * Math.PI * 2.0
        val pitch2: Double = RANDOM.nextFloat().toDouble() * Math.PI - 1.5707963267948966
        val xDist: Double = RANDOM.nextFloat().toDouble() * maxXOffset
        val yDist: Double = RANDOM.nextFloat().toDouble() * maxYOffset
        val zDist: Double = RANDOM.nextFloat().toDouble() * maxZOffset
        val xPos = sin(yaw2) * cos(pitch2) * xDist
        val yPos = sin(pitch2) * yDist
        val zPos = cos(yaw2) * cos(pitch2) * zDist
        NetworkHandler.sendMessageToClientTrackingChunk(
            SpawnParticlesPacket(options, x + xPos + dx2, y + yPos, z + zPos + dz2, xMotion, yMotion, zMotion),
            level, BlockPos((x + xPos + dx2).toInt(), (y + yPos).toInt(), (z + zPos + dz2).toInt())
        )
        return this
    }

    fun repeatCircle(
        x: Double,
        y: Double,
        z: Double,
        distance: Double,
        times: Int,
    ): HollowParticleBuilder {
        for (i in 0 until times) {
            createCircle(x, y, z, distance, i.toDouble(), times.toDouble())
        }
        return this
    }

    fun createBlockOutline(pos: BlockPos, state: BlockState): HollowParticleBuilder {
        val voxelShape = state.getShape(level, pos)
        voxelShape.forAllBoxes { x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double ->
            val v = Vec3(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
            val b = Vec3(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()).add(x1, y1, z1)
            val e = Vec3(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()).add(x2, y2, z2)
            spawnLine(b, v.add(x2, y1, z1))
            spawnLine(b, v.add(x1, y2, z1))
            spawnLine(b, v.add(x1, y1, z2))
            spawnLine(v.add(x1, y2, z1), v.add(x2, y2, z1))
            spawnLine(v.add(x1, y2, z1), v.add(x1, y2, z2))
            spawnLine(e, v.add(x2, y2, z1))
            spawnLine(e, v.add(x1, y2, z2))
            spawnLine(e, v.add(x2, y1, z2))
            spawnLine(v.add(x2, y1, z1), v.add(x2, y1, z2))
            spawnLine(v.add(x1, y1, z2), v.add(x2, y1, z2))
            spawnLine(v.add(x2, y1, z1), v.add(x2, y2, z1))
            spawnLine(v.add(x1, y1, z2), v.add(x1, y2, z2))
        }
        return this
    }

    fun spawnLine(one: Vec3, two: Vec3): HollowParticleBuilder {
        val yaw: Double = RANDOM.nextFloat().toDouble() * Math.PI * 2.0
        val pitch: Double = RANDOM.nextFloat().toDouble() * Math.PI - 1.5707963267948966
        val xSpeed: Double = RANDOM.nextFloat().toDouble() * maxXSpeed
        val ySpeed: Double = RANDOM.nextFloat().toDouble() * maxYSpeed
        val zSpeed: Double = RANDOM.nextFloat().toDouble() * maxZSpeed
        xMotion += sin(yaw) * cos(pitch) * xSpeed
        yMotion += sin(pitch) * ySpeed
        zMotion += cos(yaw) * cos(pitch) * zSpeed
        val pos = one.lerp(two, RANDOM.nextDouble())
        NetworkHandler.sendMessageToClientTrackingChunk(
            SpawnParticlesPacket(options, pos.x, pos.y, pos.z, xMotion, yMotion, zMotion),
            level, BlockPos(pos)
        )
        return this
    }

    companion object {
        private val RANDOM = RandomSource.create()
        fun create(level: Level, type: ParticleType<*>, builder: HollowParticleBuilder.() -> Unit = {}): HollowParticleBuilder {
            return HollowParticleBuilder(level, type).apply(builder)
        }

        fun create(level: Level, location: String, builder: HollowParticleBuilder.() -> Unit = {}): HollowParticleBuilder {
            return HollowParticleBuilder(level, ForgeRegistries.PARTICLE_TYPES.getValue(location.rl)!!).apply(builder)
        }
    }
}
