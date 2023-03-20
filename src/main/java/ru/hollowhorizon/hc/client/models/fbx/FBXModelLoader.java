package ru.hollowhorizon.hc.client.models.fbx;

import net.minecraft.util.ResourceLocation;
import ru.hollowhorizon.hc.client.models.core.bonemf.BoneMFAttribute;
import ru.hollowhorizon.hc.client.models.core.bonemf.BoneMFMeshAttribute;
import ru.hollowhorizon.hc.client.models.core.bonemf.BoneMFModel;
import ru.hollowhorizon.hc.client.models.core.bonemf.BoneMFNode;
import ru.hollowhorizon.hc.client.models.fbx.raw.FBXElement;
import ru.hollowhorizon.hc.client.models.fbx.raw.FBXFile;
import ru.hollowhorizon.hc.client.models.fbx.raw.FBXProperty;
import ru.hollowhorizon.hc.client.models.fbx.raw.HollowByteStream;
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils;
import ru.hollowhorizon.hc.client.utils.math.Vector4d;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class FBXModelLoader {

    public static void main(String[] args) {
        FBXFile file = loadFile(new ResourceLocation(MODID, "models/hollowrigv3.fbx"));

        BoneMFModel model = convertToBoneMF(file);


    }

    public static BoneMFModel convertToBoneMF(FBXFile file) {
        BoneMFNode root = loadRootNode(file, file.getFileName());

        return new BoneMFModel(new ResourceLocation(file.getFileName()), root);
    }

    private static BoneMFNode loadRootNode(FBXFile file, String name) {
        FBXElement[] elements = file.getChildren(0);
        BoneMFNode root = new BoneMFNode(name);

        if (elements.length > 0) {
            for (FBXElement node : elements) {
                if(!node.getType().equals("Mesh") || !node.getType().equals("Null")) continue;

                BoneMFNode mfNode = parseNode(node);
                root.addChild(mfNode);
                mfNode.setParent(root);
            }
            BoneMFNode firstChild = root.getChildren().get(0);
            root.setInheritType(firstChild.getInheritType());
        }

        return root;
    }

    private static BoneMFNode parseNode(FBXElement node) {
        String nodeName = node.getName();

        FBXElement[] children = node.getElements();
        BoneMFNode mfNode = new BoneMFNode(nodeName);

        mfNode.setInheritType(BoneMFNode.InheritTypes.fromFBX(node.getProperties()[4].getData()));
        mfNode.setPostRotation(new Vector4d(0, 0, 0, 1));
        mfNode.setPreRotation(new Vector4d(0, 0, 0, 1));
        mfNode.setRotation(new Vector4d(0, 0, 0, 1));
        mfNode.setRotationOffset(new Vector4d(0, 0, 0, 1));
        mfNode.setRotationPivot(new Vector4d(0, 0, 0, 1));
        mfNode.setScaling(new Vector4d(0, 0, 0, 1));
        mfNode.setScalingOffset(new Vector4d(0, 0, 0, 1));
        mfNode.setScalingPivot(new Vector4d(0, 0, 0, 1));
        mfNode.setTranslation(new Vector4d(0, 0, 0, 1));

        if (children.length > 0){
            for (FBXElement attr : children){
                if(node.getType().equals("Mesh") || node.getType().equals("Null")) mfNode.addChild(parseNode(attr));

                BoneMFAttribute boneAttr = parseAttribute(attr, mfNode);
                mfNode.addAttribute(boneAttr);
            }
        }
        return mfNode;

    }

    private static BoneMFAttribute parseAttribute(FBXElement element, BoneMFNode owner) {
        var name = element.getName();

        BoneMFAttribute attr;
        switch (name) {
            case "Geometry" -> {
                BoneMFMeshAttribute meshAttr = new BoneMFMeshAttribute(owner);
                double[] triangles = element.getProperties()[2].getData();
                attr = meshAttr;
            }
            default -> attr = new BoneMFAttribute(BoneMFAttribute.AttributeTypes.NULL, owner);
        }
        return attr;

    }


    public static FBXFile loadFile(ResourceLocation location) {
        return new FBXFile(location.toString(), loadRaw(location));
    }

    private static long[] getElementsByValue(FBXElement[] elements, long id) {
        List<Long> elementIds = new ArrayList<>();
        for (FBXElement element : elements) {
            if (element.getName().equals("Connections")) {
                for (FBXElement connection : element.getElements()) {
                    if (connection.getName().equals("C")) {
                        long checkId = connection.getProperties()[2].getData();
                        if (checkId == id) elementIds.add(connection.getProperties()[1].getData());
                    }
                }
            }
        }
        long[] res = new long[elementIds.size()];
        for (int i = 0; i < elementIds.size(); i++) {
            res[i] = elementIds.get(i);
        }
        return res;
    }

    public static FBXElement[] loadRaw(ResourceLocation location) {


        try {
            InputStream stream = HollowJavaUtils.getResource(location);
            HollowByteStream reader = new HollowByteStream(stream);

            reader.read(23);
            int version = reader.readUInt();

            List<FBXElement> elements = new ArrayList<>();
            while (true) {
                FBXElement element = readElement(reader);
                if (element == null) {
                    break;
                } else {
                    elements.add(element);
                }
            }
            return elements.toArray(new FBXElement[0]);
        } catch (IOException e) {
            e.printStackTrace();
            return new FBXElement[0];
        }
    }

    public static FBXElement readElement(HollowByteStream reader) throws IOException {
        int offset = reader.readUInt();
        if (offset == 0) return null;
        int propertiesCount = reader.readUInt();
        reader.readUInt();

        String nodeName = reader.readString();

        FBXProperty<?>[] properties = new FBXProperty[propertiesCount];
        char firstElement = '@';
        for (int i = 0; i < propertiesCount; i++) {
            char type = reader.readChar();
            if (i == 0) firstElement = type;
            properties[i] = FBXProperty.load(reader, type);
        }

        List<FBXElement> elements = new ArrayList<>();
        if (reader.available() < offset) {
            while (reader.available() < (offset - 13)) {
                elements.add(readElement(reader));
            }
            reader.read(13);
        }
        long id = -1;
        if (firstElement == 'L') id = properties[0].getData();
        return new FBXElement(nodeName, id, properties, elements.toArray(new FBXElement[0]));
    }


}
