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

import ru.hollowhorizon.hc.client.render.effekseer.ParticleEmitter.PreDrawCallback


@Suppress("unused")
class ParticleEmitter(private val handle: Int, private val manager: EffekseerManager, val type: Type?) {
    enum class Type {
        WORLD,
        FIRST_PERSON_MAINHAND,
        FIRST_PERSON_OFFHAND,
        GUI // TODO: implement
    }

    var isVisible: Boolean = true
    private var isPaused: Boolean = false
    private var callback: PreDrawCallback? = null

    init {
        setVisibility(true)
        resume()
    }

    fun pause() {
        manager.impl.SetPaused(this.handle, true)
        isPaused = true
    }

    fun resume() {
        manager.impl.SetPaused(this.handle, false)
        isPaused = false
    }

    fun setVisibility(visible: Boolean) {
        manager.impl.SetShown(this.handle, visible)
        isVisible = visible
    }

    fun stop() {
        manager.impl.Stop(this.handle)
    }

    fun setProgress(frame: Float) {
        manager.impl.UpdateHandleToMoveToFrame(this.handle, frame)
    }

    fun setPosition(x: Float, y: Float, z: Float) {
        manager.impl.SetEffectPosition(this.handle, x, y, z)
    }

    fun setRotation(x: Float, y: Float, z: Float) {
        manager.impl.SetEffectRotation(this.handle, x, y, z)
    }

    fun setScale(x: Float, y: Float, z: Float) {
        manager.impl.SetEffectScale(this.handle, x, y, z)
    }

    fun setTransformMatrix(matrix: FloatArray) {
        manager.impl.SetEffectTransformMatrix(
            this.handle,
            matrix[0], matrix[1], matrix[2], matrix[3],
            matrix[4], matrix[5], matrix[6], matrix[7],
            matrix[8], matrix[9], matrix[10], matrix[11]
        )
    }

    fun setTransformMatrix(matrix: Array<FloatArray>) {
        var i = 0
        manager.impl.SetEffectTransformMatrix(
            this.handle,
            matrix[i][0], matrix[i][1], matrix[i][2], matrix[i++][3],
            matrix[i][0], matrix[i][1], matrix[i][2], matrix[i++][3],
            matrix[i][0], matrix[i][1], matrix[i][2], matrix[i][3]
        )
    }

    fun setBaseTransformMatrix(matrix: FloatArray) {
        manager.impl.SetEffectTransformBaseMatrix(
            this.handle,
            matrix[0], matrix[1], matrix[2], matrix[3],
            matrix[4], matrix[5], matrix[6], matrix[7],
            matrix[8], matrix[9], matrix[10], matrix[11]
        )
    }

    fun setBaseTransformMatrix(matrix: Array<FloatArray>) {
        var i = 0
        manager.impl.SetEffectTransformBaseMatrix(
            this.handle,
            matrix[i][0], matrix[i][1], matrix[i][2], matrix[i++][3],
            matrix[i][0], matrix[i][1], matrix[i][2], matrix[i++][3],
            matrix[i][0], matrix[i][1], matrix[i][2], matrix[i][3]
        )
    }

    fun exists(): Boolean {
        return manager.impl.Exists(this.handle)
    }

    fun setDynamicInput(index: Int, value: Float) {
        manager.impl.SetDynamicInput(this.handle, index, value)
    }

    fun getDynamicInput(index: Int): Float {
        return manager.impl.GetDynamicInput(this.handle, index)
    }

    fun interface PreDrawCallback {
        fun accept(emitter: ParticleEmitter, partialTicks: Float)

        fun andThen(after: PreDrawCallback) = PreDrawCallback { emitter: ParticleEmitter, partial: Float ->
            accept(emitter, partial)
            after.accept(emitter, partial)
        }
    }

    fun addPreDrawCallback(callback: PreDrawCallback) {
        this.callback = this.callback?.andThen(callback) ?: callback
    }

    fun runPreDrawCallbacks(partial: Float) {
        this.callback?.accept(this, partial)
    }
}