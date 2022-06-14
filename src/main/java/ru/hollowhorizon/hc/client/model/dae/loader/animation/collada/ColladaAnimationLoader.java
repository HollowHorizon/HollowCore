package ru.hollowhorizon.hc.client.model.dae.loader.animation.collada;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ru.hollowhorizon.hc.client.model.dae.loader.data.AnimationData;
import ru.hollowhorizon.hc.client.utils.tools.XmlNode;
import ru.hollowhorizon.hc.client.utils.tools.XmlParser;

import static ru.hollowhorizon.hc.HollowCore.MODID;

@OnlyIn(Dist.CLIENT)
public class ColladaAnimationLoader implements IAnimationLoader {

    public boolean canLoadAnimation(final ResourceLocation animationLocation)
    {
        return animationLocation.getPath().endsWith(".dae");
    }

    public static void main(String[] args) {
        try {
            new ColladaAnimationLoader().loadAnimation(new ResourceLocation(MODID, "models/cyborg.dae"));
        } catch (AnimationLoadingException e) {
            e.printStackTrace();
        }
    }

    public AnimationData loadAnimation(final ResourceLocation animationLocation) throws AnimationLoadingException {
        System.out.println("loading animations");
        try {
            final XmlNode node = XmlParser.loadXmlFile(animationLocation);
            final XmlNode animNode = node.getChild("library_animations");
            final XmlNode jointsNode = node.getChild("library_visual_scenes");
            final ColladaAnimationExtractor loader = new ColladaAnimationExtractor(animNode, jointsNode);
            return loader.extractAnimation();
        }
        catch (final Exception e) {
            throw new AnimationLoadingException(this.getClass(), animationLocation, e);
        }
    }

}
