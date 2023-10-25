package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.blaze3d.platform.NativeImage
import com.mojang.math.*
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.graphics.AttributeContext
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.client.utils.toIS
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.EOFException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

fun DataInputStream.readUInt(): Int {
    val ch1 = this.read()
    val ch2 = this.read()
    val ch3 = this.read()
    val ch4 = this.read()
    if ((ch1 or ch2 or ch3 or ch4) < 0) {
        throw EOFException()
    } else {
        return (ch4 shl 24) + (ch3 shl 16) + (ch2 shl 8) + (ch1 shl 0)
    }
}

object GltfTree {

    fun parse(file: GltfFile, location: ResourceLocation, folder: (String) -> InputStream): GLTFTree {
        val buffers = parseBuffers(file, folder)
        val bufferViews = parseBufferViews(file, buffers)
        val accessors = parseAccessors(file, bufferViews)
        val textures = mutableSetOf<ResourceLocation>()
        val meshes = parseMeshes(file, accessors, location, textures, folder)
        val skins = parseSkins(file, accessors)
        val scenes = parseScenes(file, meshes, skins)
        connectNodes(skins, scenes)
        val animations = parseAnimations(file, accessors)

        return GLTFTree(file.scene ?: 0, scenes, animations, textures, file.extras)
    }

    private fun connectNodes(skins: List<Skin>, scenes: List<Scene>) {
        skins.forEach { skin ->
            skin.jointsIds.forEachIndexed { j, id ->
                fun walkNodes(): List<Node> {
                    val nodes = mutableListOf<Node>()
                    fun walk(node: Node) {
                        nodes += node
                        node.children.forEach { walk(it) }
                    }
                    scenes.flatMap { it.nodes }.forEach(::walk)
                    return nodes
                }

                skin.joints[j] = walkNodes().find { it.index == id }
                    ?: throw IllegalStateException("Node with id $id not found! Available: ${walkNodes().map { it.index }}")
            }
        }
    }

    fun parse(location: ResourceLocation): GLTFTree {
        val file = if (location.path.endsWith(".glb")) {
            val bytes = location.toIS().readBytes()
            val dataStream = DataInputStream(ByteArrayInputStream(bytes))
            val magic = dataStream.readUInt()
            val version = dataStream.readUInt()
            if (magic != GltfFile.GLB_FILE_MAGIC || version != 2) {
                throw UnsupportedOperationException("Unsupported glTF version or file format!")
            }
            dataStream.skipBytes(4)
            var chunkLength = dataStream.readUInt()
            var chunkType = dataStream.readUInt()
            if (chunkType != GltfFile.GLB_CHUNK_MAGIC_JSON) throw UnsupportedOperationException("Unexpected chunk type for json chunk: $chunkType (should be ${GltfFile.GLB_CHUNK_MAGIC_JSON}: JSON)")
            val data = ByteArray(chunkLength)
            dataStream.readFully(data)

            GltfDefinition.parse(ByteArrayInputStream(data)).apply {
                val array = ArrayList<ByteArray>()
                while (dataStream.available() > 0) {
                    chunkLength = dataStream.readUInt()
                    chunkType = dataStream.readUInt()
                    if (chunkType == GltfFile.GLB_CHUNK_MAGIC_BIN) {
                        val binBytes = ByteArray(chunkLength)
                        dataStream.readFully(binBytes)
                        array += binBytes
                    } else {
                        HollowCore.LOGGER.warn("Unexpected chunk type for bin chunk: $chunkType (should be ${GltfFile.GLB_CHUNK_MAGIC_BIN}: BIN)")
                        dataStream.skip(chunkLength.toLong())
                    }
                }
                this.binaryBuffer = array
            }
        } else GltfDefinition.parse(location.toIS())

        fun retrieveFile(path: String): InputStream {
            if (path.startsWith("data:application/octet-stream;base64,")) {
                return java.util.Base64.getDecoder().wrap(path.substring(37).byteInputStream())
            }
            if (path.startsWith("data:image/png;base64,")) {
                return java.util.Base64.getDecoder().wrap(path.substring(22).byteInputStream())
            }

            val basePath = location.path.substringBeforeLast('/', "")
            val loc = ResourceLocation(location.namespace, if (basePath.isEmpty()) path else "$basePath/$path")

            return loc.toIS()
        }

        return parse(file, location, ::retrieveFile)
    }

    private fun parseBuffers(file: GltfFile, folder: (String) -> InputStream): List<ByteArray> {
        if (file.binaryBuffer != null) return file.binaryBuffer!!

        return file.buffers.map { buff ->

            val bytes = if (buff.uri != null) {
                val uri = buff.uri
                folder(uri).readBytes()
            } else throw UnsupportedOperationException("Unsupported Empty URI")

            if (bytes.size != buff.byteLength) {
                error("Buffer byteLength, and resource size doesn't match, buffer: $buff, resource size: ${bytes.size}")
            }

            bytes
        }
    }

    private fun parseBufferViews(file: GltfFile, buffers: List<ByteArray>): List<ByteArray> {
        return file.bufferViews.map { view ->

            val buffer = buffers[view.buffer]
            val offset = view.byteOffset ?: 0
            val size = view.byteLength

            buffer.copyOfRange(offset, offset + size)
        }
    }

    private fun parseAccessors(file: GltfFile, bufferViews: List<ByteArray>): List<Buffer> {
        return file.accessors.map { accessor ->
            val viewIndex = accessor.bufferView ?: error("Unsupported Empty BufferView at accessor: $accessor")

            val buffer = bufferViews[viewIndex]
            val view = file.bufferViews[viewIndex]

            val offset = accessor.byteOffset ?: 0
            val type = GltfComponentType.fromId(accessor.componentType)

            val buff = ByteBuffer.wrap(buffer, offset, buffer.size - offset).order(ByteOrder.LITTLE_ENDIAN)
            val list: List<Any> = intoList(accessor.type, type, accessor.count, buff)

            Buffer(accessor.type, type, list, view.byteStride ?: 0, view.byteOffset ?: 0)
        }
    }

    @Suppress("UnnecessaryVariable")
    private fun intoList(
        listType: GltfType,
        componentType: GltfComponentType,
        count: Int,
        buffer: ByteBuffer,
    ): List<Any> {
        val t = componentType
        val b = buffer
        return when (listType) {
            GltfType.SCALAR -> List(count) { b.next(t) }
            GltfType.VEC2 -> List(count) { Pair(b.next(t).toFloat(), b.next(t).toFloat()) }
            GltfType.VEC3 -> List(count) { Vector3f(b.next(t).toFloat(), b.next(t).toFloat(), b.next(t).toFloat()) }
            GltfType.VEC4 -> List(count) {
                Vector4f(
                    b.next(t).toFloat(),
                    b.next(t).toFloat(),
                    b.next(t).toFloat(),
                    b.next(t).toFloat()
                )
            }

            GltfType.MAT2 -> error("Unsupported")
            GltfType.MAT3 -> List(count) {
                Matrix3f().apply {
                    load(
                        FloatBuffer.wrap(
                            floatArrayOf(
                                b.next(t).toFloat(), b.next(t).toFloat(), b.next(t).toFloat(),
                                b.next(t).toFloat(), b.next(t).toFloat(), b.next(t).toFloat(),
                                b.next(t).toFloat(), b.next(t).toFloat(), b.next(t).toFloat()
                            )
                        )
                    )
                    transpose()
                }
            }

            GltfType.MAT4 -> List(count) {
                Matrix4f(
                    floatArrayOf(
                        b.next(t).toFloat(), b.next(t).toFloat(), b.next(t).toFloat(), b.next(t).toFloat(),
                        b.next(t).toFloat(), b.next(t).toFloat(), b.next(t).toFloat(), b.next(t).toFloat(),
                        b.next(t).toFloat(), b.next(t).toFloat(), b.next(t).toFloat(), b.next(t).toFloat(),
                        b.next(t).toFloat(), b.next(t).toFloat(), b.next(t).toFloat(), b.next(t).toFloat()
                    )
                ).apply(Matrix4f::transpose)
            }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun ByteBuffer.next(type: GltfComponentType): Number {
        return when (type) {
            GltfComponentType.BYTE, GltfComponentType.UNSIGNED_BYTE -> get()
            GltfComponentType.SHORT, GltfComponentType.UNSIGNED_SHORT -> short
            GltfComponentType.UNSIGNED_INT -> int
            GltfComponentType.FLOAT -> float
        }
    }

    private fun parseSkins(file: GltfFile, accessors: List<Buffer>): List<Skin> {
        return file.skins.map { skin ->
            val inverseBindMatrices = accessors[skin.inverseBindMatrices!!].get<Matrix4f>()
            return@map Skin(skin.joints, inverseBindMatrices)
        }
    }

    private fun parseMeshes(
        file: GltfFile, accessors: List<Buffer>,
        location: ResourceLocation, textures: MutableSet<ResourceLocation>, folder: (String) -> InputStream,
    ): List<Mesh> {
        return file.meshes.map { mesh ->
            val primitives = mesh.primitives.map { prim ->

                val attr = prim.attributes.map { (k, v) ->
                    Pair(GltfAttribute.valueOf(k), accessors[v])
                }.toMap()

                val indices = prim.indices?.let { accessors[it] }
                val mode = GltfMode.fromId(prim.mode)

                val material =
                    getMaterial(file, prim.material, location, folder) ?: TextureManager.INTENTIONAL_MISSING_TEXTURE
                textures += material

                Primitive(attr, indices, mode, material)
            }

            Mesh(primitives)
        }
    }

    private fun getMaterial(
        file: GltfFile,
        mat: Int?,
        location: ResourceLocation,
        folder: (String) -> InputStream,
    ): ResourceLocation? {
        if (mat == null) return null
        val material = file.materials[mat]
        val texture = material.pbrMetallicRoughness?.baseColorTexture?.index ?: return null
        val image = file.textures[texture].source ?: return null
        val texturePath = file.images[image].uri ?: return null

        // If the texture path is a resource location, no extra processing is done
        if (texturePath.contains(':')) {
            if (!ResourceLocation.isValidResourceLocation(texturePath)) {
                if (texturePath.startsWith("data:image/png;base64,")) {
                    val decoded = folder(texturePath)
                    val dynamicTexture = DynamicTexture(NativeImage.read(decoded))
                    return Minecraft.getInstance().textureManager.register("gltf_dynamic_texture", dynamicTexture)
                        .also {
                            HollowCore.LOGGER.info("Creating texture: {}", it)
                        }
                }
                return ResourceLocation("base64:value")
            }

            return ResourceLocation(texturePath)
        }

        val relativeModelPath = location.path.substringAfter("models/")
        val localPath = relativeModelPath.substringBeforeLast('/', "")

        val finalPath = buildString {
            if (localPath.isNotEmpty()) {
                append(localPath)
                append("/")
            }
            append(texturePath.substringBeforeLast('.'))
        }

        return ResourceLocation(location.namespace, finalPath)
    }

    private fun parseScenes(file: GltfFile, meshes: List<Mesh>, skins: List<Skin>): List<Scene> {
        return file.scenes.map { scene ->
            val nodes = scene.nodes ?: emptyList()
            val parsedNodes = nodes.map { parseNode(file, it, file.nodes[it], meshes, skins) }

            Scene(parsedNodes)
        }
    }

    private fun parseNode(file: GltfFile, nodeIndex: Int, node: GltfNode, meshes: List<Mesh>, skins: List<Skin>): Node {
        val children = node.children.map { parseNode(file, it, file.nodes[it], meshes, skins) }
        val mesh = node.mesh?.let { meshes[it] }
        val skin = node.skin?.let { skins[it] }

        val transform = Transformation(
            translation = node.translation ?: Vector3f(),
            rotation = node.rotation ?: Quaternion(0.0f, 0.0f, 0.0f, 1.0f),
            scale = node.scale ?: Vector3f(1.0f, 1.0f, 1.0f),
            matrix = node.matrix ?: Matrix4f().apply(Matrix4f::setIdentity)
        )

        return Node(nodeIndex, children, transform, mesh, skin, node.name).apply {
            this.children.forEach { it.parent = this }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseChannel(
        channel: GltfAnimationChannel, samplers: List<GltfAnimationSampler>,
        accessors: List<Buffer>,
    ): Channel {

        val sampler = samplers[channel.sampler]
        val timeValues = accessors[sampler.input].data.map { (it as Number).toFloat() }

        return Channel(
            node = channel.target.node,
            path = GltfChannelPath.valueOf(channel.target.path),
            times = timeValues,
            interpolation = sampler.interpolation,
            values = accessors[sampler.output].data.toList()
        )
    }

    @Suppress("SENSELESS_COMPARISON")
    private fun parseAnimations(file: GltfFile, accessors: List<Buffer>): List<Animation> {
        return file.animations.filter { it.channels != null }.map { animation ->
            val channels = animation.channels.map { parseChannel(it, animation.samplers, accessors) }
            Animation(animation.name, channels)
        }
    }

    data class GLTFTree(
        val scene: Int,
        val scenes: List<Scene>,
        val animations: List<Animation>,
        val textures: Set<ResourceLocation>,
        val extra: Any?,
    ) {
        fun walkNodes(): List<Node> {
            val nodes = mutableListOf<Node>()
            fun walk(node: Node) {
                nodes += node
                node.children.forEach { walk(it) }
            }
            scenes.flatMap { it.nodes }.forEach(::walk)
            return nodes
        }

        fun findNodeByIndex(index: Int): Node? {
            return walkNodes().find { it.index == index }
        }
    }

    data class Scene(
        val nodes: List<Node>,
    )

    class Node(
        val index: Int,
        val children: List<Node>,
        val transform: Transformation,
        val mesh: Mesh? = null,
        val skin: Skin? = null,
        val name: String? = null,
    ) {
        val hasChanged: Boolean get() = transform.hasChanged || parent?.hasChanged == true
        private var lastMatrix = Matrix4f().apply(Matrix4f::setIdentity)
        var parent: Node? = null

        fun getMatrix() = transform.getMatrix()

        fun computeMatrix(): Matrix4f {
            if (hasChanged) {
                val matrix = parent?.computeMatrix() ?: Matrix4f().apply(Matrix4f::setIdentity)
                matrix.multiply(getMatrix())
                lastMatrix = matrix
                transform.hasChanged = false
            }
            return lastMatrix
        }

    }

    data class Skin(
        val jointsIds: List<Int>,
        val inverseBindMatrices: List<Matrix4f>,
    ) {
        fun getInverseBindMatrix(joint: Int, inverseBindMatrix: FloatArray) {
            val buffer = FloatBuffer.wrap(inverseBindMatrix)
            inverseBindMatrices[joint].store(buffer)
        }

        val joints = HashMap<Int, Node>(jointsIds.size)
    }

    data class Mesh(
        val primitives: List<Primitive>,
    )

    data class Primitive(
        val attributes: Map<GltfAttribute, Buffer>,
        val indices: Buffer? = null,
        val mode: GltfMode,
        val material: ResourceLocation,
    ) {
        fun AttributeContext.writeToVao() {
            indices(this@Primitive.indices?.get<Int>()?.toIntArray() ?: return)
            attributes[GltfAttribute.POSITION]?.getAsFloatArray()?.let {
                "position"(it, AttributeContext.Type.VEC3)
            }
            attributes[GltfAttribute.NORMAL]?.getAsFloatArray()?.let {
                "normal"(it, AttributeContext.Type.VEC3)
            }
            attributes[GltfAttribute.TEXCOORD_0]?.getAsIntArray()?.let {
                "texcoord"(it, AttributeContext.Type.VEC2)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    data class Buffer(
        val type: GltfType,
        val componentType: GltfComponentType,
        val data: List<Any>,
        val byteStride: Int,
        val byteOffset: Int,
    ) {
        fun <T> get() = data as List<T>
        fun getAsFloatArray() = (data as List<Float>).toFloatArray();
        fun getAsIntArray() = (data as List<Int>).toIntArray();

        fun <T> get(index: Int) = data[index] as T

        fun buffer(): ByteBuffer {
            val buffer = ByteBuffer.allocate(data.size * componentType.size)
            when (componentType) {
                GltfComponentType.BYTE -> get<Byte>().forEach(buffer::put)
                GltfComponentType.UNSIGNED_BYTE -> get<Byte>().forEach(buffer::put)
                GltfComponentType.SHORT -> get<Short>().forEach(buffer::putShort)
                GltfComponentType.UNSIGNED_SHORT -> get<Short>().forEach(buffer::putShort)
                GltfComponentType.UNSIGNED_INT -> get<Int>().forEach(buffer::putInt)
                GltfComponentType.FLOAT -> get<Float>().forEach(buffer::putFloat)
            }
            buffer.rewind()
            return buffer
        }
    }

    data class Animation(
        val name: String?,
        val channels: List<Channel>,
    )

    data class Channel(
        val node: Int,
        val path: GltfChannelPath,
        val times: List<Float>,
        val interpolation: GltfInterpolation,
        val values: List<Any>,
    )
}

fun main() {
    val tree = GltfTree.parse("hc:models/entity/hilda_regular.glb".rl)

    println(tree)
}