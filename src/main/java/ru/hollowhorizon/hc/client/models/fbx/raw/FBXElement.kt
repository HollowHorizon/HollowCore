package ru.hollowhorizon.hc.client.models.fbx.raw

class FBXElement(val name: String, val properties: Array<FBXProperty<*>>, val elements: Array<FBXElement>) {

    fun getElementByName(name: String): FBXElement? {
        for (element in elements) {
            if (element.name == name) return element
        }
        return null
    }
}
