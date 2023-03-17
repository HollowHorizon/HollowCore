package ru.hollowhorizon.hc.client.models.fbx.raw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FBXElement {
    private final String name;
    private final FBXProperty<?>[] properties;
    private final FBXElement[] elements;
    private long id;

    public FBXElement(String name, long id, FBXProperty<?>[] properties, FBXElement[] elements) {
        this.name = name;
        this.id = id;
        this.properties = properties;
        this.elements = elements;
    }

    public boolean hasElement(String name) {
        for (FBXElement element : elements) {
            if (element.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public FBXElement getFirstElement(String name) {
        for (FBXElement element : elements) {
            if (element.name.equals(name)) return element;
        }
        return null;
    }

    @Override
    public String toString() {
        if (properties.length > 0) {
            return "[" + name + "]: $" + Arrays.toString(properties) + " \n " + Arrays.toString(elements) + "";
        } else {
            return "[" + name + "]: " + Arrays.toString(elements) + "";
        }
    }

    public String getName() {
        return name;
    }

    public String getPName() {
        return properties[1].getData();
    }

    public FBXElement[] getElements() {
        return elements;
    }

    public FBXProperty<?>[] getProperties() {
        return properties;
    }

    public List<FBXElement> getElements(String nodeAttribute) {
        List<FBXElement> elements = new ArrayList<>();
        for (FBXElement element : this.elements) {
            if (element.getName().equals(nodeAttribute)) {
                elements.add(element);
            }
        }
        return elements;
    }

    public long getId() {
        return this.id;
    }

    public FBXElement getElement(long parentId) {
        for (FBXElement element : this.elements) {
            if (element.getId() == parentId) {
                return element;
            }
        }

        throw new IllegalArgumentException("No element with id " + parentId + " found");
    }
}
