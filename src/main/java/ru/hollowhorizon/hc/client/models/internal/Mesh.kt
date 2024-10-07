package ru.hollowhorizon.hc.client.models.internal

data class Mesh(
    val primitives: List<Primitive>,
    val weights: List<Float>,
) {
    fun transformSkinning(node: Node, commands: RenderCommands) {
        primitives.filter { it.hasSkinning }.forEach { it.transformSkinning(node, commands) }
    }
}