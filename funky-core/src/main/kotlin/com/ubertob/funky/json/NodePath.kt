package com.ubertob.funky.json

sealed class NodePath()
object NodeRoot : NodePath()
data class Node(val nodeName: String, val parent: NodePath) : NodePath()


fun NodePath.getPath(): String =
    when (this) {
        NodeRoot -> "[root]"
        is Node -> "${parent.getPath()}/$nodeName"
    }