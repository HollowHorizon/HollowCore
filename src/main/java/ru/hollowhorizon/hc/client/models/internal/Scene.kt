package ru.hollowhorizon.hc.client.models.internal

data class Scene(
    val nodes: List<Node>,
) {

    fun transformSkinning(stack: RenderCommands) {
        nodes.forEach { it.transformSkinning(stack) }
    }

    fun compile(context: RenderCommands) {
        nodes.forEach { it.compile(context) }
    }
}