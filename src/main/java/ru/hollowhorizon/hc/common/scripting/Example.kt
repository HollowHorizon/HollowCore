package ru.hollowhorizon.hc.common.scripting

class NodeProcessor {
    var currentNodes = mutableListOf<Node>()
}

abstract class Node {
    val children = mutableListOf<Node>()

    abstract fun tick()

    fun finish(processor: NodeProcessor) {
        processor.currentNodes.remove(this)
        processor.currentNodes.addAll(children)
    }

    class Simple : Node() {
        override fun tick() {}
    }
}

fun main() {
    val node = Node.Simple().apply {
        children.addAll(
            listOf(
                Node.Simple(),
                Node.Simple().apply {
                    children.add(Node.Simple())
                },
                Node.Simple(),
            )
        )
    }

    node.tick()
}