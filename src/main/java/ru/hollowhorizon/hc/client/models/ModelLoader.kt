package ru.hollowhorizon.hc.client.models

import jassimp.*
import net.minecraft.util.math.vector.*
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.security.NoSuchAlgorithmException
import java.util.*


object ModelLoader {
    @Throws(IOException::class)
    fun loadModel(model: String?, root: String, debug: Boolean): RiggedModel {
        val meshList: MutableList<Mesh> = ArrayList()
        val steps: MutableSet<AiPostProcessSteps> = HashSet()
        steps.add(AiPostProcessSteps.TRIANGULATE)
        steps.add(AiPostProcessSteps.FIX_INFACING_NORMALS)
        // steps.add(AiPostProcessSteps.FLIP_UVS); // zero two has infacing uvs
        val scene = Jassimp.importFile(model, steps)
        println(scene)
        val rootName: String = scene.getSceneRoot(AssimpJomlProvider()).name
        for (mesh in scene.meshes) {
            if (debug) {
                println("Mesh name: " + mesh.name)
            }
            val material = scene.materials[mesh.materialIndex]
            val textures: MutableList<Texture> = ArrayList()
            if (material.hasProperties(Collections.singleton(AiMaterial.PropertyKey.TEX_FILE))) {
                val numTextures = material.getNumTextures(AiTextureType.DIFFUSE)
                for (i in 0 until numTextures) {
                    val name = material.getTextureFile(AiTextureType.DIFFUSE, i)
                    try {
                        if (debug) {
                            println("Texture name: $name")
                        }
                        val image =
                            ImageUtil.createFlipped(ImageUtil.bufferedImageFromFile(File(root + File.separator + name)))
                        val texture = BufferedTexture(image)
                        // int id = ImageUtil.loadImage(image);
                        textures.add(
                            Texture(
                                texture.id,
                                AiTextureType.DIFFUSE,
                                image.width,
                                image.height
                            )
                        )
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: NoSuchAlgorithmException) {
                        e.printStackTrace()
                    }
                }
            }
            val numFaces = mesh.numFaces
            val vertices: MutableList<Vertex> = ArrayList()

            for (j in 0 until numFaces) {
                val numFacesIndices = mesh.getFaceNumIndices(j)
                for (k in 0 until numFacesIndices) {
                    var vertex: Vertex
                    val i = mesh.getFaceVertex(j, k)
                    val x = mesh.getPositionX(i)
                    val y = mesh.getPositionY(i)
                    val z = mesh.getPositionZ(i)
                    val normX = mesh.getNormalX(i)
                    val normY = mesh.getNormalX(i)
                    val normZ = mesh.getNormalX(i)
                    val flag = false
                    var u = 0.0f
                    var v = 0.0f
                    if (mesh.hasTexCoords(0)) {
                        u = mesh.getTexCoordU(i, 0)
                        v = mesh.getTexCoordV(i, 0)
                    }
                    vertex = Vertex(
                        Vector3d(x.toDouble(), y.toDouble(), z.toDouble()),
                        Vector3d(normX.toDouble(), normY.toDouble(), normZ.toDouble()),
                        Vector2f(u, v)
                    )
                    vertices.add(vertex)
                }
            }
            meshList.add(Mesh(vertices, mesh.indexBuffer, textures))
        }
        val meshArray = arrayOfNulls<Mesh>(meshList.size)
        for (i in meshArray.indices) {
            meshArray[i] = meshList[i]
        }
        val meshes = arrayOfNulls<AiMesh>(scene.numMeshes)
        for (i in meshes.indices) {
            meshes[i] = scene.meshes[i]
        }
        val rigged = scene.numAnimations > 0 // TODO: improve!

        /*Animation animation = new Animation(scene, model1);
        AnimationWrapper wrapper = new AnimationWrapper(animation);*/
        // return new Model(null, meshArray, name1);
        return RiggedModel(scene, meshes.requireNoNulls(), meshArray.requireNoNulls(), rootName)
    }
}

class AssimpJomlProvider : AiWrapperProvider<Vector3f, Matrix4f, Color, SourceNode, Quaternion> {
    override fun wrapVector3f(byteBuffer: ByteBuffer, i: Int, i1: Int): Vector3f {
        val buffer: FloatBuffer = byteBuffer.asFloatBuffer()
        return Vector3f(
            if (i1 > 0) buffer.get(i) else 0.0f,
            if (i1 > 1) buffer.get(i + 1) else 0.0f,
            if (i1 > 2) buffer.get(i + 2) else 0.0f
        )
    }

    override fun wrapMatrix4f(floats: FloatArray): Matrix4f {
        return Matrix4f(floats)
    }

    override fun wrapColor(byteBuffer: ByteBuffer, i: Int): Color {
        return Color(byteBuffer.get(i).toInt(), byteBuffer.get(i + 1).toInt(), byteBuffer.get(i + 2).toInt(), byteBuffer.get(i + 3).toInt())
    }

    override fun wrapSceneNode(o: Any, o1: Any, ints: IntArray, s: String): SourceNode {
        val matrix: Matrix4f = o1 as Matrix4f
        return SourceNode(if (o == null) null else o as SourceNode, s, ints, matrix)
    }

    override fun wrapQuaternion(byteBuffer: ByteBuffer, i: Int): Quaternion {
        try {
            val buffer: FloatBuffer = byteBuffer.asFloatBuffer()
            val limit: Int = buffer.limit()
            val f1 = if (i > limit) buffer.get(i) else 0.0f
            // System.out.println("f1: " + f1);
            val f2 = if (i + 1 > limit) buffer.get(i + 1) else 0.0f
            // System.out.println("f2: " + f2);
            val f3 = if (i + 2 > limit) buffer.get(i + 2) else 0.0f
            // System.out.println("f3: " + f3);
            val f4 = if (i + 3 > limit) buffer.get(i + 3) else 0.0f
            // System.out.println("f4: " + f4);
            return Quaternion(f1, f2, f3, f4)
        } catch (ignored: IndexOutOfBoundsException) {
        } // why does this happen???????
        return Quaternion.ONE.copy()
    }
}

class Color(val r: Int, val g: Int, val b: Int, val a: Int) {
    constructor(r: Byte, g: Byte, b: Byte, a: Byte) : this(r.toInt(), g.toInt(), b.toInt(), a.toInt())
}

class SourceNode(val parent: SourceNode?, val name: String, val meshes: IntArray, val transformation: Matrix4f) {
    private val children: MutableList<SourceNode> = ArrayList()

    init {
        parent?.addChild(this)
    }

    fun addChild(child: SourceNode) {
        children.add(child)
    }

    fun getChildren(): List<SourceNode> {
        return children
    }
}