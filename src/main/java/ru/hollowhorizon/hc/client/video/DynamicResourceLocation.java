package ru.hollowhorizon.hc.client.video;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public class DynamicResourceLocation extends ResourceLocation {

    private final String playerNamespace;
    private final String playerPath;

    /**
     * This will construct a ResourceLocation that takes care about the name pre- and postfix minecraft adds to dynamic textures.
     *
     * @param namespace Should be your modid
     * @param path      Should be a unique identifier for this player
     * @since 0.2.0.0
     */
    public DynamicResourceLocation(@ParametersAreNonnullByDefault String namespace, @ParametersAreNonnullByDefault String path) {
        super("minecraft:dynamic/" + namespace + "." + path + "_1");
        playerNamespace = namespace;
        playerPath = path;
    }

    /**
     * See {@link #toWorkingString()} to get the internal name
     *
     * @return The true ResourceLocation, containing pre- and postfix.
     * @since 0.2.0.0
     */
    @Override
    public @Nonnull
    String toString() {
        return super.toString();
    }

    /**
     * See {@link #toString()} to get the name containing pre- and postfix
     *
     * @return The FancyVideo-API ResourceLocation, <b>NOT</b> containing pre- and postfix.
     * @since 0.2.0.0
     */
    public @Nonnull String toWorkingString() {
        return playerNamespace + ":" + playerPath;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
