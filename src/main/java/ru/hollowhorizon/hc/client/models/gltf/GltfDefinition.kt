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

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector4f
import java.io.InputStream
import java.lang.reflect.Type

private typealias JsObject = Map<String, Any>

internal class Vector4Deserializer : JsonDeserializer<Vector4f> {
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Vector4f {
        val arr = json.asJsonArray
        return Vector4f(arr[0].asFloat, arr[1].asFloat, arr[2].asFloat, arr[3].asFloat)
    }
}

internal class Vector3Deserializer : JsonDeserializer<Vector3f> {
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Vector3f {
        val arr = json.asJsonArray
        return Vector3f(arr[0].asFloat, arr[1].asFloat, arr[2].asFloat)
    }
}

internal class QuaternionDeserializer : JsonDeserializer<Quaternionf> {
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Quaternionf {
        val arr = json.asJsonArray
        return Quaternionf(arr[0].asFloat, arr[1].asFloat, arr[2].asFloat, arr[3].asFloat)
    }
}

internal class Matrix4Deserializer : JsonDeserializer<Matrix4f> {
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Matrix4f {
        val arr = json.asJsonArray

        return Matrix4f().set(arr.map { it.asFloat }.toFloatArray())
    }
}

object GltfDefinition {
    private val GSON = GsonBuilder()
        .registerTypeAdapter(Vector3f::class.java, Vector3Deserializer())
        .registerTypeAdapter(Vector4f::class.java, Vector4Deserializer())
        .registerTypeAdapter(Quaternionf::class.java, QuaternionDeserializer())
        .registerTypeAdapter(Matrix4f::class.java, Matrix4Deserializer())
        .create()

    fun parse(fileStream: InputStream): GltfFile {
        return GSON.fromJson(fileStream.reader(), GltfFile::class.java)
    }
}


data class GltfFile(
    val extensionsUsed: List<String> = emptyList(),
    val extensionsRequired: List<String> = emptyList(),
    val accessors: List<GltfAccessor> = emptyList(),
    val animations: List<GltfAnimation> = emptyList(),
    val asset: JsObject = emptyMap(),
    val buffers: List<GltfBuffer> = emptyList(),
    val bufferViews: List<GltfBufferView> = emptyList(),
    val cameras: List<GltfCamera> = emptyList(),
    val images: List<GltfImage> = emptyList(),
    val materials: List<GltfMaterial> = emptyList(),
    val meshes: List<GltfMesh> = emptyList(),
    val nodes: List<GltfNode> = emptyList(),
    val samplers: List<GltfSampler> = emptyList(),
    val scene: Int? = null,
    val scenes: List<GltfScene> = emptyList(),
    val skins: List<GltfSkin> = emptyList(),
    val textures: List<GltfTexture> = emptyList(),
    val extensions: JsObject? = null,
    val extras: Any? = null,
) {
    companion object {
        const val GLB_FILE_MAGIC = 0x46546c67 // String: "glTF"
        const val GLB_CHUNK_MAGIC_JSON = 0x4e4f534a // String: "JSON"
        const val GLB_CHUNK_MAGIC_BIN = 0x004e4942 // String: "BIN"
    }

    var binaryBuffer: List<ByteArray>? = null

    override fun toString(): String {
        return "glTF(\n" +
                "  extensionsUsed = $extensionsUsed, \n" +
                "  extensionsRequired = $extensionsRequired, \n" +
                "  accessors = $accessors, \n" +
                "  animations = $animations, \n" +
                "  asset = $asset, \n" +
                "  buffers = $buffers, \n" +
                "  bufferViews = $bufferViews, \n" +
                "  cameras = $cameras, \n" +
                "  images = $images, \n" +
                "  materials = $materials, \n" +
                "  meshes = $meshes, \n" +
                "  nodes = $nodes, \n" +
                "  samplers = $samplers, \n" +
                "  scene = $scene, \n" +
                "  scenes = $scenes, \n" +
                "  skins = $skins, \n" +
                "  textures = $textures, \n" +
                "  extensions = $extensions, \n" +
                "  extras = $extras" +
                "\n)"
    }
}

data class GltfScene(
    val nodes: List<Int>? = null,
    val name: String? = null,
    val extensions: String? = null,
    val extras: Any? = null,
)

data class GltfNode(
    val camera: Int? = null,
    val children: List<Int> = emptyList(),
    val skin: Int? = null,
    val matrix: Matrix4f? = null,
    val mesh: Int? = null,
    val rotation: Quaternionf? = Quaternionf(0.0f, 0.0f, 0.0f, 1.0f),
    val scale: Vector3f? = Vector3f(1.0f, 1.0f, 1.0f),
    val translation: Vector3f? = Vector3f(),
    val weights: List<Float> = emptyList(),
    val name: String? = null,
    val extensions: Map<String, Any?>? = null,
    val extras: Any? = null,
)

data class GltfBuffer(
    val uri: String? = null,
    val byteLength: Int = 0,
    val name: String? = null,
    val extensions: String? = null,
    val extras: Any? = null,
)

data class GltfBufferView(
    val buffer: Int = 0,
    val byteOffset: Int? = 0,
    val byteLength: Int = 0,
    val byteStride: Int? = null,
    val target: Int? = 0,
    val name: String? = null,
    val extensions: String? = null,
    val extras: Any? = null,
)

data class GltfCamera(
    val orthographic: GltfOrthographicCamera? = null,
    val perspective: GltfPerspectiveCamera? = null,
    val type: GltfCameraType = GltfCameraType.orthographic,
    val name: String? = null,
    val extensions: String? = null,
    val extras: Any? = null,
)

data class GltfAccessor(
    val bufferView: Int? = 0,
    val byteOffset: Int? = 0,
    val componentType: Int = 0,
    val normalized: Boolean? = false,
    val count: Int = 0,
    val type: GltfType = GltfType.SCALAR,
    val max: List<Double> = emptyList(),
    val min: List<Double> = emptyList(),
    val sparse: GltfSparse? = null,
    val name: String? = null,
    val extensions: String? = null,
    val extras: Any? = null,
)

data class GltfSparse(
    val count: Int = 0,
    val indices: GltfAccessor?,
    val values: GltfAccessor?,
    val extensions: String? = null,
    val extras: Any? = null,
)

data class GltfMesh(
    val primitives: List<GltfPrimitive> = emptyList(),
    val weights: List<Double> = emptyList(),
    val name: String? = null,
    val extensions: String? = null,
    val extras: Any? = null,
)

data class GltfPrimitive(
    val attributes: Map<String, Int> = emptyMap(),
    val indices: Int? = null,
    val material: Int? = null,
    val mode: Int = 4,
    val targets: List<Map<String, Int>> = emptyList(),
    val extensions: String? = null,
    val extras: Any? = null,
)

data class GltfSkin(
    val inverseBindMatrices: Int? = 0,
    val joints: List<Int> = emptyList(),
    val skeleton: Int? = 0,
    val name: String? = null,
    val extensions: String? = null,
    val extras: Any? = null,
)

data class GltfTexture(
    val sampler: Int? = 0,
    val source: Int? = 0,
    val name: String? = null,
    val extensions: String? = null,
    val extras: Any? = null,
)

data class GltfImage(
    val uri: String? = null,
    val mimeType: String? = null,
    val bufferView: Int? = null,
    val name: String? = null,
    val extensions: String? = null,
    val extras: Any? = null,
)

data class GltfSampler(
    val magFilter: Int? = null,
    val minFilter: Int? = null,
    val wrapS: Int? = null,
    val wrapT: Int? = null,
    val name: String? = null,
    val extensions: String? = null,
    val extras: Any? = null,
)

data class GltfMaterial(
    val name: String? = null,
    val extensions: Any? = null,
    val extras: Any? = null,
    val pbrMetallicRoughness: GltfPbrMetallicRoughness? = null,
    val normalTexture: GltfNormalTextureInfo? = null,
    val occlusionTexture: GltfOcclusionTextureInfo? = null,
    val emissiveTexture: GltfTextureInfo? = null,
    val emissiveFactor: Vector3f? = Vector3f(0.0f, 0.0f, 0.0f),
    val alphaMode: GltfAlphaMode? = GltfAlphaMode.OPAQUE,
    val alphaCutoff: Double? = 0.5,
    val doubleSided: Boolean = false,
)

data class GltfPbrMetallicRoughness(
    val baseColorFactor: Vector4f = Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
    val baseColorTexture: GltfTextureInfo? = null,
    val metallicFactor: Double = 1.0,
    val roughnessFactor: Double = 1.0,
    val metallicRoughnessTexture: GltfTextureInfo? = null,
    val extensions: String? = null,
    val extras: Any? = null,
)

data class GltfTextureInfo(
    val index: Int = 0,
    val texCoord: Int = 0,
    val extensions: String? = null,
    val extras: Any? = null,
)

data class GltfNormalTextureInfo(
    val index: Int = 0,
    val texCoord: Int = 0,
    val scale: Double = 1.0,
    val extensions: String? = null,
    val extras: Any? = null,
)

data class GltfOcclusionTextureInfo(
    val index: Int = 0,
    val texCoord: Int = 0,
    val strength: Double = 1.0,
    val extensions: String? = null,
    val extras: Any? = null,
)

data class GltfPerspectiveCamera(
    val aspectRatio: Double,
    val yfov: Double,
    val znear: Double,
    val zfar: Double = Double.POSITIVE_INFINITY,
)

data class GltfOrthographicCamera(
    val xmag: Double,
    val ymag: Double,
    val zfar: Double,
    val znear: Double,
)


data class GltfAnimation(
    val name: String?,
    val channels: List<GltfAnimationChannel>,
    val samplers: List<GltfAnimationSampler>,
)

data class GltfAnimationChannel(
    val sampler: Int,
    val target: GltfChannelTarget,
)

data class GltfChannelTarget(
    val node: Int,
    val path: String,
)

data class GltfAnimationSampler(
    val input: Int,
    val interpolation: GltfInterpolation,
    val output: Int,
)

enum class GltfChannelPath {
    translation, rotation, scale, weights
}

enum class GltfInterpolation {
    LINEAR, STEP, CUBICSPLINE
}

enum class GltfCameraType {
    perspective, orthographic
}

enum class GltfAlphaMode {
    OPAQUE, MASK, BLEND
}

enum class GltfAttribute {
    POSITION, NORMAL, TANGENT, TEXCOORD_0, TEXCOORD_1, COLOR_0, JOINTS_0, JOINTS_1, WEIGHTS_0
}

enum class GltfComponentType(val id: Int, val size: Int) {
    BYTE(5120, 1),
    UNSIGNED_BYTE(5121, 1),
    SHORT(5122, 2),
    UNSIGNED_SHORT(5123, 2),
    UNSIGNED_INT(5125, 4),
    FLOAT(5126, 4);

    companion object {
        val conversionMap: Map<Int, GltfComponentType> = values().associateBy { it.id }

        fun fromId(value: Int) = conversionMap[value] ?: error("Invalid Component type value: $value")
    }
}

enum class GltfType(val numComponents: Int) {
    SCALAR(1),
    VEC2(2),
    VEC3(3),
    VEC4(4),
    MAT2(4),
    MAT3(9),
    MAT4(16)
}

enum class GltfMode(val code: Int) {
    POINTS(0x0),
    LINES(0x1),
    LINE_LOOP(0x2),
    LINE_STRIP(0x3),
    TRIANGLES(0x4),
    TRIANGLE_STRIP(0x5),
    TRIANGLE_FAN(0x6),
    QUADS(0x7),
    QUAD_STRIP(0x8),
    POLYGON(0x9);

    companion object {
        private val conversionMap: Map<Int, GltfMode> = values().associateBy { it.code }

        fun fromId(value: Int) = conversionMap[value] ?: error("Invalid GL mode: $value")
    }
}