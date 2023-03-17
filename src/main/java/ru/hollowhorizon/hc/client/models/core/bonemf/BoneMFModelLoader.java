package ru.hollowhorizon.hc.client.models.core.bonemf;

import com.upokecenter.cbor.CBORObject;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.lwjgl.system.MemoryUtil;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.models.core.utils.ByteBufferBackedInputStream;
import ru.hollowhorizon.hc.client.utils.math.Matrix4d;
import ru.hollowhorizon.hc.client.utils.math.Vector4d;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;


public class BoneMFModelLoader {

    public static InputStream asInputStream(ByteBuffer buffer) {
        if (buffer.hasArray()) {
            // use heap buffer; no array is created; only the reference is used
            return new ByteArrayInputStream(buffer.array());
        }
        return new ByteBufferBackedInputStream(buffer);
    }

    private static BoneMFVertex parseVertex(CBORObject cbor){
        double nX = cbor.get("nX").AsDouble();
        double nY = cbor.get("nY").AsDouble();
        double nZ = cbor.get("nZ").AsDouble();
        double x = cbor.get("x").AsDouble();
        double y = cbor.get("y").AsDouble();
        double z = cbor.get("z").AsDouble();
        double u = cbor.get("u").AsDouble();
        double v = 1.0 - cbor.get("v").AsDouble();
        BoneMFVertex vertex = new BoneMFVertex(x, y, z, nX, nY, nZ, u, v);
        CBORObject boneWeights = cbor.get("boneWeights");
        if (!boneWeights.isNull()){
            for (CBORObject boneWeight : boneWeights.getValues()){
                String boneName = boneWeight.get("boneName").AsString();
                double weight = boneWeight.get("weight").AsDouble();
                vertex.addBoneWeight(boneName, weight);
            }
        }
        return vertex;
    }

    private static BoneMFAttribute parseAttribute(CBORObject cbor, BoneMFNode owner){
        String attrType = cbor.get("type").AsString();
        BoneMFAttribute.AttributeTypes type = BoneMFAttribute.getAttributeTypeFromString(attrType);
        BoneMFAttribute attr;
        switch (type){
            case MESH:
            {
                CBORObject mesh = cbor.get("mesh");
                CBORObject triangles = mesh.get("triangles");
                BoneMFMeshAttribute meshAttr = new BoneMFMeshAttribute(owner);
                for (int i = 0; i < triangles.size() / 3; i++){
                    int realIndex = i * 3;
                    meshAttr.addTriangle(triangles.get(realIndex).AsInt32(),
                            triangles.get(realIndex + 1).AsInt32(),
                            triangles.get(realIndex + 2).AsInt32());
                }
                for (CBORObject vert : mesh.get("vertices").getValues()){
                    meshAttr.addVertex(parseVertex(vert));
                }

                attr = meshAttr;
                break;
            }
            case SKELETON:
            case NULL: {
                attr = new BoneMFAttribute(type, owner);
                break;
            }
            default:
                attr = new BoneMFAttribute(BoneMFAttribute.AttributeTypes.NULL, owner);
                //HollowCore.LOGGER.error("Failed to parse attribute: %s", cbor.toString());
        }
        return attr;
    }

    private static Matrix4d parseMatrix(CBORObject cborMat){
        Vector4d[] rows = new Vector4d[4];
        int i = 0;
        for (CBORObject row : cborMat.getValues()){
            rows[i] = parseVector(row);
            i++;
        }
        Matrix4d mat = new Matrix4d();
        mat.setM00(rows[0].x());
        mat.setM10(rows[0].y());
        mat.setM20(rows[0].z());
        mat.setM30(rows[3].x());
        mat.setM01(rows[1].x());
        mat.setM11(rows[1].y());
        mat.setM21(rows[1].z());
        mat.setM31(rows[3].y());
        mat.setM02(rows[2].x());
        mat.setM12(rows[2].y());
        mat.setM22(rows[2].z());
        mat.setM32(rows[3].z());
        mat.setM03(rows[0].w());
        mat.setM13(rows[1].w());
        mat.setM23(rows[2].w());
        mat.setM33(rows[3].w());
        return mat;
    }

    private static Vector4d parseVector(CBORObject vec){
        return new Vector4d(vec.get(0).AsDouble(), vec.get(1).AsDouble(),
                vec.get(2).AsDouble(), vec.get(3).AsDouble());
    }


    private static BoneMFNodeFrame parseNodeFrame(CBORObject channelFrame){
        BoneMFNodeFrame mfFrame = new BoneMFNodeFrame();
        mfFrame.setRotation(parseVector(channelFrame.get("rotation")));
        mfFrame.setTranslation(parseVector(channelFrame.get("translation")));
        mfFrame.setScale(parseVector(channelFrame.get("scale")));
        return mfFrame;
    }

    private static BoneMFAnimationChannel parseAnimationChannel(String nodeName, CBORObject channelCbor){
        BoneMFAnimationChannel channel = new BoneMFAnimationChannel(nodeName);
        for (CBORObject channelFrame : channelCbor.getValues()){
            channel.addNodeFrame(parseNodeFrame(channelFrame));
        }
        return channel;
    }

    private static BoneMFAnimation parseAnimation(String animationName, CBORObject animationCbor){
        BoneMFAnimation animation = new BoneMFAnimation(animationName);
        CBORObject channels = animationCbor.get("channels");
        long frameCount = animationCbor.get("frameCount").AsInt64Value();
        double frameRate = animationCbor.get("frameRate").AsDouble();
        animation.setFrameCount(frameCount);
        animation.setFrameRate(frameRate);
        if (!channels.isNull()){
            for (CBORObject key : channels.getKeys()){
                String channelName = key.AsString();
                CBORObject channelCbor = channels.get(channelName);
                animation.addChannel(channelName, parseAnimationChannel(channelName, channelCbor));
            }
        }
        return animation;
    }

    private static BoneMFNode parseNode(CBORObject node){
        String nodeName = node.get("name").AsString();
        //HollowCore.LOGGER.info("CBOR Found node: {}", nodeName);
        CBORObject children = node.get("children");
        BoneMFNode mfNode = new BoneMFNode(nodeName);
        String inheritType = node.get("inheritType").AsString();
        mfNode.setInheritType(BoneMFNode.getInheritTypeFromString(inheritType));
        mfNode.setPostRotation(parseVector(node.get("postRotation")));
        mfNode.setPreRotation(parseVector(node.get("preRotation")));
        mfNode.setRotation(parseVector(node.get("rotation")));
        mfNode.setRotationOffset(parseVector(node.get("rotationOffset")));
        mfNode.setRotationPivot(parseVector(node.get("rotationPivot")));
        mfNode.setScaling(parseVector(node.get("scaling")));
        mfNode.setScalingOffset(parseVector(node.get("scalingOffset")));
        mfNode.setScalingPivot(parseVector(node.get("scalingPivot")));
        mfNode.setTranslation(parseVector(node.get("translation")));

        CBORObject attributes = node.get("attributes");
        if (!attributes.isNull()){
            for (CBORObject attr : attributes.getValues()){
                BoneMFAttribute boneAttr = parseAttribute(attr, mfNode);
                mfNode.addAttribute(boneAttr);
            }
        }

        if (!children.isNull()){
            for (CBORObject child : children.getValues()){
                BoneMFNode childNode = parseNode(child);
                childNode.setParent(mfNode);
                mfNode.addChild(childNode);
            }
        }
        return mfNode;
    }

    public static void loadAnimations(BoneMFModel model, CBORObject animationsCbor, ResourceLocation name){
        model.getSkeleton().ifPresent((BoneMFSkeleton skeleton) -> {
            int count = animationsCbor.size();
            int i = 0;
            for (CBORObject key : animationsCbor.getKeys()){
                String animationName = key.AsString();
                CBORObject animationCbor = animationsCbor.get(animationName);
                ResourceLocation animName;
                if (count > 1 ){
                    animName = new ResourceLocation(name.getNamespace(), name.getPath() + "_" + count);
                } else {
                    animName = name;
                }
                skeleton.addAnimation(animName, parseAnimation(animationName, animationCbor));
            }
        });
    }

    public static void loadAdditionalAnimations(BoneMFModel model, ByteBuffer resource, ResourceLocation name){
        InputStream iStream = BoneMFModelLoader.asInputStream(resource);
        CBORObject cbor = CBORObject.Read(iStream);
        if (cbor.ContainsKey("animations")){
            CBORObject animations = cbor.get("animations");
            if (!animations.isNull()){
                loadAnimations(model, animations, name);
            }
        }
    }

    public static BoneMFArmorModel loadArmor(ByteBuffer resource, ResourceLocation name,
                                             List<String> headMeshes, List<String> bodyMeshes,
                                             List<String> legMeshes, List<String> feetMeshes) throws Exception {
        InputStream stream = asInputStream(resource);
        CBORObject cbor = CBORObject.Read(stream);
        BoneMFNode root = loadRootNode(cbor, name);
        return new BoneMFArmorModel(name, root, headMeshes, bodyMeshes, legMeshes, feetMeshes);
    }

    private static BoneMFNode loadRootNode(CBORObject cbor, ResourceLocation name) throws Exception {
        CBORObject nodes = cbor.get("nodes");
        BoneMFNode root = new BoneMFNode(name.toString());
        if (!nodes.isNull()){
            for (CBORObject node : nodes.getValues()){
                BoneMFNode mfNode = parseNode(node);
                root.addChild(mfNode);
                mfNode.setParent(root);
            }
            BoneMFNode firstChild = root.getChildren().get(0);
            root.setInheritType(firstChild.getInheritType());
        }
        return root;
    }

    public static BoneMFModel load(ByteBuffer resource, ResourceLocation name)
            throws Exception {
        InputStream stream = asInputStream(resource);
        CBORObject cbor = CBORObject.Read(stream);
        BoneMFNode root = loadRootNode(cbor, name);
        BoneMFModel model = new BoneMFModel(name, root);
        if (cbor.ContainsKey("animations")){
            CBORObject animations = cbor.get("animations");
            if (!animations.isNull()){
                loadAnimations(model, animations, name);
            }
        }
        return model;
    }

    public static void main(String[] args) throws Exception {
        InputStream stream = BoneMFModelLoader.class.getResourceAsStream("/assets/hc/models/biped.bonemf");
        byte[] _data = IOUtils.toByteArray(stream);
        ByteBuffer data = MemoryUtil.memCalloc(_data.length + 1);
        data.put(_data);
        data.put((byte) 0);
        data.flip();
        stream.close();
        BoneMFModel m = BoneMFModelLoader.load(data, new ResourceLocation("hc:hc"));

        System.out.println(m);
    }

}
