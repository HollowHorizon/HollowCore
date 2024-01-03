package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.*
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.resources.ResourceLocation
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL33
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.HollowCore.MODID
import ru.hollowhorizon.hc.client.models.gltf.manager.GltfManager
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.client.utils.toIS
import ru.hollowhorizon.hc.client.utils.use
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.EOFException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.util.*


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
    val TEXTURE_MAP = HashMap<ResourceLocation, DynamicTexture>()


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

        if (texturePath.contains(':')) {
            if (!ResourceLocation.isValidResourceLocation(texturePath)) {
                if (texturePath.startsWith("data:image/png;base64,")) {
                    val decoded = folder(texturePath)
                    val dynamicTexture = DynamicTexture(NativeImage.read(decoded))
                    var textureName = file.textures[texture].name?.substringBefore(".png")
                        ?: "" //Название изначально может быть пустым, а не только null, так что строчка ниже не просто так
                    if (textureName.isEmpty()) textureName = "gltf_texture_${location.path.replace("/", ".")}_$texture"
                    val textureLocation = ResourceLocation(MODID, textureName)

                    if (!TEXTURE_MAP.contains(textureLocation)) {
                        TEXTURE_MAP[textureLocation] = dynamicTexture
                        Minecraft.getInstance().textureManager.register(textureLocation, dynamicTexture)
                    }

                    return textureLocation
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
    ) {
        fun render(
            stack: PoseStack,
            nodeRenderer: NodeRenderer,
            data: ModelData,
            consumer: (ResourceLocation) -> RenderType,
            light: Int,
            overlay: Int
        ) {
            nodes.forEach { it.render(stack, nodeRenderer, data, consumer, light, overlay) }
        }
    }

    class Node(
        val index: Int,
        val children: List<Node>,
        val transform: Transformation,
        val mesh: Mesh? = null,
        val skin: Skin? = null,
        val name: String? = null,
    ) {
        fun render(
            stack: PoseStack,
            nodeRenderer: NodeRenderer,
            data: ModelData,
            consumer: (ResourceLocation) -> RenderType,
            light: Int,
            overlay: Int
        ) {
            stack.use {
                mulPoseMatrix(localMatrix)

                mesh?.render(this@Node, stack, consumer, light, overlay)
                children.forEach { it.render(stack, nodeRenderer, data, consumer, light, overlay) }


                data.entity?.let {
                    nodeRenderer(it, stack, this@Node, light)
                }
            }
        }

        fun skinningMatrix(jointId: Int, matrix: Matrix4f): Matrix4f {
            val skin = skin ?: return matrix

            val inverseTransform = matrix.copy().apply { invert() }
            val jointMat = skin.joints[jointId]?.globalMatrix?.apply {
                multiply(
                    skin.inverseBindMatrices[skin.jointsIds.indexOf(jointId)]
                )
            } ?: return matrix
            return inverseTransform.apply { multiply(jointMat) }
        }

        var parent: Node? = null
        val isHead: Boolean get() = name?.lowercase()?.contains("head") == true && parent?.isHead == false
        val globalMatrix: Matrix4f
            get() {
                return NODE_GLOBAL_TRANSFORMATION_LOOKUP_CACHE.computeIfAbsent(this) { node ->
                    val matrix = node.parent?.globalMatrix ?: return@computeIfAbsent localMatrix
                    matrix.multiply(localMatrix)
                    matrix
                }
            }

        val localMatrix get() = transform.getMatrix()
    }

    data class Skin(
        val jointsIds: List<Int>,
        val inverseBindMatrices: List<Matrix4f>,
    ) {
        val joints = HashMap<Int, Node>(jointsIds.size)

        fun finalMatrices(matrix: Node): Array<Matrix4f> {
            return joints.values.mapIndexed { i, joint ->
                val inverseTransform = matrix.globalMatrix.copy().apply { invert() }
                val jointMat = joint.globalMatrix.apply { multiply(this@Skin.inverseBindMatrices[i]) }
                inverseTransform.apply { multiply(jointMat) }
            }.toTypedArray()
        }
    }

    data class Mesh(
        val primitives: List<Primitive>,
    ) {
        fun render(
            node: Node,
            stack: PoseStack,
            consumer: (ResourceLocation) -> RenderType,
            light: Int,
            overlay: Int,
        ) {
            primitives.forEach {
                it.renderForVanilla(stack, node, consumer, light, overlay)
            }
        }
    }

    data class Primitive(
        val attributes: Map<GltfAttribute, Buffer>,
        val indices: Buffer? = null,
        val mode: GltfMode,
        val material: ResourceLocation,
    ) {
        private val indicesArray = indices?.get<Int>()?.toIntArray() ?: intArrayOf()
        private var indexCount = indicesArray.size
        private val vertices = attributes[GltfAttribute.POSITION]?.get<Vector3f>()?.run {
            val positions = FloatArray(this.size * 3)
            this.forEachIndexed { i, vec ->
                positions[i * 3] = vec.x()
                positions[i * 3 + 1] = vec.y()
                positions[i * 3 + 2] = vec.z()
            }
            positions
        } ?: floatArrayOf()
        private val normals = attributes[GltfAttribute.NORMAL]?.get<Vector3f>()?.run {
            val normals = FloatArray(this.size * 3)
            this.forEachIndexed { i, vec ->
                normals[i * 3] = vec.x()
                normals[i * 3 + 1] = vec.y()
                normals[i * 3 + 2] = vec.z()
            }
            normals
        } ?: floatArrayOf()
        private val texCords = attributes[GltfAttribute.TEXCOORD_0]?.get<Pair<Float, Float>>()?.run {
            val texCords = FloatArray(this.size * 2)
            this.forEachIndexed { i, vec ->
                texCords[i * 2] = vec.first
                texCords[i * 2 + 1] = vec.second
            }
            texCords
        } ?: floatArrayOf()
        private val joints = attributes[GltfAttribute.JOINTS_0]?.get<Vector4f>()?.run {
            val joints = IntArray(this.size * 4)
            this.forEachIndexed { i, vec ->
                joints[i * 4] = vec.x().toInt()
                joints[i * 4 + 1] = vec.y().toInt()
                joints[i * 4 + 2] = vec.z().toInt()
                joints[i * 4 + 3] = vec.w().toInt()
            }
            joints
        } ?: intArrayOf()
        private val weights = attributes[GltfAttribute.WEIGHTS_0]?.get<Vector4f>()?.run {
            val weights = FloatArray(this.size * 4)
            this.forEachIndexed { i, vec ->
                weights[i * 4] = vec.x()
                weights[i * 4 + 1] = vec.y()
                weights[i * 4 + 2] = vec.z()
                weights[i * 4 + 3] = vec.w()
            }
            weights
        } ?: floatArrayOf()
        private var vertexBuffer = -1
        private var normalBuffer = -1
        private var texCoordsBuffer = -1
        private var indexBuffer = -1
        private var vao = -1

        init {
            initBuffers()
        }

        private fun initBuffers() {
            val currentVAO = GL33.glGetInteger(GL33.GL_VERTEX_ARRAY_BINDING)
            val currentArrayBuffer = GL33.glGetInteger(GL33.GL_ARRAY_BUFFER_BINDING)
            val currentElementArrayBuffer = GL33.glGetInteger(GL33.GL_ELEMENT_ARRAY_BUFFER_BINDING)

            vao = GL33.glGenVertexArrays()
            GL33.glBindVertexArray(vao)

            vertexBuffer = GL33.glGenBuffers()
            GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, vertexBuffer)
            GL33.glBufferData(GL33.GL_ARRAY_BUFFER, vertices, GL33.GL_STATIC_DRAW)
            GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 0, 0)

            texCoordsBuffer = GL33.glGenBuffers()
            GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, texCoordsBuffer)
            GL33.glBufferData(GL33.GL_ARRAY_BUFFER, texCords, GL33.GL_STATIC_DRAW)
            GL33.glVertexAttribPointer(2, 2, GL33.GL_FLOAT, false, 0, 0)

            normalBuffer = GL33.glGenBuffers()
            GL33.glEnableVertexAttribArray(5) //normal
            GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, normalBuffer)
            GL33.glBufferData(GL33.GL_ARRAY_BUFFER, normals, GL33.GL_STATIC_DRAW)
            GL33.glVertexAttribPointer(5, 3, GL33.GL_FLOAT, false, 0, 0)

            indexBuffer = GL33.glGenBuffers()
            GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, indexBuffer)
            GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, putIntBuffer(indicesArray), GL33.GL_STATIC_DRAW)
            indexCount = indicesArray.size

            GL33.glBindVertexArray(currentVAO)
            GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, currentArrayBuffer)
            GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer)
        }

        fun renderForVanilla(
            stack: PoseStack,
            node: Node,
            consumer: (ResourceLocation) -> RenderType,
            light: Int,
            overlay: Int,
        ) {
            val shader = GameRenderer.getRendertypeEntityTranslucentShader() ?: return
            //Всякие настройки смешивания, материалы и т.п.
            val type = consumer(material)
            type.setupRenderState()

            //Подключение VAO и IBO
            GL33.glBindVertexArray(vao)
            GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, indexBuffer)

            GL33.glEnableVertexAttribArray(0) // Вершины
            GL33.glEnableVertexAttribArray(2) // Текстурные координаты
            GL33.glEnableVertexAttribArray(5) // Нормали

            GL33.glActiveTexture(GL33.GL_TEXTURE1) //Оверлей
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, RenderSystem.getShaderTexture(1))

            GL33.glActiveTexture(GL33.GL_TEXTURE0) //Текстуры модели
            GL33.glBindTexture(GL11.GL_TEXTURE_2D, RenderSystem.getShaderTexture(0))

            //Отрисовка
            shader.MODEL_VIEW_MATRIX?.set(stack.last().pose())
            shader.MODEL_VIEW_MATRIX?.upload()

            shader.getUniform("NormalMat")?.let {
                it.set(stack.last().normal())
                it.upload()
            }

            GL33.glDrawElements(GL33.GL_TRIANGLES, indexCount, GL33.GL_UNSIGNED_INT, 0L)

            GL33.glActiveTexture(GL33.GL_TEXTURE2)


            //Отключение параметров выше
            GL33.glDisableVertexAttribArray(0)
            GL33.glDisableVertexAttribArray(2)
            GL33.glDisableVertexAttribArray(5)

            // Очистка настроек
            type.clearRenderState()
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

val NODE_GLOBAL_TRANSFORMATION_LOOKUP_CACHE = IdentityHashMap<GltfTree.Node, Matrix4f>()

fun main() {
    val tree = GltfTree.parse("hc:models/entity/hilda_regular.glb".rl)

    println(tree)
}

fun putFloatBuffer(value: FloatArray): FloatBuffer {
    val buffer = BufferUtils.createFloatBuffer(value.size)
    buffer.put(value)
    buffer.flip()
    return buffer
}

fun putIntBuffer(value: IntArray): IntBuffer {
    val buffer = BufferUtils.createIntBuffer(value.size)
    buffer.put(value)
    buffer.flip()
    return buffer
}