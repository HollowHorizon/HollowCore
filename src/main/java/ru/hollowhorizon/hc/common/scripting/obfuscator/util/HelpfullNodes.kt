package ru.hollowhorizon.hc.common.scripting.obfuscator.util

import ru.hollowhorizon.hc.common.scripting.obfuscator.IKtNode
import ru.hollowhorizon.hc.common.scripting.obfuscator.general.KtField
import ru.hollowhorizon.hc.common.scripting.obfuscator.general.KtType

class StartNode(var nextNode: IKtNode<*>? = null): IKtNode<StartNode> {
    override fun replace(new: StartNode) {
        nextNode = new.nextNode
    }
}

class EndNode(var prevNode: IKtNode<*>): IKtNode<EndNode> {

    override fun replace(new: EndNode) {
        prevNode = new.prevNode
    }
}

fun createEmptyNode(): StartNode {
    val startNode = StartNode()
    val endNode = EndNode(startNode)
    startNode.nextNode = endNode
    return startNode
}

class SimpleNode(var currentNode: IKtNode<*>): IKtNode<SimpleNode> {
    var prevNode: IKtNode<*>? = null
    var nextNode: IKtNode<*>? = null

    override fun replace(new: SimpleNode) {
        prevNode = new.prevNode
        currentNode = new.currentNode
        nextNode = new.nextNode
    }

}

class AsNode(var fromNode: KtField, var toNode: KtField): IKtNode<AsNode> {

    override fun replace(new: AsNode) {
        fromNode = new.fromNode
        toNode = new.toNode
    }

}

class LoopNode(var condition: IKtNode<*>): IKtNode<LoopNode> {
    var body: IKtNode<*>? = null

    override fun replace(new: LoopNode) {
        condition = new.condition
        body = new.body
    }

}

class IfNode(var condition: IKtNode<*>): IKtNode<IfNode> {
    var body: IKtNode<*>? = null
    var elseIfNodes: List<IfNode>? = null
    var elseBody: IKtNode<*>? = null

    override fun replace(new: IfNode) {
        condition = new.condition
        body = new.body
        elseIfNodes = new.elseIfNodes
        elseBody = new.elseBody
    }

}

class WhenNode(var condition: IKtNode<*>): IKtNode<WhenNode> {
    var cases: List<WhenCaseNode>? = null

    override fun replace(new: WhenNode) {
        condition = new.condition
        cases = new.cases
    }

}

class WhenCaseNode(var condition: IKtNode<*>): IKtNode<WhenCaseNode> {
    var body: IKtNode<*>? = null

    override fun replace(new: WhenCaseNode) {
        condition = new.condition
        body = new.body
    }

}

class TryNode(var body: IKtNode<*>): IKtNode<TryNode> {
    var catchNodes: List<CatchNode>? = null
    var finallyNode: IKtNode<*>? = null

    override fun replace(new: TryNode) {
        body = new.body
        catchNodes = new.catchNodes
        finallyNode = new.finallyNode
    }

}

class CatchNode(var exceptionType: KtType): IKtNode<CatchNode> {
    var body: IKtNode<*>? = null

    override fun replace(new: CatchNode) {
        exceptionType = new.exceptionType
        body = new.body
    }

}

class InNode(var fromNode: IKtNode<*>, var toNode: IKtNode<*>): IKtNode<InNode> {

    override fun replace(new: InNode) {
        fromNode = new.fromNode
        toNode = new.toNode
    }

}

class GetterNode(var body: IKtNode<*>): IKtNode<GetterNode> {
    override fun replace(new: GetterNode) {
        body = new.body
    }
}

class SetterNode(var body: IKtNode<*>): IKtNode<SetterNode> {
    override fun replace(new: SetterNode) {
        body = new.body
    }
}

class SuperNode(var superType: KtType): IKtNode<SuperNode> {
    override fun replace(new: SuperNode) {
        superType = new.superType
    }
}

class LambdaNode(var parameters: KtType, var body: IKtNode<*>): IKtNode<LambdaNode> {
    override fun replace(new: LambdaNode) {
        body = new.body
    }
}

class AnnotationNode(var annotationType: KtType, var annotationParameters: List<KtType>): IKtNode<AnnotationNode> {
    override fun replace(new: AnnotationNode) {
        annotationType = new.annotationType
        annotationParameters = new.annotationParameters
    }
}