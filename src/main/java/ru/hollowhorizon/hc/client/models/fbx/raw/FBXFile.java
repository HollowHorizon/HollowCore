package ru.hollowhorizon.hc.client.models.fbx.raw;

import java.util.ArrayList;
import java.util.List;

public class FBXFile {
    private final String fileName;
    private final FBXElement[] elements;

    public FBXFile(String fileName, FBXElement[] elements) {
        this.fileName = fileName;
        this.elements = elements;
    }

    public FBXElement getElement(String name) {
        for (FBXElement element : elements) {
            if (element.getName().equals(name)) return element;
        }
        throw new IllegalArgumentException("No element with name " + name + " found");
    }

    public FBXElement getElement(long id) {
        for (FBXElement element : elements) {
            if (element.getId() == id) return element;
        }
        throw new IllegalArgumentException("No element with id " + id + " found");
    }

    public FBXElement[] getParents(long id) {
        FBXElement[] connections = getElement("Connections").getElements();

        List<FBXElement> parents = new ArrayList<>();
        for (FBXElement connection : connections) {
            long checkId = connection.getProperties()[1].getData();
            if (checkId == id) {
                long parentId = connection.getProperties()[2].getData();
                if(parentId == 0) continue;
                parents.add(getElement("Objects").getElement(parentId));
            }
        }
        return parents.toArray(new FBXElement[0]);
    }

    public FBXElement getChild(long id) {
        return getChildren(id)[0];
    }

    public FBXElement[] getChildren(long id) {
        FBXElement[] connections = getElement("Connections").getElements();

        List<FBXElement> children = new ArrayList<>();
        for (FBXElement connection : connections) {
            long checkId = connection.getProperties()[2].getData();
            if (checkId == id) {
                long childId = connection.getProperties()[1].getData();
                children.add(getElement("Objects").getElement(childId));
            }
        }
        return children.toArray(new FBXElement[0]);
    }

    public boolean hasElement(String name) {
        for (FBXElement element : elements) {
            if (element.getName().equals(name)) return true;
        }
        return false;
    }

    public String getFileName() {
        return fileName;
    }
}
