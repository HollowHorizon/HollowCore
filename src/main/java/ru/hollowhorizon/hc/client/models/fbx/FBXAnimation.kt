package ru.hollowhorizon.hc.client.models.fbx

data class FBXAnimation(@JvmField val animationName: String, val animationId: Long, @JvmField val nodes: List<FBXCurveNode>) {
    private var currentFrame = 0

    fun setFrame(currentFrame: Int) {
        this.currentFrame = currentFrame - 1
        tickFrame()
    }

    fun tickFrame() {
        for (node in nodes) {
            if (node.updateValues(currentFrame)) {
                currentFrame = 0
                return
            }
        }
        currentFrame++
    }
}
