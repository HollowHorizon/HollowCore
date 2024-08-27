package ru.hollowhorizon.hc.client.models.internal

data class Model(
    val scene: Int,
    val scenes: List<Scene>,
    val animations: List<Animation>,
    val materials: Set<Material>,
) {
    fun initGl() {
        walkNodes().forEach { it.mesh?.primitives?.forEach { it.init() } }
    }

    fun walkNodes(): List<Node> {
        val nodes = mutableListOf<Node>()
        fun walk(node: Node) {
            nodes += node
            node.children.forEach { walk(it) }
        }
        scenes.flatMap { it.nodes }.forEach(::walk)
        return nodes
    }

    fun findNodeByIndex(index: Int): Node? {
        return walkNodes().find { it.index == index }
    }
}