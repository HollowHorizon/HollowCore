package ru.hollowhorizon.hc.client.model.fbx.raw;

public class FBXElement {
    private final String name;
    private final FBXProperty<?>[] properties;
    private final FBXElement[] elements;

    public FBXElement(String name, FBXProperty<?>[] properties, FBXElement[] elements) {
        this.name = name;
        this.properties = properties;
        this.elements = elements;
    }

    public FBXElement getElementByName(String name) {
        for (FBXElement element : elements) {
            if(element.name.equals(name)) return element;
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public FBXElement[] getElements() {
        return elements;
    }

    public FBXProperty<?>[] getProperties() {
        return properties;
    }
}
