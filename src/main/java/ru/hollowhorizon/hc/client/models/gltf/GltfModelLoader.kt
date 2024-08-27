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

import kotlinx.coroutines.runBlocking
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.resources.ResourceLocation
import org.joml.*
import ru.hollowhorizon.hc.HollowCore.MODID
import ru.hollowhorizon.hc.client.models.internal.*
import ru.hollowhorizon.hc.client.utils.*
import java.util.*


object GltfModelLoader {
    val TEXTURE_MAP = HashMap<ResourceLocation, DynamicTexture>()


    fun parse(file: GltfFile, location: ResourceLocation): Model {
        val skins = parseSkins(file)
        val materials = runBlocking {
            file.materials.map { material ->
                material.toMaterial(file, location)
            }
        }

        val scenes = parseScenes(file, skins, materials)
        val animations = parseAnimations(file)

        return Model(file.scene, scenes, animations, materials.toSet())
    }

    fun parse(resource: ResourceLocation): Model {
        val location = if (!resource.exists()) "$MODID:models/error.gltf".rl else resource
        return parse(loadGltf(location).getOrThrow(), location)
    }


    private fun parseSkins(file: GltfFile): List<Skin> {
        return file.skins.map { skin ->
            return@map Skin(
                skin.joints,
                Mat4fAccessor(skin.inverseBindMatrixAccessorRef!!).list
            )
        }
    }


    private fun parseScenes(file: GltfFile, skins: List<Skin>, materials: List<Material>): List<Scene> {
        return file.scenes.map { scene ->
            val nodes = scene.nodes
            val parsedNodes = nodes.map { parseNode(file, it, file.nodes[it], skins, materials) }

            Scene(parsedNodes)
        }
    }

    private fun parseNode(
        file: GltfFile,
        nodeIndex: Int,
        node: GltfNode,
        skins: List<Skin>,
        materials: List<Material>,
    ): Node {
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
                                        Vec3fAccessor(accessor).list.flatMap { listOf(it.x, it.y, it.z) }.toFloatArray()
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
                ).apply { init() }
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

        return Node(nodeIndex, children, transform, mesh, skin, node.name).apply {
            this.children.forEach { it.parent = this }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseChannel(
        file: GltfFile, channel: GltfAnimation.Channel, samplers: List<GltfAnimation.Sampler>
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

}

val NODE_GLOBAL_TRANSFORMATION_LOOKUP_CACHE = IdentityHashMap<Node, Matrix4f>()

var CURRENT_NORMAL = Matrix3f()

val hasFirstPersonModel = isModLoaded("firstperson")

fun Vector3f.toArray() = floatArrayOf(x, y, z)
fun Vector4f.toArray() = floatArrayOf(x, y, z, w)