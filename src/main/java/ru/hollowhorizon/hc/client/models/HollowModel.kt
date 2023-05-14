package ru.hollowhorizon.hc.client.models

import com.mojang.blaze3d.matrix.MatrixStack
import kotlinx.serialization.Serializable
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.texture.NativeImage
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.vector.Matrix3f
import net.minecraft.util.math.vector.Matrix4f
import net.minecraft.util.math.vector.Quaternion
import net.minecraft.util.math.vector.Vector3f
import net.minecraft.util.math.vector.Vector4f
import org.lwjgl.assimp.AIMaterial
import org.lwjgl.assimp.AIMesh
import org.lwjgl.assimp.AIScene
import org.lwjgl.assimp.AITexture
import org.lwjgl.system.MemoryUtil
import ru.hollowhorizon.hc.client.utils.mc
import ru.hollowhorizon.hc.client.utils.nbt.ForMatrix4f
import ru.hollowhorizon.hc.client.utils.use
import java.nio.ByteBuffer

class HollowModel(scene: AIScene) {
    val animations: ArrayList<HollowAnimation>
    val materials: ArrayList<HollowMaterial>
    val root: HollowNode
    var currentAnimation: HollowAnimation? = null
    var animTick = 0

    init {
        val bones = ArrayList<HollowBone>()
        val numMeshes = scene.mNumMeshes()
        val aiMeshes = scene.mMeshes()!!
        val rawMeshes = arrayOfNulls<HollowMesh>(numMeshes)
        for (i in 0 until numMeshes) {
            val aiMesh = AIMesh.create(aiMeshes[i])
            rawMeshes[i] = processAnimatedMesh(aiMesh, bones)
        }

        val meshes = rawMeshes.filterNotNull().toTypedArray()

        val embeddedTextures = processEmbeddedTextures(scene)

        materials = processMaterials(scene, embeddedTextures)

        root = processNode(scene.mRootNode()!!)

        animations = processAnimations(scene)

        currentAnimation = animations.firstOrNull()

        root.bindMeshAndBones(meshes, bones)
    }

    private fun processEmbeddedTextures(scene: AIScene): ArrayList<HollowTexture> {
        val textures = ArrayList<HollowTexture>()
        val numTextures = scene.mNumTextures()
        val aiTextures = scene.mTextures()!!
        for (i in 0 until numTextures) {
            val texture = AITexture.create(aiTextures[i])
            val data = MemoryUtil.memByteBuffer(texture.pcData(0).address0(), texture.mWidth())
            textures.add(HollowTexture(data))
        }
        return textures
    }

    private fun processMaterials(
        scene: AIScene,
        embeddedTextures: ArrayList<HollowTexture>,
    ): ArrayList<HollowMaterial> {
        val materials = ArrayList<HollowMaterial>()
        val numMaterials = scene.mNumMaterials()
        val aiTextures = scene.mMaterials()!!
        for (i in 0 until numMaterials) {
            val mat = AIMaterial.create(aiTextures[i])
            materials.add(processMaterial(i, mat, embeddedTextures))
        }
        return materials
    }

    private fun processAnimatedMesh(aiMesh: AIMesh, bones: MutableList<HollowBone>): HollowMesh {
        val vertices = ArrayList<Float>()
        val textures = ArrayList<Float>()
        val normals = ArrayList<Float>()
        val indices = ArrayList<Int>()
        val weights = ArrayList<Float>()
        val boneIds = ArrayList<Int>()
        processVertices(aiMesh, vertices)
        processNormals(aiMesh, normals)
        processTextCoords(aiMesh, textures)
        processIndices(aiMesh, indices)
        processBones(aiMesh, bones, boneIds, weights)

        return HollowMesh(
            aiMesh.mName().dataString(),
            vertices.toFloatArray(),
            textures.toFloatArray(),
            normals.toFloatArray(),
            indices.toIntArray(),
            boneIds.toIntArray(),
            weights.toFloatArray(),
            aiMesh.mMaterialIndex()
        )
    }


}

@Serializable
open class HollowMesh(
    val name: String,
    val positions: FloatArray,
    val texCoords: FloatArray,
    val normals: FloatArray,
    val indices: IntArray,
    val boneIds: IntArray,
    val weights: FloatArray,
    val materialIndex: Int,
) {

    fun render(buffers: IRenderTypeBuffer, model: HollowModel, matrix: Matrix4f, normalMat: Matrix3f, light: Int) {
        val buffer = buffers.getBuffer(RenderType.entityTranslucent(model.materials[materialIndex].texture!!.location))

        for (i in indices.indices) {
            val vertexIndex = indices[i] * 3
            val textureIndex = indices[i] * 2

            val vector = Vector4f(positions[vertexIndex], positions[vertexIndex + 1], positions[vertexIndex + 2], 1f)
            val normal = Vector3f(normals[vertexIndex], normals[vertexIndex + 1], normals[vertexIndex + 2])

            normal.transform(normalMat)

            vector.transform(matrix)
            buffer
                .vertex(
                    vector.x(),
                    vector.y(),
                    vector.z(),
                    1.0f,
                    1.0f,
                    1.0f,
                    1.0f,
                    texCoords[textureIndex],
                    texCoords[textureIndex + 1],
                    OverlayTexture.NO_OVERLAY,
                    light,
                    normal.x(),
                    normal.y(),
                    normal.z()
                )

        }

    }
}

@Serializable
class HollowBone(
    val boneId: Int,
    val name: String,
    val matrix: @Serializable(ForMatrix4f::class) Matrix4f,
) {
    class VertexWeight(val boneId: Int, var vertexId: Int, var weight: Float)
}

class HollowMaterial(
    val index: Int,
    val texturePath: String,
) {
    var texture: HollowTexture? = null
}

class HollowTexture(val data: ByteBuffer) : DynamicTexture(NativeImage.read(data)) {
    val location: ResourceLocation = mc.textureManager.register("entity_material", this)
}

@Serializable
class HollowNode(
    val name: String,
    val parent: HollowNode?,
    val nodeMatrix: @Serializable(ForMatrix4f::class) Matrix4f,
) {
    val children = ArrayList<HollowNode>()
    val meshes: ArrayList<HollowMesh> = ArrayList()
    val bones: ArrayList<HollowBone> = ArrayList()

    fun addChild(childNode: HollowNode) = children.add(childNode)

    fun find(name: String): HollowNode? {
        if (this.name == name) return this
        return children.find { it.find(name) != null }
    }

    fun print(indent: String = "") {
        println("$indent$name")
        children.forEach { it.print("$indent  ") }
    }

    fun bindMeshAndBones(meshes: Array<HollowMesh>, bones: ArrayList<HollowBone>) {
        meshes.forEach { mesh ->
            if (mesh.name == name) this.meshes.add(mesh)
        }
        bones.forEach { bone ->
            if (bone.name == name) this.bones.add(bone)
        }
        children.forEach { it.bindMeshAndBones(meshes, bones) }
    }

    fun render(buffers: IRenderTypeBuffer, model: HollowModel, stack: MatrixStack, light: Int) {
        stack.use {
            val pose = stack.last().pose()
            val normal = stack.last().normal()

//            if(model.currentAnimation != null) {
//                val anim = model.currentAnimation!!.getAt(model.animTick, name)
//                val matrix = nodeMatrix
//
//                matrix.multiply(Matrix4f.createTranslateMatrix(anim.translate.x(), anim.translate.y(), anim.translate.z()))
//                matrix.multiply(anim.rotate)
//                matrix.multiply(Matrix4f.createScaleMatrix(anim.scale.x(), anim.scale.y(), anim.scale.z()))
//
//                pose.multiply(matrix)
//
//                model.animTick++
//
//                if(model.animTick >= model.currentAnimation!!.channels.size) model.animTick = 0
//            } else
            pose.multiply(nodeMatrix)

            meshes.forEach { mesh ->
                mesh.render(buffers, model, pose, normal, light)
            }
            children.forEach { it.render(buffers, model, stack, light) }
        }
    }
}

class HollowAnimation(
    val name: String,
    val duration: Double,
    val ticksPerSec: Double,
) {
    val channels = HashMap<Int, ArrayList<ChannelFrame>>()

    fun getAt(tick: Int, channel: String): ChannelFrame {
        return channels[tick]?.find { it.name == channel } ?: ChannelFrame(name, Vector3f(0f, 0f, 0f), Quaternion(0f, 0f, 0f, 0f), Vector3f(1f, 1f, 1f))
    }

    class ChannelFrame(
        val name: String,
        val translate: Vector3f,
        val rotate: Quaternion,
        val scale: Vector3f,
    )
}