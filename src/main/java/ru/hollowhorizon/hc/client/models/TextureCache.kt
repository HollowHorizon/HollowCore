package ru.hollowhorizon.hc.client.models

class TextureCache private constructor() {
    private val texturesMap = HashMap<String, Texture>()

    @Throws(Exception::class)
    fun getTexture(path: String): Texture {
        var texture = texturesMap[path]
        if (texture == null) {
            texture = Texture(path)
            texturesMap[path] = texture
        }
        return texture
    }

    companion object {
        private var INSTANCE: TextureCache? = null

        @get:Synchronized
        val instance: TextureCache
            get() {
                return INSTANCE ?: run {
                    INSTANCE = TextureCache()
                    return INSTANCE!!
                }
            }
    }
}