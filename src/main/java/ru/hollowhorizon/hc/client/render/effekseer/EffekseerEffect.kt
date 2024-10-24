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

package ru.hollowhorizon.hc.client.render.effekseer

import Effekseer.swig.EffekseerEffectCore
import java.io.IOException
import java.io.InputStream

@Suppress("unused")
open class EffekseerEffect(val impl: EffekseerEffectCore = EffekseerEffectCore()) :
    SafeFinalized<EffekseerEffectCore>(impl, EffekseerEffectCore::delete) {
    private var isLoaded: Boolean = false

    override fun close() {
        impl.delete()
    }

    @Throws(IOException::class)
    open fun load(stream: InputStream, amplifier: Float): Boolean {
        val bytes = stream.readAllBytes()
        return load(bytes, bytes.size, amplifier)
    }

    open fun load(data: ByteArray, length: Int, amplifier: Float): Boolean {
        isLoaded = impl.Load(data, length, amplifier)
        return isLoaded
    }

    @Throws(IOException::class)
    open fun loadTexture(stream: InputStream, index: Int, type: TextureType): Boolean {
        val bytes = stream.readAllBytes()
        return loadTexture(bytes, bytes.size, index, type)
    }

    open fun loadTexture(data: ByteArray, length: Int, index: Int, type: TextureType): Boolean {
        return impl.LoadTexture(data, length, index, type.impl)
    }

    @Throws(IOException::class)
    open fun loadCurve(stream: InputStream, index: Int): Boolean {
        val bytes = stream.readAllBytes()
        return loadCurve(bytes, bytes.size, index)
    }

    open fun loadCurve(data: ByteArray, length: Int, index: Int): Boolean {
        return impl.LoadCurve(data, length, index)
    }

    @Throws(IOException::class)
    open fun loadMaterial(stream: InputStream, index: Int): Boolean {
        val bytes = stream.readAllBytes()
        return loadMaterial(bytes, bytes.size, index)
    }

    open fun loadMaterial(data: ByteArray, length: Int, index: Int): Boolean {
        return impl.LoadMaterial(data, length, index)
    }

    @Throws(IOException::class)
    open fun loadModel(stream: InputStream, index: Int): Boolean {
        val bytes = stream.readAllBytes()
        return loadModel(bytes, bytes.size, index)
    }

    open fun loadModel(data: ByteArray, length: Int, index: Int): Boolean {
        return impl.LoadModel(data, length, index)
    }

    open fun isModelLoaded(index: Int): Boolean {
        return impl.HasModelLoaded(index)
    }

    fun isCurveLoaded(index: Int): Boolean {
        return impl.HasCurveLoaded(index)
    }

    fun isMaterialLoaded(index: Int): Boolean {
        return impl.HasMaterialLoaded(index)
    }

    fun isTextureLoaded(index: Int, type: TextureType): Boolean {
        return impl.HasTextureLoaded(index, type.impl)
    }

    fun curveCount(): Int {
        return impl.GetCurveCount()
    }

    fun modelCount(): Int {
        return impl.GetModelCount()
    }

    fun materialCount(): Int {
        return impl.GetMaterialCount()
    }

    fun textureCount(type: TextureType): Int {
        return impl.GetTextureCount(type.impl)
    }

    fun textureCount(): Int {
        var amt = 0
        for (value in TextureType.values()) {
            amt += impl.GetTextureCount(value.impl)
        }
        return amt
    }

    fun getTexturePath(index: Int, type: TextureType): String {
        return impl.GetTexturePath(index, type.impl)
    }

    fun getCurvePath(index: Int): String {
        return impl.GetCurvePath(index)
    }

    fun getMaterialPath(index: Int): String {
        return impl.GetMaterialPath(index)
    }

    fun getModelPath(index: Int): String {
        return impl.GetModelPath(index)
    }

    fun minTerm(): Int {
        return impl.GetTermMin()
    }

    fun maxTerm(): Int {
        return impl.GetTermMax()
    }
}