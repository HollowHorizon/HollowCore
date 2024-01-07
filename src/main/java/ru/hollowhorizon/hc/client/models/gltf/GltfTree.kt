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
import org.lwjgl.opengl.*
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.HollowCore.MODID
import ru.hollowhorizon.hc.client.utils.areShadersEnabled
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.client.utils.toIS
import ru.hollowhorizon.hc.client.utils.use
import ru.hollowhorizon.hc.common.registry.ModShaders
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

fun ByteBuffer.readUInt(): Int {
    return ((getInt().toLong() and 0xffffffffL).toInt())
}

fun ByteBuffer.readUByte(): Int {
    return (get().toInt() and 0xff)
}

fun ByteBuffer.readUShort(): Int {
    return getShort().toInt() and 0xffff
}

class Vec4i(val x: Int, val y: Int, val z: Int, val w: Int)


object GltfTree {
    val TEXTURE_MAP = HashMap<ResourceLocation, DynamicTexture>()


    fun parse(file: GltfFile, location: ResourceLocation, folder: (String) -> InputStream): GLTFTree {
        val buffers = parseBuffers(file, folder)
        val bufferViews = parseBufferViews(file, buffers)
        val accessors = parseAccessors(file, bufferViews)
        val textures = mutableSetOf<ResourceLocation>()
        val meshes = parseMeshes(file, accessors, bufferViews, location, textures, folder)
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
                    b.next(t).toFloat(), b.next(t).toFloat(), b.next(t).toFloat(), b.next(t).toFloat()
                )
            }

            GltfType.MAT2 -> error("Unsupported")
            GltfType.MAT3 -> List(count) {
                Matrix3f().apply {
                    load(
                        FloatBuffer.wrap(
                            floatArrayOf(
                                b.next(t).toFloat(),
                                b.next(t).toFloat(),
                                b.next(t).toFloat(),
                                b.next(t).toFloat(),
                                b.next(t).toFloat(),
                                b.next(t).toFloat(),
                                b.next(t).toFloat(),
                                b.next(t).toFloat(),
                                b.next(t).toFloat()
                            )
                        )
                    )
                    transpose()
                }
            }

            GltfType.MAT4 -> List(count) {
                Matrix4f(
                    floatArrayOf(
                        b.next(t).toFloat(),
                        b.next(t).toFloat(),
                        b.next(t).toFloat(),
                        b.next(t).toFloat(),
                        b.next(t).toFloat(),
                        b.next(t).toFloat(),
                        b.next(t).toFloat(),
                        b.next(t).toFloat(),
                        b.next(t).toFloat(),
                        b.next(t).toFloat(),
                        b.next(t).toFloat(),
                        b.next(t).toFloat(),
                        b.next(t).toFloat(),
                        b.next(t).toFloat(),
                        b.next(t).toFloat(),
                        b.next(t).toFloat()
                    )
                ).apply(Matrix4f::transpose)
            }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun ByteBuffer.next(type: GltfComponentType): Number {
        return when (type) {
            GltfComponentType.BYTE -> get()
            GltfComponentType.UNSIGNED_BYTE -> readUByte()
            GltfComponentType.SHORT -> short
            GltfComponentType.UNSIGNED_SHORT -> readUShort()
            GltfComponentType.UNSIGNED_INT -> readUInt()
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
        file: GltfFile,
        accessors: List<Buffer>,
        bufferViews: List<ByteArray>,
        location: ResourceLocation,
        textures: MutableSet<ResourceLocation>,
        folder: (String) -> InputStream,
    ): List<Mesh> {
        return file.meshes.map { mesh ->
            val primitives = mesh.primitives.map { prim ->

                val attr = prim.attributes.map { (k, v) ->
                    Pair(GltfAttribute.valueOf(k), accessors[v])
                }.toMap()

                val indices = prim.indices?.let { accessors[it] }
                val mode = GltfMode.fromId(prim.mode)

                val material = getMaterial(file, prim.material, bufferViews, location, folder)
                    ?: TextureManager.INTENTIONAL_MISSING_TEXTURE
                textures += material

                Primitive(attr, indices, mode, material)
            }

            Mesh(primitives)
        }
    }

    private fun getMaterial(
        file: GltfFile,
        mat: Int?,
        bufferViews: List<ByteArray>,
        location: ResourceLocation,
        folder: (String) -> InputStream,
    ): ResourceLocation? {
        if (mat == null) return null
        val material = file.materials[mat]

        val texture = material.pbrMetallicRoughness?.baseColorTexture?.index
            ?: return if (material.name != null) "$MODID:textures/models/${material.name}.png".lowercase().rl
            else null
        val image = file.textures[texture].source ?: return null
        file.images[image].bufferView?.let { index ->
            val data = bufferViews[index]
            val stream = ByteArrayInputStream(data)

            val dynamicTexture = DynamicTexture(NativeImage.read(stream))
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
        val mesh = node.mesh?.let {
            meshes[it].apply {
                if (node.skin != null) {
                    this.primitives.forEach { p -> p.jointCount = skins[node.skin].jointsIds.size }
                }
                this.primitives.forEach(Primitive::init)
            }
        }
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
        ) {
            nodes.forEach { it.render(stack, nodeRenderer, data, consumer, light) }
        }

        fun transformSkinning(stack: PoseStack) {
            nodes.forEach { it.transformSkinning(stack) }
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
        val baseTransform = transform.copy()

        fun render(
            stack: PoseStack,
            nodeRenderer: NodeRenderer,
            data: ModelData,
            consumer: (ResourceLocation) -> RenderType,
            light: Int,
        ) {
            stack.use {
                mulPoseMatrix(localMatrix)

                mesh?.render(this@Node, stack, consumer)
                children.forEach { it.render(stack, nodeRenderer, data, consumer, light) }


                data.entity?.let {
                    nodeRenderer(it, stack, this@Node, light)
                }
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
                matrix.multiply(localMatrix)
                return matrix
            }

        val localMatrix get() = transform.getMatrix()
    }

    data class Skin(
        val jointsIds: List<Int>,
        val inverseBindMatrices: List<Matrix4f>,
    ) {
        val joints = HashMap<Int, Node>(jointsIds.size)

        fun finalMatrices(node: Node): Array<Matrix4f> {
            val jointMatrices = Array(jointsIds.size) { Matrix4f().apply { setIdentity() } }
            val inverseTransform = node.globalMatrix
            inverseTransform.invertMatrix()

            for (i in jointsIds.indices) {
                jointMatrices[i] = joints[i]!!.globalMatrix.apply { multiply(inverseBindMatrices[i]) }
                jointMatrices[i] = inverseTransform.copy().apply { multiply(jointMatrices[i]) }
            }
            return jointMatrices
        }
    }

    data class Mesh(
        val primitives: List<Primitive>,
    ) {
        fun render(
            node: Node,
            stack: PoseStack,
            consumer: (ResourceLocation) -> RenderType,
        ) {
            primitives.forEach {
                it.renderForVanilla(stack, node, consumer)
            }
        }

        fun transformSkinning(node: Node, stack: PoseStack) {
            primitives.filter { it.hasSkinning }.forEach { it.transformSkinning(node, stack) }
        }
    }

    data class Primitive(
        val attributes: Map<GltfAttribute, Buffer>,
        val indices: Buffer? = null,
        val mode: GltfMode,
        val material: ResourceLocation,
    ) {
        val hasSkinning = attributes[GltfAttribute.JOINTS_0] != null && attributes[GltfAttribute.WEIGHTS_0] != null
        private val indexCount = indices?.get<Int>()?.size ?: 0
        private val positionsCount = (attributes[GltfAttribute.POSITION]?.get<Vector3f>()?.size ?: 0) * 3
        var jointCount = 0

        private var vao = -1
        private var skinningVao = -1

        private var vertexBuffer = -1
        private var normalBuffer = -1
        private var texCoordsBuffer = -1
        private var indexBuffer = -1

        private var glTexture = -1
        private var jointBuffer = -1
        private var weightsBuffer = -1
        private var skinVertexBuffer = -1
        private var skinNormalBuffer = -1
        private var jointMatrixBuffer = -1

        fun init() {
            val currentVAO = GL33.glGetInteger(GL33.GL_VERTEX_ARRAY_BINDING)
            val currentArrayBuffer = GL33.glGetInteger(GL33.GL_ARRAY_BUFFER_BINDING)
            val currentElementArrayBuffer = GL33.glGetInteger(GL33.GL_ELEMENT_ARRAY_BUFFER_BINDING)

            if (hasSkinning) initTransformFeedback()
            initBuffers()

            GL33.glBindVertexArray(currentVAO)
            GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, currentArrayBuffer)
            GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer)
        }

        private fun initBuffers() {

            vao = GL33.glGenVertexArrays()
            GL33.glBindVertexArray(vao)

            if (skinningVao == -1) {
                attributes[GltfAttribute.POSITION]?.get<Vector3f>()?.run {
                    val positions = BufferUtils.createFloatBuffer(this.size * 3)
                    for (n in this) positions.put(n.x()).put(n.y()).put(n.z())
                    positions.flip()

                    vertexBuffer = GL33.glGenBuffers()
                    GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, vertexBuffer)
                    GL33.glBufferData(GL33.GL_ARRAY_BUFFER, positions, GL33.GL_STATIC_DRAW)
                    GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 0, 0)
                }
                attributes[GltfAttribute.NORMAL]?.get<Vector3f>()?.run {
                    val normals = BufferUtils.createFloatBuffer(this.size * 3)
                    for (n in this) normals.put(n.x()).put(n.y()).put(n.z())
                    normals.flip()

                    normalBuffer = GL33.glGenBuffers()
                    GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, normalBuffer)
                    GL33.glBufferData(GL33.GL_ARRAY_BUFFER, normals, GL33.GL_STATIC_DRAW)
                    GL33.glVertexAttribPointer(5, 3, GL33.GL_FLOAT, false, 0, 0)
                }
            } else {
                GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, vertexBuffer)
                GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 0, 0)

                GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, normalBuffer)
                GL33.glVertexAttribPointer(5, 3, GL33.GL_FLOAT, false, 0, 0)
            }

            attributes[GltfAttribute.TEXCOORD_0]?.get<Pair<Float, Float>>()?.run {
                val texCords = BufferUtils.createFloatBuffer(this.size * 2)
                for (t in this) texCords.put(t.first).put(t.second)
                texCords.flip()

                texCoordsBuffer = GL33.glGenBuffers()
                GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, texCoordsBuffer)
                GL33.glBufferData(GL33.GL_ARRAY_BUFFER, texCords, GL33.GL_STATIC_DRAW)
                GL33.glVertexAttribPointer(2, 2, GL33.GL_FLOAT, false, 0, 0)
            }

            indices?.get<Int>()?.run {
                val buffer = BufferUtils.createIntBuffer(this.size)
                for (n in this) buffer.put(n)
                buffer.flip()

                indexBuffer = GL33.glGenBuffers()
                GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, indexBuffer)
                GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, buffer, GL33.GL_STATIC_DRAW)
            }
        }

        private fun initTransformFeedback() {
            skinningVao = GL30.glGenVertexArrays()
            GL30.glBindVertexArray(skinningVao)

            var posSize = -1L
            var norSize = -1L

            attributes[GltfAttribute.JOINTS_0]?.get<Vector4f>()?.run {
                val joints = BufferUtils.createIntBuffer(this.size * 4)
                for (n in this) joints
                    .put(n.x().toInt())
                    .put(n.y().toInt())
                    .put(n.z().toInt())
                    .put(n.w().toInt())
                joints.flip()

                jointBuffer = GL33.glGenBuffers()
                GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, jointBuffer)
                GL33.glBufferData(GL33.GL_ARRAY_BUFFER, joints, GL33.GL_STATIC_DRAW)
                GL33.glVertexAttribPointer(0, 4, GL33.GL_INT, false, 0, 0)
            }
            attributes[GltfAttribute.WEIGHTS_0]?.get<Vector4f>()?.run {
                val weights = BufferUtils.createFloatBuffer(this.size * 4)
                for (n in this) weights.put(n.x()).put(n.y()).put(n.z()).put(n.w())
                weights.flip()

                weightsBuffer = GL33.glGenBuffers()
                GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, weightsBuffer)
                GL33.glBufferData(GL33.GL_ARRAY_BUFFER, weights, GL33.GL_STATIC_DRAW)
                GL33.glVertexAttribPointer(1, 4, GL33.GL_FLOAT, false, 0, 0)
            }
            attributes[GltfAttribute.POSITION]?.get<Vector3f>()?.run {
                posSize = this.size * 12L //bytes size
                val positions = BufferUtils.createFloatBuffer(this.size * 3)
                for (n in this) positions.put(n.x()).put(n.y()).put(n.z())
                positions.flip()

                skinVertexBuffer = GL33.glGenBuffers()
                GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, skinVertexBuffer)
                GL33.glBufferData(GL33.GL_ARRAY_BUFFER, positions, GL33.GL_STATIC_DRAW)
                GL33.glVertexAttribPointer(2, 3, GL33.GL_FLOAT, false, 0, 0)
            }
            attributes[GltfAttribute.NORMAL]?.get<Vector3f>()?.run {
                norSize = this.size * 12L //bytes size
                val normals = BufferUtils.createFloatBuffer(this.size * 3)
                for (n in this) normals.put(n.x()).put(n.y()).put(n.z())
                normals.flip()

                skinNormalBuffer = GL33.glGenBuffers()
                GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, skinNormalBuffer)
                GL33.glBufferData(GL33.GL_ARRAY_BUFFER, normals, GL33.GL_STATIC_DRAW)
                GL33.glVertexAttribPointer(3, 3, GL33.GL_FLOAT, false, 0, 0)
            }

            vertexBuffer = GL33.glGenBuffers()
            GL33.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, vertexBuffer)
            GL15.glBufferData(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, posSize, GL33.GL_STATIC_DRAW)

            normalBuffer = GL33.glGenBuffers()
            GL33.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, normalBuffer)
            GL15.glBufferData(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, norSize, GL33.GL_STATIC_DRAW)

            jointMatrixBuffer = GL15.glGenBuffers()
            GL15.glBindBuffer(GL31.GL_TEXTURE_BUFFER, jointMatrixBuffer)
            GL15.glBufferData(GL31.GL_TEXTURE_BUFFER, jointCount * 64L, GL15.GL_STATIC_DRAW)
            glTexture = GL11.glGenTextures()
            GL11.glBindTexture(GL31.GL_TEXTURE_BUFFER, glTexture)
            GL31.glTexBuffer(GL31.GL_TEXTURE_BUFFER, GL30.GL_RGBA32F, jointMatrixBuffer)
            GL11.glBindTexture(GL31.GL_TEXTURE_BUFFER, 0)

            GL15.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, 0)
            GL15.glBindBuffer(GL31.GL_TEXTURE_BUFFER, 0)
        }

        fun renderForVanilla(
            stack: PoseStack,
            node: Node,
            consumer: (ResourceLocation) -> RenderType,
        ) {
            val shader =
                if (!areShadersEnabled) ModShaders.GLTF_ENTITY else GameRenderer.getRendertypeEntityTranslucentShader()!!
            //Всякие настройки смешивания, материалы и т.п.
            val type = consumer(material)
            type.setupRenderState()

            GL33.glBindTexture(GL33.GL_TEXTURE_2D, RenderSystem.getShaderTexture(0))

            RenderSystem.enableBlend()
            RenderSystem.defaultBlendFunc()
            GL11.glEnable(GL11.GL_CULL_FACE)

            //Подключение VAO и IBO
            GL33.glBindVertexArray(vao)
            GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, indexBuffer)

            GL33.glEnableVertexAttribArray(0) // Вершины
            GL33.glEnableVertexAttribArray(2) // Текстурные координаты
            GL33.glEnableVertexAttribArray(5) // Нормали

            //Матрица
            shader.MODEL_VIEW_MATRIX?.set(RenderSystem.getModelViewMatrix().copy()
                .apply { multiply(stack.last().pose()) })
            shader.MODEL_VIEW_MATRIX?.upload()

            val normal = Matrix3f(node.globalMatrix)
            val currentNormal = CURRENT_NORMAL.copy()
            currentNormal.mul(normal)

            //Нормали
            shader.getUniform("NormalMat")?.let {
                it.set(stack.last().normal())
                it.upload()
            }

            //Отрисовка
            GL33.glDrawElements(GL33.GL_TRIANGLES, indexCount, GL33.GL_UNSIGNED_INT, 0L)

            //Отключение параметров выше
            GL33.glDisableVertexAttribArray(0)
            GL33.glDisableVertexAttribArray(2)
            GL33.glDisableVertexAttribArray(5)

            // Очистка настроек
            type.clearRenderState()
        }

        fun transformSkinning(node: Node, stack: PoseStack) {
            val texBind = GL33.glGetInteger(GL33.GL_ACTIVE_TEXTURE)

            GL13.glActiveTexture(GL13.GL_TEXTURE0)
            GL33.glBindBuffer(GL33.GL_TEXTURE_BUFFER, jointMatrixBuffer)
            GL33.glBufferSubData(GL33.GL_TEXTURE_BUFFER, 0, computeMatrices(node, stack))

            GL33.glBindTexture(GL33.GL_TEXTURE_BUFFER, glTexture)

            GL30.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, 0, vertexBuffer)
            GL30.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, 1, normalBuffer)

            GL30.glBeginTransformFeedback(GL11.GL_POINTS)
            GL30.glBindVertexArray(skinningVao)
            for (i in 0..3) GL33.glEnableVertexAttribArray(i)
            GL11.glDrawArrays(GL11.GL_POINTS, 0, positionsCount)
            for (i in 0..3) GL33.glDisableVertexAttribArray(i)
            GL30.glBindVertexArray(0)
            GL30.glEndTransformFeedback()
            GL13.glActiveTexture(texBind)
        }

        private fun computeMatrices(node: Node, stack: PoseStack): FloatBuffer {
            val matrices = node.skin!!.finalMatrices(node)

            val buffer = BufferUtils.createFloatBuffer(matrices.size * 16)
            for (m in matrices) {
                // Угадайте сколько часов у меня ушло, чтобы угадать, в каком порядке я должен передавать эти значения :(
                buffer.put(m.m00)
                buffer.put(m.m10)
                buffer.put(m.m20)
                buffer.put(m.m30)
                buffer.put(m.m01)
                buffer.put(m.m11)
                buffer.put(m.m21)
                buffer.put(m.m31)
                buffer.put(m.m02)
                buffer.put(m.m12)
                buffer.put(m.m22)
                buffer.put(m.m32)
                buffer.put(m.m03)
                buffer.put(m.m13)
                buffer.put(m.m23)
                buffer.put(m.m33)
            }
            buffer.flip()
            return buffer
        }

        fun destroy() {
            GL30.glDeleteVertexArrays(vao)
            GL30.glDeleteVertexArrays(skinningVao)

            GL30.glDeleteBuffers(indexBuffer)
            GL30.glDeleteBuffers(vertexBuffer)
            GL30.glDeleteBuffers(texCoordsBuffer)
            GL30.glDeleteBuffers(normalBuffer)

            GL30.glDeleteBuffers(skinVertexBuffer)
            GL30.glDeleteBuffers(skinNormalBuffer)
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

var CURRENT_NORMAL = Matrix3f()