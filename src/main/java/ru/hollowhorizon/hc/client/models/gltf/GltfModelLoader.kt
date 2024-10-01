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

import com.mojang.datafixers.util.Pair
import kotlinx.coroutines.*
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.client.resources.model.ModelBakery
import net.minecraft.client.resources.model.ModelState
import net.minecraft.client.resources.model.UnbakedModel
import net.minecraft.resources.ResourceLocation
import org.joml.*
import ru.hollowhorizon.hc.HollowCore.MODID
import ru.hollowhorizon.hc.client.models.internal.*
import ru.hollowhorizon.hc.client.utils.exists
import ru.hollowhorizon.hc.client.utils.isModLoaded
import ru.hollowhorizon.hc.client.utils.rl
import java.util.*
import java.util.function.Function


object GltfModelLoader {
    val TEXTURE_MAP = HashMap<ResourceLocation, DynamicTexture>()


    suspend fun load(file: GltfFile, location: ResourceLocation): Model {
        val skins = parseSkins(file)
        val materials = file.materials.map { material ->
            material.toMaterial(file, location)
        }

        val scenes = parseScenes(file, skins, materials)
        val animations = parseAnimations(file)

        return Model(file.scene, scenes, animations, materials.toSet()).apply {
            for (skin in skins) {
                for ((i, id) in skin.jointsIds.withIndex()) {
                    skin.joints[i] = walkNodes().first { it.index == id }
                }
                walkNodes().forEach { node ->
                    node.skin?.let { skin ->
                        node.mesh?.primitives?.forEach {
                            it.jointCount = skin.jointsIds.size
                        }
                    }
                }
            }
        }
    }

    suspend fun parse(resource: ResourceLocation): Model {
        val location = if (!resource.exists()) "$MODID:models/error.gltf".rl else resource

        val gltf = loadGltf(location)
        return load(gltf.getOrThrow(), location)
    }


    private fun parseSkins(file: GltfFile): List<Skin> {
        return file.skins.map { skin ->
            return@map Skin(
                skin.joints,
                Mat4fAccessor(skin.inverseBindMatrixAccessorRef!!).list
            )
        }
    }


    private suspend fun parseScenes(file: GltfFile, skins: List<Skin>, materials: List<Material>): List<Scene> {
        return file.scenes.map { scene ->
            val nodes = scene.nodes
            val parsedNodes = nodes.map { parseNode(file, it, file.nodes[it], skins, materials) }

            Scene(parsedNodes.awaitAll())
        }
    }

    private suspend fun parseNode(
        file: GltfFile,
        nodeIndex: Int,
        node: GltfNode,
        skins: List<Skin>,
        materials: List<Material>,
    ): Deferred<Node> {
        return coroutineScope {
            async {
                val children = node.children.map { parseNode(file, it, file.nodes[it], skins, materials) }
                val weights = ArrayList<Float>()
                val mesh = node.meshRef?.let { mesh ->
                    weights.addAll(mesh.weights ?: emptyList())
                    val primitives = mesh.primitives.map { prim ->
                        Primitive(
                            prim.attributes.map { it.key to file.accessors[it.value] }.toMap(),
                            if (prim.indices != -1) file.accessors[prim.indices] else null,
                            prim.mode,
                            if (prim.material != -1) materials[prim.material] else Material(),
                            prim.targets.map {
                                it.map {
                                    it.key to file.accessors[it.value].let { accessor ->
                                        when (it.key) {
                                            GltfMesh.Primitive.ATTRIBUTE_POSITION, GltfMesh.Primitive.ATTRIBUTE_NORMAL -> {
                                                Vec3fAccessor(accessor).list.flatMap { listOf(it.x, it.y, it.z) }
                                                    .toFloatArray()
                                            }

                                            GltfMesh.Primitive.ATTRIBUTE_TANGENT -> {
                                                Vec3fAccessor(accessor).list.flatMap { listOf(it.x, it.y, it.z, 1f) }
                                                    .toFloatArray()
                                            }

                                            else -> throw IllegalStateException("Unsupported morph target!")
                                        }
                                    }
                                }.toMap()
                            }
                        )
                    }

                    return@let Mesh(primitives, weights)
                }
                val skin = if (node.skin != -1) skins[node.skin] else null

                val transform = Transformation(
                    translation = node.translation?.let { Vector3f(it[0], it[1], it[2]) } ?: Vector3f(),
                    rotation = node.rotation?.let { Quaternionf(it[0], it[1], it[2], it[3]) } ?: Quaternionf(
                        0.0f,
                        0.0f,
                        0.0f,
                        1.0f
                    ),
                    scale = node.scale?.let { Vector3f(it[0], it[1], it[2]) } ?: Vector3f(1.0f, 1.0f, 1.0f),
                    weights = weights,
                    matrix = node.matrix?.let {
                        Matrix4f(
                            it[0], it[1], it[2], it[3],
                            it[4], it[5], it[6], it[7],
                            it[8], it[9], it[10], it[11],
                            it[12], it[13], it[14], it[15],
                        )
                    } ?: Matrix4f()
                )

                Node(nodeIndex, children.awaitAll(), transform, mesh, skin, node.name).apply {
                    this.children.forEach { it.parent = this }
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseChannel(
        file: GltfFile, channel: GltfAnimation.Channel, samplers: List<GltfAnimation.Sampler>,
    ): Channel {
        val accessors = file.accessors
        val sampler = samplers[channel.sampler]
        val timeValues = FloatAccessor(accessors[sampler.input]).list

        return Channel(
            node = channel.target.node,
            path = channel.target.path,
            times = timeValues.toList(),
            interpolation = sampler.interpolation,
            values = accessors[sampler.output]
        )
    }

    @Suppress("SENSELESS_COMPARISON")
    private fun parseAnimations(file: GltfFile): List<Animation> {
        return file.animations.filter { it.channels != null }.map { animation ->
            val channels = animation.channels.map { parseChannel(file, it, animation.samplers) }
            Animation(animation.name, channels)
        }
    }

    fun tryLoad(id: ResourceLocation, unbakedCache: MutableMap<ResourceLocation, UnbakedModel>): Boolean {
        if (id.namespace == "hollowcore") {
            unbakedCache[id] = object: UnbakedModel {
                override fun getDependencies(): MutableCollection<ResourceLocation> = mutableSetOf()

                override fun getMaterials(
                    modelGetter: Function<ResourceLocation, UnbakedModel>,
                    missingTextureErrors: MutableSet<Pair<String, String>>,
                ): MutableCollection<net.minecraft.client.resources.model.Material> {
                    return mutableSetOf()
                }

                override fun bake(
                    modelBakery: ModelBakery,
                    spriteGetter: Function<net.minecraft.client.resources.model.Material, TextureAtlasSprite>,
                    transform: ModelState,
                    location: ResourceLocation,
                ): BakedModel {
                    return runBlocking { BakedConverter.convert(parse("${id.namespace}:models/block/${id.path}.gltf".rl), spriteGetter) }
                }

            }
            return true
        }
        return false
    }

}

val NODE_GLOBAL_TRANSFORMATION_LOOKUP_CACHE = IdentityHashMap<Node, Matrix4f>()

var CURRENT_NORMAL = Matrix3f()

val hasFirstPersonModel = isModLoaded("firstperson")

fun Vector3f.toArray() = floatArrayOf(x, y, z)
fun Vector4f.toArray() = floatArrayOf(x, y, z, w)