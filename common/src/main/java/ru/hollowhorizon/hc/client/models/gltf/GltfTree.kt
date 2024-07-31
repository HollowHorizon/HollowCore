/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ArmorItem
import org.joml.*
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.*
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.HollowCore.MODID
import ru.hollowhorizon.hc.client.models.gltf.manager.GltfManager
import ru.hollowhorizon.hc.client.utils.*
import ru.hollowhorizon.hc.client.utils.math.MikkTSpaceContext
import ru.hollowhorizon.hc.client.utils.math.MikktspaceTangentGenerator
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.EOFException
import java.io.InputStream
import java.nio.BufferUnderflowException
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
        val textures = mutableSetOf<Material>()
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

    fun parse(resource: ResourceLocation): GLTFTree {
        val location = if (!resource.exists()) "$MODID:models/error.gltf".rl else resource

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
                return Base64.getDecoder().wrap(path.substring(37).byteInputStream())
            }
            if (path.startsWith("data:image/png;base64,")) {
                return Base64.getDecoder().wrap(path.substring(22).byteInputStream())
            }

            val basePath = location.path.substringBeforeLast('/', "")
            val loc = (location.namespace + ":" + if (basePath.isEmpty()) path else "$basePath/$path").rl

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
                    set(
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
                    transpose()
                }
            }

            GltfType.MAT4 -> List(count) {
                Matrix4f(
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
                ).transpose()
            }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun ByteBuffer.next(type: GltfComponentType): Number {
        try {
            return when (type) {
                GltfComponentType.BYTE -> get()
                GltfComponentType.UNSIGNED_BYTE -> readUByte()
                GltfComponentType.SHORT -> short
                GltfComponentType.UNSIGNED_SHORT -> readUShort()
                GltfComponentType.UNSIGNED_INT -> readUInt()
                GltfComponentType.FLOAT -> float
            }
        } catch (ex: BufferUnderflowException) {
            HollowCore.LOGGER.error("Can't read buffer $type: ", ex)
            return 0
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
        textures: MutableSet<Material>,
        folder: (String) -> InputStream,
    ): List<Mesh> {
        return file.meshes.map { mesh ->
            val primitives = mesh.primitives.map { prim ->
                val attributes = GltfAttribute.values().map { it.name }
                val attr = prim.attributes.filter { it.key in attributes }.map { (k, v) ->
                    Pair(GltfAttribute.valueOf(k), accessors[v])
                }.toMap()

                val morphTargets = prim.targets.map { attribs ->
                    attribs.filter { it.key in attributes }.map { (k, v) ->
                        val attribute = GltfAttribute.valueOf(k)
                        val buffer = accessors[v]
                        val array = ArrayList<Float>()

                        when (attribute) {
                            GltfAttribute.POSITION, GltfAttribute.NORMAL, GltfAttribute.TANGENT -> {
                                buffer.get<Vector3f>().forEach {
                                    array.add(it.x())
                                    array.add(it.y())
                                    array.add(it.z())
                                }
                            }

                            GltfAttribute.COLOR_0, GltfAttribute.JOINTS_0, GltfAttribute.JOINTS_1, GltfAttribute.WEIGHTS_0 -> {
                                buffer.get<Vector4f>().forEach {
                                    array.add(it.x())
                                    array.add(it.y())
                                    array.add(it.z())
                                    array.add(it.w())
                                }
                            }

                            GltfAttribute.TEXCOORD_0, GltfAttribute.TEXCOORD_1 -> {
                                buffer.get<Pair<Float, Float>>().forEach {
                                    array.add(it.first)
                                    array.add(it.second)
                                }
                            }
                        }

                        Pair(attribute, array.toFloatArray())
                    }.toMap()
                }

                val indices = prim.indices?.let { accessors[it] }
                val mode = GltfMode.fromId(prim.mode)

                val material = getMaterial(file, prim.material, bufferViews, location, folder)
                textures += material

                Primitive(attr, indices, mode, material, morphTargets)
            }

            Mesh(primitives, mesh.weights)
        }
    }

    private fun getMaterial(
        file: GltfFile,
        mat: Int?,
        bufferViews: List<ByteArray>,
        location: ResourceLocation,
        folder: (String) -> InputStream,
    ): Material {
        if (mat == null) return Material()
        val material = file.materials[mat]

        val color = material.pbrMetallicRoughness?.baseColorFactor ?: Vector4f(1.0f, 1.0f, 1.0f, 1.0f)

        val textureId = material.pbrMetallicRoughness?.baseColorTexture?.index ?: -1

        if (textureId == -1) return if (material.name != null) Material(
            color,
            "$MODID:textures/models/${material.name}.png".lowercase().rl,
            doubleSided = material.doubleSided
        )
        else Material(color, doubleSided = material.doubleSided)

        val texture =
            getTexture(file, bufferViews, location, folder, textureId) ?: "$MODID:default_color_map".rl
        var normalTexture = "$MODID:default_normal_map".rl
        material.normalTexture?.index?.let {
            getTexture(file, bufferViews, location, folder, it)?.let { texture ->
                normalTexture = texture
            }
        }
        var occlusionTexture = "$MODID:default_specular_map".rl
        material.pbrMetallicRoughness?.metallicRoughnessTexture?.index?.let {
            getTexture(file, bufferViews, location, folder, it)?.let { texture ->
                occlusionTexture = texture
            }
        }


        return Material(color, texture, normalTexture, occlusionTexture, material.doubleSided)
    }

    fun getTexture(
        file: GltfFile,
        bufferViews: List<ByteArray>,
        model: ResourceLocation,
        folder: (String) -> InputStream,
        index: Int,
    ): ResourceLocation? {
        val texture = file.textures[index]
        val generatedTextureName = "gltf_texture_${model.path.replace("/", ".")}_$index"
        val image = texture.source ?: return null

        file.images[image].bufferView?.let { bufferId ->
            val data = bufferViews[bufferId]
            val stream = ByteArrayInputStream(data)

            val dynamicTexture = DynamicTexture(NativeImage.read(stream))

            //Название изначально может быть пустым, а не только null, так что строчка ниже не просто так
            var textureName = texture.name?.substringBefore(".png") ?: ""
            if (textureName.isEmpty()) textureName = generatedTextureName

            val textureLocation = "$MODID:textureName.lowercase()".rl

            if (!TEXTURE_MAP.contains(textureLocation)) {
                TEXTURE_MAP[textureLocation] = dynamicTexture
                Minecraft.getInstance().textureManager.register(textureLocation, dynamicTexture)
            }

            return textureLocation
        }

        val texturePath = file.images[image].uri ?: return null

        if (texturePath.contains(':')) {
            fun validPathChar(c: Char): Boolean {
                return c == '_' || c == '-' || c in 'a'..'z' || c in '0'..'9' || c == '/' || c == '.'
            }

            fun isValidPath(pPath: String): Boolean {
                for (element in pPath) {
                    if (!validPathChar(element)) {
                        return false
                    }
                }

                return true
            }

            if (!isValidPath(texturePath.split(":")[1])) {
                if (texturePath.startsWith("data:image/png;base64,")) {
                    val decoded = folder(texturePath)
                    val dynamicTexture = DynamicTexture(NativeImage.read(decoded))
                    var textureName = texture.name?.substringBefore(".png")
                        ?: "" //Название изначально может быть пустым, а не только null, так что строчка ниже не просто так
                    if (textureName.isEmpty()) textureName = "gltf_texture_${model.path.replace("/", ".")}_$index"
                    val textureLocation = (MODID + ":" + textureName.lowercase()).rl

                    if (!TEXTURE_MAP.contains(textureLocation)) {
                        TEXTURE_MAP[textureLocation] = dynamicTexture
                        Minecraft.getInstance().textureManager.register(textureLocation, dynamicTexture)
                    }

                    return textureLocation
                }
                return null
            }

            return texturePath.rl
        }

        val relativeModelPath = model.path.substringAfter("models/")
        val localPath = relativeModelPath.substringBeforeLast('/', "")

        val finalPath = buildString {
            if (localPath.isNotEmpty()) {
                append(localPath)
                append("/")
            }
            append(texturePath)
        }

        return (model.namespace + ":models/" + finalPath.lowercase()).rl
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
        val weights = ArrayList<Float>()
        val mesh = node.mesh?.let { index ->
            meshes[index].apply {
                weights.addAll(this.weights.map { it.toFloat() })
                if (node.skin != null) {
                    this.primitives.forEach { p -> p.jointCount = skins[node.skin].jointsIds.size }
                }
                this.primitives.forEach(Primitive::init)
            }
        }
        val skin = node.skin?.let { skins[it] }

        val transform = Transformation(
            translation = node.translation ?: Vector3f(),
            rotation = node.rotation ?: Quaternionf(0.0f, 0.0f, 0.0f, 1.0f),
            scale = node.scale ?: Vector3f(1.0f, 1.0f, 1.0f),
            weights = weights,
            matrix = node.matrix ?: Matrix4f()
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
        val materials: Set<Material>,
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

    data class Material(
        val color: Vector4f = Vector4f(1f, 1f, 1f, 1f),
        val texture: ResourceLocation = "$MODID:default_color_map".rl,
        val normalTexture: ResourceLocation = "$MODID:default_normal_map".rl,
        val specularTexture: ResourceLocation = "$MODID:default_specular_map".rl,
        val doubleSided: Boolean = false,
    )

    data class Scene(
        val nodes: List<Node>,
    ) {
        fun render(
            stack: PoseStack,
            nodeRenderer: NodeRenderer,
            data: ModelData,
            consumer: (ResourceLocation) -> Int,
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
                mulPose(localMatrix)

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
                mulPose(localMatrix)
                last().normal().mul(normalMatrix)

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

    class Skin(
        val jointsIds: List<Int>,
        val inverseBindMatrices: List<Matrix4f>,
    ) {
        val joints = HashMap<Int, Node>(jointsIds.size)

        private val skin = Array(jointsIds.size) { Matrix4f() }

        fun finalMatrices(node: Node): Array<Matrix4f> {
            val inverseTransform = node.globalMatrix
            inverseTransform.invert()

            for (i in jointsIds.indices) {
                skin[i] = joints[i]!!.globalMatrix.mul(inverseBindMatrices[i])
                skin[i] = Matrix4f(inverseTransform).mul(skin[i])
            }
            return skin
        }
    }

    data class Mesh(
        val primitives: List<Primitive>,
        val weights: List<Double>,
    ) {
        fun render(
            node: Node,
            stack: PoseStack,
            consumer: (ResourceLocation) -> Int,
        ) {
            primitives.forEach {
                it.render(stack, node, consumer)
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
        val material: Material,
        val morphTargets: List<Map<GltfAttribute, FloatArray>> = ArrayList(),
    ) {
        val hasSkinning = attributes[GltfAttribute.JOINTS_0] != null && attributes[GltfAttribute.WEIGHTS_0] != null
        private val indexCount = indices?.get<Int>()?.size ?: 0
        private val positionsCount = (attributes[GltfAttribute.POSITION]?.get<Vector3f>()?.size ?: 0) * 3
        var jointCount = 0
        val morphCommands = ArrayList<(FloatArray) -> Unit>()

        private var vao = -1
        private var skinningVao = -1

        private var vertexBuffer = -1
        private var normalBuffer = -1
        private var tangentBuffer = -1
        private var texCoordsBuffer = -1
        private var midCoordsBuffer = -1
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

                    morphCommands += { array ->
                        for (i in 0 until this.size * 3) {
                            var value = this[i / 3].toArray()[i % 3]
                            array.forEachIndexed { j, shapeKey ->
                                morphTargets[j][GltfAttribute.POSITION]?.let {
                                    value += it[i] * shapeKey
                                }
                            }
                            positions.put(i, value)
                        }

                        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, vertexBuffer)
                        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, positions, GL33.GL_STATIC_DRAW)
                    }

                    vertexBuffer = GL33.glGenBuffers()
                    GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, vertexBuffer)
                    GL33.glBufferData(GL33.GL_ARRAY_BUFFER, positions, GL33.GL_STATIC_DRAW)
                    GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 0, 0)
                }

                attributes[GltfAttribute.NORMAL]?.get<Vector3f>()?.run {
                    val normals = BufferUtils.createFloatBuffer(this.size * 3)
                    for (n in this) normals.put(n.x()).put(n.y()).put(n.z())
                    normals.flip()

                    morphCommands += { array ->
                        for (i in 0 until this.size * 3) {
                            var value = this[i / 3].toArray()[i % 3]
                            array.forEachIndexed { j, percent ->
                                morphTargets[j][GltfAttribute.NORMAL]?.let {
                                    value += it[i] * percent
                                }
                            }
                            normals.put(i, value)
                        }
                        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, normalBuffer)
                        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, normals, GL33.GL_STATIC_DRAW)
                    }

                    normalBuffer = GL33.glGenBuffers()
                    GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, normalBuffer)
                    GL33.glBufferData(GL33.GL_ARRAY_BUFFER, normals, GL33.GL_STATIC_DRAW)
                    GL33.glVertexAttribPointer(5, 3, GL33.GL_FLOAT, false, 0, 0)

                    if (GltfAttribute.TANGENT !in attributes) {
                        val pos = attributes[GltfAttribute.POSITION]!!.get<Vector3f>()
                        val texCoords = attributes[GltfAttribute.TEXCOORD_0]!!.get<Pair<Float, Float>>()
                        val normal = this

                        val tangents = BufferUtils.createFloatBuffer(this.size * 4)

                        MikktspaceTangentGenerator.genTangSpaceDefault(object : MikkTSpaceContext {
                            override fun getNumFaces(): Int {
                                return positionsCount / 9
                            }

                            override fun getNumVerticesOfFace(face: Int): Int {
                                return 3
                            }

                            override fun getPosition(posOut: FloatArray, face: Int, vert: Int) {
                                val index = (face * 3) + vert
                                posOut[0] = pos[index].x
                                posOut[1] = pos[index].y
                                posOut[2] = pos[index].z
                            }

                            override fun getNormal(normOut: FloatArray, face: Int, vert: Int) {
                                val index = (face * 3) + vert
                                normOut[0] = normal[index].x
                                normOut[1] = normal[index].y
                                normOut[2] = normal[index].z
                            }

                            override fun getTexCoord(texOut: FloatArray, face: Int, vert: Int) {
                                val index = (face * 3) + vert
                                texOut[0] = texCoords[index].first
                                texOut[1] = texCoords[index].second
                            }

                            override fun setTSpaceBasic(tangent: FloatArray, sign: Float, face: Int, vert: Int) {
                                tangents
                                    .put(tangent[0])
                                    .put(tangent[1])
                                    .put(tangent[2])
                                    .put(-sign)
                            }

                            override fun setTSpace(
                                tangent: FloatArray?,
                                biTangent: FloatArray?,
                                magS: Float,
                                magT: Float,
                                isOrientationPreserving: Boolean,
                                face: Int,
                                vert: Int,
                            ) {
                            }
                        })

                        tangents.flip()
                        tangentBuffer = GL33.glGenBuffers()
                        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, tangentBuffer)
                        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, tangents, GL33.GL_STATIC_DRAW)
                        GL33.glVertexAttribPointer(8, 4, GL33.GL_FLOAT, false, 0, 0)
                    }
                }

                attributes[GltfAttribute.TANGENT]?.get<Vector4f>()?.run {
                    val tangents = BufferUtils.createFloatBuffer(this.size * 4)
                    for (t in this) {
                        tangents.put(t.x()).put(t.y()).put(t.z()).put(t.w())
                    }
                    tangents.flip()

                    morphCommands += { array ->
                        for (i in 0 until this.size * 3) {
                            var value = this[i / 3].toArray()[i % 3]
                            array.forEachIndexed { j, percent ->
                                morphTargets[j][GltfAttribute.TANGENT]?.let {
                                    value += it[i] * percent
                                }
                            }
                            tangents.put(i, value)
                        }
                        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, tangentBuffer)
                        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, tangents, GL33.GL_STATIC_DRAW)
                    }

                    tangentBuffer = GL33.glGenBuffers()
                    GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, tangentBuffer)
                    GL33.glBufferData(GL33.GL_ARRAY_BUFFER, tangents, GL33.GL_STATIC_DRAW)
                    GL33.glVertexAttribPointer(8, 4, GL33.GL_FLOAT, false, 0, 0)
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
                GL33.glVertexAttribPointer(7, 2, GL33.GL_FLOAT, false, 0, 0)


//                if (attributes[GltfAttribute.TEXCOORD_1] == null) {
//                    midCoordsBuffer = GL33.glGenBuffers()
//                    GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, midCoordsBuffer)
//                    GL33.glBufferData(GL33.GL_ARRAY_BUFFER, texCords, GL33.GL_STATIC_DRAW)
//                    GL33.glVertexAttribPointer(7, 2, GL33.GL_FLOAT, false, 0, 0)
//                }
            }

            if (attributes[GltfAttribute.TEXCOORD_1] != null) {
                attributes[GltfAttribute.TEXCOORD_1]?.get<Pair<Float, Float>>()?.run {
                    val texCords = BufferUtils.createFloatBuffer(this.size * 2)
                    for (t in this) texCords.put(t.first).put(t.second)
                    texCords.flip()

                    midCoordsBuffer = GL33.glGenBuffers()
                    GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, midCoordsBuffer)
                    GL33.glBufferData(GL33.GL_ARRAY_BUFFER, texCords, GL33.GL_STATIC_DRAW)
                    GL33.glVertexAttribPointer(7, 2, GL33.GL_FLOAT, false, 0, 0)

                }
            }

            GL20.glVertexAttribPointer(1, 4, GL33.GL_FLOAT, false, 0, 0)

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
                for (n in this) joints.put(n.x().toInt()).put(n.y().toInt()).put(n.z().toInt()).put(n.w().toInt())
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

        fun render(
            stack: PoseStack,
            node: Node,
            consumer: (ResourceLocation) -> Int,
        ) {
            if (morphTargets.isNotEmpty()) updateMorphTargets(node)

            val shader = GltfModel.SHADER
            //Всякие настройки смешивания, материалы и т.п.
            val texture = consumer(material.texture)

            if (!node.isAllHovered()) GL33.glVertexAttrib4f(
                1, material.color.x(), material.color.y(), material.color.z(), material.color.w()
            )
            else GL33.glVertexAttrib4f(1, 0f, 0.45f, 1f, 1f)

            var normal = 0
            var specular = 0

            if (areShadersEnabled) {
                //т.к. Iris использует отличные от Optifine id текстур стоит взять их из самого шейдера
                GL33.glGetUniformLocation(shader.id, "normals").takeIf { it != -1 }?.let {
                    GL33.glActiveTexture(COLOR_MAP_INDEX + GL33.glGetUniformi(shader.id, it))
                    normal = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
                    GL33.glBindTexture(GL33.GL_TEXTURE_2D, material.normalTexture.toTexture().id)
                }
                GL33.glGetUniformLocation(shader.id, "specular").takeIf { it != -1 }?.let {
                    GL33.glActiveTexture(COLOR_MAP_INDEX + GL33.glGetUniformi(shader.id, it))
                    specular = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
                    GL33.glBindTexture(GL33.GL_TEXTURE_2D, material.specularTexture.toTexture().id)
                }
            }

            GL13.glActiveTexture(COLOR_MAP_INDEX + 2)
            GL13.glBindTexture(GL33.GL_TEXTURE_2D, GltfManager.lightTexture.id)
            GL13.glActiveTexture(COLOR_MAP_INDEX)
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, texture)

            if (material.doubleSided) RenderSystem.disableCull()
            //Подключение VAO и IBO
            GL33.glBindVertexArray(vao)
            if (indexBuffer != -1) GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, indexBuffer)

            GL33.glEnableVertexAttribArray(0) // Вершины (или цвет)
            GL33.glEnableVertexAttribArray(2) // Текстурные координаты
            GL33.glEnableVertexAttribArray(5) // Нормали
            if (tangentBuffer != -1) GL33.glEnableVertexAttribArray(8) //Тангенты
            if (hasShaders) GL20.glEnableVertexAttribArray(7) //координаты для глубины (pbr)

            val modelView = Matrix4f(RenderSystem.getModelViewMatrix()).mul(stack.last().pose())

            //Матрица
            shader.MODEL_VIEW_MATRIX?.set(modelView)
            shader.MODEL_VIEW_MATRIX?.upload()

            //Нормали
            shader.getUniform("NormalMat")?.let {
                it.set(stack.last().normal())
                it.upload()
            }

            //Отрисовка
            if (indexBuffer != -1) GL33.glDrawElements(GL33.GL_TRIANGLES, indexCount, GL33.GL_UNSIGNED_INT, 0L)
            else GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, positionsCount)

            if (material.doubleSided) RenderSystem.enableCull()

            if (hasShaders) {
                //т.к. Iris использует отличные от Optifine id текстур стоит взять их из самого шейдера
                GL33.glGetUniformLocation(shader.id, "normals").takeIf { it != -1 }?.let {
                    GL33.glActiveTexture(COLOR_MAP_INDEX + GL33.glGetUniformi(shader.id, it))
                    GL33.glBindTexture(GL33.GL_TEXTURE_2D, normal)
                }
                GL33.glGetUniformLocation(shader.id, "specular").takeIf { it != -1 }?.let {
                    GL33.glActiveTexture(COLOR_MAP_INDEX + GL33.glGetUniformi(shader.id, it))
                    GL33.glBindTexture(GL33.GL_TEXTURE_2D, specular)
                }
            }

            GL13.glActiveTexture(COLOR_MAP_INDEX)
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0)
            GL33.glCullFace(GL33.GL_BACK)

            //Отключение параметров выше
            GL33.glDisableVertexAttribArray(0)
            GL33.glDisableVertexAttribArray(2)
            GL33.glDisableVertexAttribArray(5)
            if (tangentBuffer != -1) GL33.glDisableVertexAttribArray(8)
            if (hasShaders) GL20.glDisableVertexAttribArray(7)
        }

        private fun updateMorphTargets(node: Node) {
            val weights = node.transform.weights.toFloatArray()

            morphCommands.forEach { it(weights) }
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
                buffer.put(m.m00())
                buffer.put(m.m10())
                buffer.put(m.m20())
                buffer.put(m.m30())
                buffer.put(m.m01())
                buffer.put(m.m11())
                buffer.put(m.m21())
                buffer.put(m.m31())
                buffer.put(m.m02())
                buffer.put(m.m12())
                buffer.put(m.m22())
                buffer.put(m.m32())
                buffer.put(m.m03())
                buffer.put(m.m13())
                buffer.put(m.m23())
                buffer.put(m.m33())
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
            GL30.glDeleteBuffers(midCoordsBuffer)

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
        fun getAsFloattoArray() = (data as List<Float>).toFloatArray()
        fun getAsInttoArray() = (data as List<Int>).toIntArray()

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

val hasFirstPersonModel = isModLoaded("firstperson")

fun Vector3f.toArray() = floatArrayOf(x, y, z)
fun Vector4f.toArray() = floatArrayOf(x, y, z, w)