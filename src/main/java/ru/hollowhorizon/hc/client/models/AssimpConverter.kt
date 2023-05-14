package ru.hollowhorizon.hc.client.models

import net.minecraft.util.math.vector.Matrix4f
import net.minecraft.util.math.vector.Quaternion
import net.minecraft.util.math.vector.Vector3f
import org.lwjgl.assimp.*
import org.lwjgl.system.MemoryStack
import java.nio.IntBuffer

fun processVertices(aiMesh: AIMesh, vertices: MutableList<Float>) {
    val aiVertices = aiMesh.mVertices()
    while (aiVertices.remaining() > 0) {
        val aiVertex = aiVertices.get()
        vertices.add(aiVertex.x())
        vertices.add(aiVertex.y())
        vertices.add(aiVertex.z())
    }
}

fun processNormals(aiMesh: AIMesh, normals: MutableList<Float>) {
    val aiNormals = aiMesh.mNormals()
    while (aiNormals != null && aiNormals.remaining() > 0) {
        val aiNormal = aiNormals.get()
        normals.add(aiNormal.x())
        normals.add(aiNormal.y())
        normals.add(aiNormal.z())
    }
}

fun processTextCoords(aiMesh: AIMesh, textures: MutableList<Float>) {
    val textCoords = aiMesh.mTextureCoords(0)
    val numTextCoords = textCoords?.remaining() ?: 0
    for (i in 0 until numTextCoords) {
        val textCoord = textCoords!!.get()
        textures.add(textCoord.x())
        textures.add(1f - textCoord.y())
    }
}

fun processIndices(aiMesh: AIMesh, indices: MutableList<Int>) {
    val numFaces = aiMesh.mNumFaces()
    val aiFaces = aiMesh.mFaces()
    for (i in 0 until numFaces) {
        val aiFace = aiFaces[i]
        val buffer = aiFace.mIndices()
        while (buffer.remaining() > 0) {
            indices.add(buffer.get())
        }
    }
}

val AIMatrix4x4.mc: Matrix4f
    get() {
        return Matrix4f(
            floatArrayOf(
                this.a1(), this.a2(), this.a3(), this.a4(),
                this.b1(), this.b2(), this.b3(), this.b4(),
                this.c1(), this.c2(), this.c3(), this.c4(),
                this.d1(), this.d2(), this.d3(), this.d4(),
            )
        )
    }

fun processBones(
    aiMesh: AIMesh, boneList: MutableList<HollowBone>,
    boneIds: MutableList<Int>, weights: MutableList<Float>,
) {
    val weightSet = HashMap<Int, MutableList<HollowBone.VertexWeight>>()
    val numBones = aiMesh.mNumBones()
    val aiBones = aiMesh.mBones()

    for (i in 0 until numBones) {
        val aiBone = AIBone.create(aiBones!![i])
        val id = boneList.size
        val bone = HollowBone(
            id,
            aiBone.mName().dataString(),
            aiBone.mOffsetMatrix().mc
        )
        boneList.add(bone)
        val numWeights = aiBone.mNumWeights()
        val aiWeights = aiBone.mWeights()
        for (j in 0 until numWeights) {
            val aiWeight = aiWeights[j]
            val vw = HollowBone.VertexWeight(
                bone.boneId, aiWeight.mVertexId(),
                aiWeight.mWeight()
            )
            val vertexWeightList =
                weightSet[vw.vertexId] ?: ArrayList<HollowBone.VertexWeight>().apply { weightSet[vw.vertexId] = this }
            vertexWeightList.add(vw)
        }
    }
    val numVertices = aiMesh.mNumVertices()
    for (i in 0 until numVertices) {
        val vertexWeightList: List<HollowBone.VertexWeight>? = weightSet[i]
        val size = vertexWeightList?.size ?: 0
        for (j in 0 until 150) {
            if (j < size) {
                val vw: HollowBone.VertexWeight = vertexWeightList!![j]
                weights.add(vw.weight)
                boneIds.add(vw.boneId)
            } else {
                weights.add(0.0f)
                boneIds.add(0)
            }
        }
    }
}

fun processNode(aiNode: AINode, parent: HollowNode? = null): HollowNode {
    val name = aiNode.mName().dataString()
    val mMatrix = aiNode.mTransformation().mc
    val node = HollowNode(name, parent, mMatrix)

    val numChildren = aiNode.mNumChildren()
    val childrenIterator = aiNode.mChildren() ?: return node
    for (i in 0 until numChildren) {
        val child = AINode.create(childrenIterator[i])
        val childNode = processNode(child, node)
        node.addChild(childNode)
    }

    val aiMeshes = aiNode.mMeshes()
    val numMeshes = aiNode.mNumMeshes()
    for (i in 0 until numMeshes) {
        val index = aiMeshes!![i]

        println("Обнаружен меш у ноды (теоретически его быть не может, я пытался :D) $index")
    }

    return node
}

fun processMaterial(index: Int, material: AIMaterial, embeddedTextures: ArrayList<HollowTexture>): HollowMaterial {
    val stack = MemoryStack.stackPush()

    val aiTexturePath: AIString = AIString.callocStack(stack)
    Assimp.aiGetMaterialTexture(
        material, Assimp.aiTextureType_DIFFUSE, 0, aiTexturePath, null as IntBuffer?,
        null, null, null, null, null
    )
    val texturePath = aiTexturePath.dataString()

    return HollowMaterial(index, texturePath).apply {
        texture = embeddedTextures.getOrNull(index)
    }
}

fun processAnimations(scene: AIScene): ArrayList<HollowAnimation> {
    val animationList = ArrayList<HollowAnimation>()
    val numAnimations = scene.mNumAnimations()
    val aiAnimations = scene.mAnimations() ?: return animationList

    for (i in 0 until numAnimations) {
        val aiAnimation = AIAnimation.create(aiAnimations[i])
        val animation =
            HollowAnimation(aiAnimation.mName().dataString(), aiAnimation.mDuration(), aiAnimation.mTicksPerSecond())
        processAnimationData(animation, aiAnimation)
        animationList.add(animation)
    }

    return animationList
}

fun processAnimationData(animation: HollowAnimation, aiAnimation: AIAnimation) {
    val numChannels = aiAnimation.mNumChannels()
    val aiChannels = aiAnimation.mChannels() ?: return

    for (i in 0 until numChannels) {
        val aiChannel = AINodeAnim.create(aiChannels[i])
        val name = aiChannel.mNodeName().dataString()

        val numFrames: Int = maxOf(
            aiChannel.mNumPositionKeys(),
            aiChannel.mNumRotationKeys(),
            aiChannel.mNumScalingKeys()
        )
        val translationKeys = aiChannel.mPositionKeys()
        val rotationKeys = aiChannel.mRotationKeys()
        val scalingKeys = aiChannel.mScalingKeys()

        for (index in 0 until numFrames) {
            val translation = if (index < aiChannel.mNumPositionKeys()) {
                val aiVecKey = translationKeys!![index]
                val vec = aiVecKey.mValue()
                Vector3f(vec.x(), vec.y(), vec.z())
            } else {
                Vector3f(0f, 0f, 0f)
            }

            val rotation = if (index < aiChannel.mNumRotationKeys()) {
                val quatKey = rotationKeys!![index]
                val aiQuat = quatKey.mValue()
                Quaternion(aiQuat.x(), aiQuat.y(), aiQuat.z(), aiQuat.w())
            } else Quaternion(0f, 0f, 0f, 0f)

            val scale = if (index < aiChannel.mNumScalingKeys()) {
                val aiVecKey = scalingKeys!![index]
                val vec = aiVecKey.mValue()
                Vector3f(vec.x(), vec.y(), vec.z())
            } else Vector3f(1f, 1f, 1f)

            animation.channels.computeIfAbsent(index) { arrayListOf() }.add(HollowAnimation.ChannelFrame(name, translation, rotation, scale))
        }
    }

    val numMeshChannels = aiAnimation.mNumMeshChannels()
    val aiMeshChannels = aiAnimation.mMeshChannels() ?: return

    for (i in 0 until numMeshChannels) {
        val aiMeshChannel = AIMeshAnim.create(aiMeshChannels[i])
        val name = aiMeshChannel.mName().dataString()

        println("name (mesh): $name")
    }

    val numMorphChannels = aiAnimation.mNumMorphMeshChannels()
    val aiMorphMeshChannels = aiAnimation.mMorphMeshChannels() ?: return

    for (i in 0 until numMorphChannels) {
        val aiMorphMeshChannel = AIMeshMorphAnim.create(aiMorphMeshChannels[i])
        val name = aiMorphMeshChannel.mName().dataString()

        println("name (morph): $name")
    }
}
