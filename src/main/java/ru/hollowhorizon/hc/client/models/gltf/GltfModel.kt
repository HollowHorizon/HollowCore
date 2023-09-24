package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.math.Matrix3f
import com.mojang.math.Matrix4f
import net.minecraft.resources.ResourceLocation
import org.lwjgl.opengl.*
import java.util.*


open class GltfModel(val model: GltfTree.GLTFTree) {
    val clearModelData = ArrayList<Runnable>()
    val renderedGltfScenes = ArrayList<RenderedScene>(model.scenes.size)
    protected val rootNodeModelToCommands =
        IdentityHashMap<GltfTree.Node, Triple<List<Runnable>, List<Runnable>, List<Runnable>>>()
    protected val bufferViewModelToGlBufferView = IdentityHashMap<GltfTree.Buffer, Int>()

    constructor(location: ResourceLocation) : this(GltfTree.parse(location))

    init {
        prepareSceneModels()
    }

    private fun prepareSceneModels() {
        model.scenes.forEach { scene ->
            val renderedGltfScene = RenderedScene()
            renderedGltfScenes.add(renderedGltfScene)

            scene.nodes.forEach { node ->
                val (skinningCommands, vanillaRenders, shaderRenders) = rootNodeModelToCommands.computeIfAbsent(
                    node,
                    ::prepareNodeModel
                )
                renderedGltfScene.skinningCommands.addAll(skinningCommands)
                renderedGltfScene.vanillaRenderCommands.addAll(vanillaRenders)
                renderedGltfScene.shaderModRenderCommands.addAll(shaderRenders)
            }
        }
    }

    private fun prepareNodeModel(node: GltfTree.Node): Triple<List<Runnable>, List<Runnable>, List<Runnable>> {
        val nodeSkinningCommands = ArrayList<Runnable>()
        val vanillaNodeRenderCommands = ArrayList<Runnable>()
        val shaderModNodeRenderCommands = ArrayList<Runnable>()

        node.skin?.let { skin ->
            val jointCount: Int = skin.joints.size

            val transforms = arrayOfNulls<FloatArray>(jointCount)
            val invertNodeTransform = FloatArray(16)
            val bindShapeMatrix = FloatArray(16)

            val canSkinning = node.canHaveHardwareSkinning

            var jointMatrices = FloatArray(0)
            var jointMatrixBuffer = 0
            if (canSkinning) {
                val jointMatrixSize = jointCount * 16
                jointMatrices = FloatArray(jointMatrixSize)

                jointMatrixBuffer = GL15.glGenBuffers()
                clearModelData.add { GL15.glDeleteBuffers(jointMatrixBuffer) }
                GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, jointMatrixBuffer)
                GL15.glBufferData(
                    GL43.GL_SHADER_STORAGE_BUFFER,
                    jointMatrixSize.toLong() * java.lang.Float.BYTES,
                    GL15.GL_STATIC_DRAW
                )
            }

            val jointMatricesTransformCommands = ArrayList<Runnable>(jointCount)
            for (joint in 0..<jointCount) {
                transforms[joint] = FloatArray(16)
                val transform = transforms[joint]!!
                val inverseBindMatrix = FloatArray(16)
                if (canSkinning) {
                    jointMatricesTransformCommands.add {
                        mul4x4(invertNodeTransform, transform, transform)
                        skin.getInverseBindMatrix(joint, inverseBindMatrix)
                        mul4x4(transform, inverseBindMatrix, transform)
                        mul4x4(transform, bindShapeMatrix, transform)
                        System.arraycopy(transform, 0, jointMatrices, joint * 16, 16)
                    }
                    nodeSkinningCommands.add {
                        for (i in transforms.indices) {
                            System.arraycopy(
                                findGlobalTransform(skin.joints[i]),
                                0,
                                transforms[i]!!,
                                0,
                                16
                            )
                        }
                        invert4x4(findGlobalTransform(node), invertNodeTransform)
                        setIdentity4x4(bindShapeMatrix)
                        jointMatricesTransformCommands.parallelStream().forEach { obj: Runnable -> obj.run() }
                        GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, jointMatrixBuffer)
                        GL15.glBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER, 0, jointMatrices.floatBuffer)
                        GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, jointMatrixBuffer)
                    }
                } else {
                    jointMatricesTransformCommands.add {
                        mul4x4(invertNodeTransform, transform, transform)
                        skin.getInverseBindMatrix(joint, inverseBindMatrix)
                        mul4x4(transform, inverseBindMatrix, transform)
                        mul4x4(transform, bindShapeMatrix, transform)
                    }
                }
            }

            if (!canSkinning) {
                val jointMatricesTransformCommand = {
                    for (i in transforms.indices) {
                        System.arraycopy(
                            findGlobalTransform(skin.joints[i]),
                            0,
                            transforms[i]!!,
                            0,
                            16
                        )
                    }
                    invert4x4(findGlobalTransform(node), invertNodeTransform)
                    setIdentity4x4(bindShapeMatrix)
                    jointMatricesTransformCommands.parallelStream().forEach { obj: Runnable -> obj.run() }
                }
                vanillaNodeRenderCommands.add(jointMatricesTransformCommand)
                shaderModNodeRenderCommands.add(jointMatricesTransformCommand)
            }

            node.mesh?.let { mesh ->
                for (meshPrimitiveModel in mesh.primitives) {
                    processMeshPrimitiveModel(
                        clearModelData,
                        node,
                        mesh,
                        meshPrimitiveModel,
                        transforms,
                        nodeSkinningCommands,
                        vanillaNodeRenderCommands,
                        shaderModNodeRenderCommands
                    )
                }
            }

        }

        return Triple(nodeSkinningCommands, vanillaNodeRenderCommands, shaderModNodeRenderCommands)
    }

    private fun processMeshPrimitiveModel(
        clearModelData: ArrayList<Runnable>,
        node: GltfTree.Node,
        mesh: GltfTree.Mesh,
        meshPrimitiveModel: GltfTree.Primitive,
        transforms: Array<FloatArray?>,
        nodeSkinningCommands: ArrayList<Runnable>,
        vanillaNodeRenderCommands: ArrayList<Runnable>,
        shaderModNodeRenderCommands: ArrayList<Runnable>,
    ) {
        val attributes = meshPrimitiveModel.attributes

        val positions = attributes[GltfAttribute.POSITION]
        val normals = attributes[GltfAttribute.NORMAL]
        val tangents = attributes[GltfAttribute.TANGENT]

        val glTransformFeedback = GL40.glGenTransformFeedbacks()
        clearModelData.add { GL40.glDeleteTransformFeedbacks(glTransformFeedback) }
        GL40.glBindTransformFeedback(GL40.GL_TRANSFORM_FEEDBACK, glTransformFeedback)

        val glVertexArraySkinning = GL30.glGenVertexArrays()
        clearModelData.add { GL30.glDeleteVertexArrays(glVertexArraySkinning) }
        GL30.glBindVertexArray(glVertexArraySkinning)

        val morphTargets: Map<GltfAttribute, GltfTree.Buffer> = meshPrimitiveModel.attributes

        val jointsAccessorModel = attributes[GltfAttribute.JOINTS_0]!!
        bindArrayBufferViewModel(clearModelData, jointsAccessorModel)
        GL20.glVertexAttribPointer(
            skinning_joint,
            jointsAccessorModel.componentType.size,
            jointsAccessorModel.componentType.id,
            false,
            jointsAccessorModel.byteStride,
            jointsAccessorModel.byteOffset.toLong()
        )
        GL20.glEnableVertexAttribArray(skinning_joint)

        val weightsAccessorModel = attributes[GltfAttribute.WEIGHTS_0]!!
        bindArrayBufferViewModel(clearModelData, weightsAccessorModel)
        GL20.glVertexAttribPointer(
            skinning_weight,
            jointsAccessorModel.componentType.size,
            jointsAccessorModel.componentType.id,
            false,
            jointsAccessorModel.byteStride,
            jointsAccessorModel.byteOffset.toLong()
        )
        GL20.glEnableVertexAttribArray(skinning_weight)

        val targetAccessorDatas = ArrayList<GltfTree.Buffer>(morphTargets.size)
        if (createMorphTarget(morphTargets, targetAccessorDatas, GltfAttribute.POSITION)) {
            bindVec3FloatMorphed(
                clearModelData,
                node,
                mesh,
                nodeSkinningCommands,
                positions!!,
                targetAccessorDatas
            )
        } else {
            bindArrayBufferViewModel(clearModelData, positions!!)
        }
    }

    open fun bindVec3FloatMorphed(
        gltfRenderData: MutableList<Runnable>,
        nodeModel: GltfTree.Node,
        meshModel: GltfTree.Mesh,
        command: MutableList<Runnable>,
        baseAccessorModel: GltfTree.Buffer,
        targetAccessorDatas: List<GltfTree.Buffer>,
    ) {
        val morphedBufferViewData = baseAccessorModel.buffer()
        val glBufferView = GL15.glGenBuffers()
        gltfRenderData.add(Runnable { GL15.glDeleteBuffers(glBufferView) })
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferView)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, morphedBufferViewData, GL15.GL_STATIC_DRAW)
        val weights = FloatArray(targetAccessorDatas.size)
        val numComponents = 3
        val numElements: Int = baseAccessorModel.type.numComponents
        val morphingCommands: MutableList<Runnable> = ArrayList(numElements * numComponents)
//        for (element in 0 until numElements) {
//            for (component in 0 until numComponents) {
//                morphingCommands.add(Runnable {
//                    var r: Float = baseAccessorModel.get(element, component)
//                    for (i in weights.indices) {
//                        val target: AccessorFloatData? = targetAccessorDatas[i]
//                        if (target != null) {
//                            r += weights[i] * target.get(element, component)
//                        }
//                    }
//                    morphedAccessorData.set(element, component, r)
//                })
//            }
//        }
//        command.add(Runnable {
//            if (nodeModel.getWeights() != null) System.arraycopy(
//                nodeModel.getWeights(),
//                0,
//                weights,
//                0,
//                weights.size
//            ) else if (meshModel.getWeights() != null) System.arraycopy(
//                meshModel.getWeights(),
//                0,
//                weights,
//                0,
//                weights.size
//            )
//            morphingCommands.parallelStream()
//                .forEach { obj: Runnable -> obj.run() }
//            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferView)
//            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, morphedBufferViewData)
//        })
    }

    private fun createMorphTarget(
        morphTargets: Map<GltfAttribute, GltfTree.Buffer>,
        targetAccessorDatas: ArrayList<GltfTree.Buffer>,
        attributeName: GltfAttribute,
    ): Boolean {
        var isMorphableAttribute = false

        val accessorModel = morphTargets[attributeName]
        if (accessorModel != null) {
            isMorphableAttribute = true
            targetAccessorDatas.add(accessorModel)
        }

        return isMorphableAttribute
    }

    open fun bindArrayBufferViewModel(gltfRenderData: MutableList<Runnable>, bufferViewModel: GltfTree.Buffer) {
        val glBufferView = bufferViewModelToGlBufferView[bufferViewModel]
        if (glBufferView == null) {
            val glBufferViewNew = GL15.glGenBuffers()
            gltfRenderData.add(Runnable { GL15.glDeleteBuffers(glBufferViewNew) })
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferViewNew)
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, bufferViewModel.buffer(), GL15.GL_STATIC_DRAW)
            bufferViewModelToGlBufferView[bufferViewModel] = glBufferViewNew
        } else GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferView)
    }

    companion object {
        protected const val skinning_joint = 0
        protected const val skinning_weight = 1
        protected const val skinning_position = 2
        protected const val skinning_normal = 3
        protected const val skinning_tangent = 4
        protected const val skinning_out_position = 0
        protected const val skinning_out_normal = 1
        protected const val skinning_out_tangent = 2
        var CURRENT_POSE: Matrix4f? = null
        var CURRENT_NORMAL: Matrix3f? = null
    }
}

class RenderedScene {
    val skinningCommands = arrayListOf<Runnable>()
    val vanillaRenderCommands = arrayListOf<Runnable>()
    val shaderModRenderCommands = arrayListOf<Runnable>()
}