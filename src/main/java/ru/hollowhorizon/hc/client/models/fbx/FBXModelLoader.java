package ru.hollowhorizon.hc.client.models.fbx;

import net.minecraft.util.ResourceLocation;
import ru.hollowhorizon.hc.client.models.core.bonemf.BoneMFModel;
import ru.hollowhorizon.hc.client.models.core.bonemf.BoneMFNode;
import ru.hollowhorizon.hc.client.models.fbx.raw.FBXElement;
import ru.hollowhorizon.hc.client.models.fbx.raw.FBXFile;
import ru.hollowhorizon.hc.client.models.fbx.raw.FBXProperty;
import ru.hollowhorizon.hc.client.models.fbx.raw.HollowByteStream;
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class FBXModelLoader {

    public static void main(String[] args) {
        FBXFile file = loadFile(new ResourceLocation(MODID, "models/hollowrigv3.fbx"));

        loadRoot(file, "ROOT");
        //convertToBoneMF(file);
    }

    public static BoneMFModel convertToBoneMF(FBXFile file) {
        BoneMFNode root = loadRoot(file, file.getFileName());

        return null;
    }

    private static BoneMFNode loadRoot(FBXFile file, String name) {
        FBXElement[] elements = file.getChildren(0);


        return null;
    }


    public static FBXFile loadFile(ResourceLocation location) {
        FBXElement[] elements = loadRaw(location);

        return new FBXFile(location.getPath(), elements);
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
            if(i==0) firstElement = type;
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
        if(firstElement=='L') id = properties[0].getData();
        return new FBXElement(nodeName, id, properties, elements.toArray(new FBXElement[0]));
    }


}
