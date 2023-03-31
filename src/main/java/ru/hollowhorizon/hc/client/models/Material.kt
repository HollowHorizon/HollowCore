package ru.hollowhorizon.hc.client.models

import net.minecraft.util.math.vector.Vector4f


class Material(
    var ambientColour: Vector4f?,
    var diffuseColour: Vector4f?,
    var specularColour: Vector4f?,
    var texture: Texture?,
    var reflectance: Float,
) {
    companion object {
        val DEFAULT_COLOUR = Vector4f(1.0f, 1.0f, 1.0f, 1.0f)
    }

    val shininess = 0f

    var normalMap: Texture? = null

    init {
        ambientColour = DEFAULT_COLOUR
        diffuseColour = DEFAULT_COLOUR
        specularColour = DEFAULT_COLOUR
        texture = null
        reflectance = 0f
    }

    constructor() : this(DEFAULT_COLOUR, DEFAULT_COLOUR, DEFAULT_COLOUR, null, 0f)

    constructor(colour: Vector4f?, reflectance: Float) : this(colour, colour, colour, null, reflectance)

    constructor(texture: Texture?) : this(DEFAULT_COLOUR, DEFAULT_COLOUR, DEFAULT_COLOUR, texture, 0f)

    constructor(texture: Texture?, reflectance: Float) : this(
        DEFAULT_COLOUR,
        DEFAULT_COLOUR,
        DEFAULT_COLOUR,
        texture,
        reflectance
    )

    constructor(
        ambientColour: Vector4f?,
        diffuseColour: Vector4f?,
        specularColour: Vector4f?,
        reflectance: Float,
    ) : this(ambientColour, diffuseColour, specularColour, null, reflectance)



    fun isTextured(): Boolean {
        return texture != null
    }

    fun hasNormalMap(): Boolean {
        return normalMap != null
    }

}