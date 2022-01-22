package ru.hollowhorizon.hc.common.registry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.api.registy.HollowRegister;
import ru.hollowhorizon.hc.common.world.structures.config.StructureNameConfig;
import ru.hollowhorizon.hc.common.world.structures.objects.HollowBastion;
import ru.hollowhorizon.hc.mixin.FlatStructuresAccessor;

import java.util.*;
import java.util.function.Supplier;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class ModStructures {
    public static final ArrayList<HollowStructureData> STRUCTURE_DATA = new ArrayList<>();

    @HollowRegister
    public static final HollowBastion bastion = create("bastion", new HollowBastion(
            GenerationStage.Decoration.UNDERGROUND_STRUCTURES, "bastion"
    ), 8, 32);

    private static <T extends net.minecraft.world.gen.feature.structure.Structure<?>> T create(String id, T structure, int minSeparation, int averageSpacing) {
        return create(id, structure, minSeparation, averageSpacing, new Random(id.hashCode()).nextInt(Integer.MAX_VALUE), true, false);
    }

    private static <T extends net.minecraft.world.gen.feature.structure.Structure<?>> T create(String id, T structure, int minSeparation, int averageSpacing, int salt) {
        return create(id, structure, minSeparation, averageSpacing, salt, false, false);
    }

    private static <T extends net.minecraft.world.gen.feature.structure.Structure<?>> T create(String id, T structure, int minSeparation, int averageSpacing, int salt, boolean blendLandToStructure, boolean forVanillaBiomes) {

        STRUCTURE_DATA.add(new HollowStructureData(structure, new StructureSeparationSettings(averageSpacing, minSeparation, salt), blendLandToStructure, forVanillaBiomes));

        return structure;
    }

    public static void postInit() {
        prePopulateStructureSpacings();
        HollowConfiguration.postInit();
    }

    private static void prePopulateStructureSpacings() {
        for (HollowStructureData structureData : STRUCTURE_DATA) {
            Structure<?> structure = structureData.structure;

            Structure.STRUCTURES_REGISTRY.put(structure.getRegistryName().toString(), structure);

            if (structureData.blendsLand)
                Structure.NOISE_AFFECTING_FEATURES = ImmutableList.<Structure<?>>builder().addAll(Structure.NOISE_AFFECTING_FEATURES).add(structure).build();

            DimensionStructuresSettings.DEFAULTS = ImmutableMap.<Structure<?>, StructureSeparationSettings>builder().putAll(DimensionStructuresSettings.DEFAULTS).put(structure, structureData.spreadSettings).build();

            for (Map.Entry<RegistryKey<DimensionSettings>, DimensionSettings> settingsEntry : WorldGenRegistries.NOISE_GENERATOR_SETTINGS.entrySet()) {
                Map<Structure<?>, StructureSeparationSettings> separationSettings = settingsEntry.getValue().structureSettings().structureConfig();

                if (separationSettings instanceof ImmutableMap) {
                    Map<Structure<?>, StructureSeparationSettings> newMap = new HashMap<>(separationSettings);

                    newMap.put(structure, structureData.spreadSettings);
                    settingsEntry.getValue().structureSettings().structureConfig = newMap;
                } else {
                    separationSettings.put(structure, structureData.spreadSettings);
                }
            }
        }
    }

    public static void onBiomeLoad(final BiomeLoadingEvent ev) {
        if (ev.getName() == null)
            return;

        if (ev.getCategory()== Biome.Category.NETHER) {
            ev.getGeneration().getStructures().add(() -> HollowConfiguration.BLOCKPOS_STRUCTURE);
        }
    }

    public static class HollowStructureData {
        public final Structure<?> structure;
        public final StructureSeparationSettings spreadSettings;
        private final boolean blendsLand;
        private final boolean isVanillaBiomeStructure;

        private <T extends net.minecraft.world.gen.feature.structure.Structure<?>> HollowStructureData(T structure, StructureSeparationSettings settings, boolean blendsLand, boolean forVanillaBiomes) {
            this.structure = structure;
            this.spreadSettings = settings;
            this.blendsLand = blendsLand;
            this.isVanillaBiomeStructure = forVanillaBiomes;
        }
    }

    public static class HollowConfiguration {
        private static final StructureFeature<?, ? extends Structure<?>> BLOCKPOS_STRUCTURE = register("bastion", ModStructures.bastion.configured(new StructureNameConfig("hollow_bastion")));

        public static void postInit() {
            HollowCore.LOGGER.info("post init");
            FlatStructuresAccessor.getStructureFeatures().put(ModStructures.bastion, BLOCKPOS_STRUCTURE);
        }

        private static <C extends IFeatureConfig, S extends Structure<C>, SF extends StructureFeature<C, ? extends S>> SF register(String id, SF configuredStructure) {
            return WorldGenRegistries.register(WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE, new ResourceLocation(MODID, id), configuredStructure);
        }
    }
}
