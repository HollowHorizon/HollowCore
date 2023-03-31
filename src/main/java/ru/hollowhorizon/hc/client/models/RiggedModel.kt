package ru.hollowhorizon.hc.client.models

import com.mojang.blaze3d.matrix.MatrixStack
import jassimp.AiMesh
import jassimp.AiScene
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.vertex.VertexBuffer
import net.minecraft.util.math.vector.Matrix4f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30


class RiggedModel(scene: AiScene, private val aiMeshes: Array<AiMesh>, val meshes: Array<Mesh>, var name: String) {
    private val bones: MutableMap<String, BoneInfo> = HashMap()
    private val animations: Array<Animation?>
    private val animationWrappers: Array<AnimationWrapper?>
    private val currentAnimation: Int
    var boneCount = 0
        private set
    private var lastFrame: Long = -1

    init {
        this.setupMeshes()
        setMeshBones()
        animations = arrayOfNulls<Animation>(scene.numAnimations)
        for (i in animations.indices) {
            animations[i] = Animation(scene, this, i)
        }
        animationWrappers = arrayOfNulls<AnimationWrapper>(animations.size)
        for (i in animationWrappers.indices) {
            animationWrappers[i] = AnimationWrapper(animations[i])
        }
        currentAnimation = 0
    }

    fun setupMeshes() {
        meshes.forEach { setupMesh(it) }
    }

    private fun setMeshBones() {
        for (k in meshes.indices) {
            val mesh = aiMeshes[k]
            val customMesh = meshes[k]
            for (i in mesh.bones.indices) {
                var boneID: Int
                val bone = mesh.bones[i]
                val name = bone.name
                println("Mesh name: " + mesh.name + " Bone: " + name)
                if (!bones.containsKey(name)) {
                    val info = BoneInfo(boneCount, bone.getOffsetMatrix(AssimpJomlProvider()))
                    boneID = boneCount
                    bones[name] = info
                    boneCount++
                    if (boneCount >= 100) break // TODO
                } else {
                    boneID = bones[name]!!.id
                }
                val numWeights = bone.numWeights
                for (j in 0 until numWeights) {
                    val weight = bone.boneWeights[j]
                    val id = weight.vertexId
                    val fWeight = weight.weight
                    val vertex = customMesh.getVertices()[id]
                    for (l in 0..3) {
                        if (vertex.bones[l] < 0) {
                            vertex.weights[l] = fWeight
                            vertex.bones[l] = boneID
                        }
                    }
                }
            }
        }
    }

    fun setupMesh(mesh: Mesh) {
        val builder: BufferBuilder = Tessellator.getInstance().builder
        builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL)
        for (vertex in mesh.getVertices()) {
            builder
                .vertex(vertex.position.x, vertex.position.y, vertex.position.z)
                .normal(vertex.normal.x.toFloat(), vertex.normal.y.toFloat(), vertex.normal.z.toFloat())
                .uv(
                    vertex.tex.x,
                    vertex.tex.y
                ) // Below this is optional, will later be abstracted out, just here for testing purposes.
                .color(vertex.bones[0], vertex.bones[1], vertex.bones[2], vertex.bones[3])
                .color(
                    vertex.weights[0] / 255.0f,
                    vertex.weights[1] / 255.0f,
                    vertex.weights[2] / 255.0f,
                    vertex.weights[3] / 255.0f
                )
                .endVertex()
        }
        builder.end()
        builder.clear()
        mesh.buffer.upload(builder)
        mesh.vAO = GL30.glGenVertexArrays()

        // TODO: update with
        mesh.buffer.bind()
        GL30.glBindVertexArray(mesh.vAO)
        GL20.glEnableVertexAttribArray(0)
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 64, 0)
        GL20.glEnableVertexAttribArray(1)
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 64, 12)
        GL20.glEnableVertexAttribArray(2)
        GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, 64, 24)
        GL20.glEnableVertexAttribArray(3)
        GL20.glVertexAttribPointer(3, 4, GL11.GL_INT, false, 64, 32)
        GL20.glEnableVertexAttribArray(4)
        GL20.glVertexAttribPointer(4, 4, GL11.GL_FLOAT, false, 64, 48)
        GL30.glBindVertexArray(0)
        VertexBuffer.unbind()
    }

    fun genMeshes(scene: AiScene?): Array<Mesh?> {
        return arrayOfNulls(0)
    }

    fun render(stack: MatrixStack, x: Double, y: Double, z: Double, partialTicks: Double) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)
        GL11.glPushMatrix()
        GL11.glPushClientAttrib(GL11.GL_CLIENT_ALL_ATTRIB_BITS)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST)
        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        if (lastFrame == -1L) lastFrame = System.currentTimeMillis()
        val current = System.currentTimeMillis()
        val delta = current - lastFrame
        lastFrame = current
        //ModelUtil.SHADER.bind()
        val wrapper = animationWrappers[currentAnimation]!!
        wrapper.update(delta.toDouble())
        val matrices: Array<Matrix4f> = wrapper.matrices
        for (i in matrices.indices) {
            //ModelUtil.SHADER.set("finalBonesMatrices[$i]", matrices[i])
        }
        GL11.glTranslated(x, y, z)
        for (mesh in meshes) {

            for (i in 0 until mesh.getTextures().size) {
                val texture = mesh.getTextures()[i]
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.id)
                break
            }
            //ModelUtil.SHADER.set("sampler", 0)
            mesh.buffer.bind()
            GL30.glBindVertexArray(mesh.vAO)
            /*GL11.glVertexPointer(3, GL11.GL_FLOAT, 32, 0);
            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            GL11.glNormalPointer(GL11.GL_FLOAT, 32, 12);
            GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
            GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 32, 24);
            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);*/
            mesh.buffer.draw(stack.last().pose(), GL11.GL_TRIANGLES)
            VertexBuffer.unbind()
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
            GL30.glBindVertexArray(0)
            /*GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
            GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
            GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);*/
        }
        //ModelUtil.SHADER.unbind()
        GL11.glTranslated(-x, -y, -z)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_LIGHTING)
        GL11.glPopClientAttrib()
        GL11.glPopAttrib()
        GL11.glPopMatrix()
    }

    fun getBones(): MutableMap<String, BoneInfo> {
        return bones
    }

    fun getAnimations(): Array<Animation?> {
        return animations
    }
}