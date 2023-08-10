package ru.hollowhorizon.hc.mixin;

import com.google.common.collect.BiMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import ru.hollowhorizon.hc.api.registy.IReloadableForgeRegistry;
import ru.hollowhorizon.hc.common.registry.ReloadableRegistryManager;

import java.util.BitSet;
import java.util.Set;
import java.util.function.Supplier;

@Mixin(value = ForgeRegistry.class, remap = false)
public abstract class ForgeRegistryMixin<V> implements IForgeRegistry<V>, IReloadableForgeRegistry<V> {

    @Unique
    @Final
    private final Set<ResourceLocation> dummies = new ObjectOpenHashSet<>();
    @Shadow
    @Final
    private BiMap<ResourceLocation, V> names;
    @Shadow
    @Final
    private BiMap<Integer, V> ids;
    @Shadow
    @Final
    private BiMap<Object, V> owners;
    @Shadow
    @Final
    private DummyFactory<V> dummyFactory;
    @Shadow
    @Final
    private BitSet availabilityMap;
    @Unique
    private Set<Triple<V, Integer, ResourceLocation>> backups;
    @Unique
    private Set<V> scripted;
    @Unique
    private Supplier<V> dummySupplier;

    @Shadow
    public abstract void unfreeze();

    @Shadow
    public abstract V getValue(int id);

    @Shadow
    abstract int add(int id, ResourceLocation key, V value);

    @Shadow
    public abstract void freeze();

    @Shadow public abstract ResourceLocation getRegistryName();

    @Override
    public V registerEntry(ResourceLocation location, V registryEntry) {
        if (registryEntry != null) hollowcore$removeEntry(location);
        V newEntry = getValue(add(-1, null, registryEntry));
        if (newEntry == registryEntry) {
            if (this.scripted == null) {
                this.scripted = new ObjectOpenHashSet<>();
            }
            this.scripted.add(registryEntry);
        }
        return newEntry;
    }

    @Override
    public void removeEntry(ResourceLocation name) {
        V entry = this.names.remove(name);
        if (entry != null) {
            if (this.backups == null) {
                this.backups = new ObjectOpenHashSet<>();
            }
            Integer id = this.ids.inverse().remove(entry);
            Object ownerOverride = this.owners.inverse().remove(entry);
            this.backups.add(Triple.of(entry, id, name));
            hollowcore$putDummy(entry, name, id, ownerOverride);
        }
    }

    @Override
    public void onReload() {
        unfreeze();
        if (this.scripted != null) {
            for (V entry : this.scripted) {
                ResourceLocation rl = this.names.inverse().remove(entry);
                Integer id = this.ids.inverse().remove(entry);
                Object owner = this.owners.inverse().remove(entry);
                hollowcore$putDummy(entry, rl, id, owner);
            }
            this.scripted = null;
        }
        if (this.backups != null) {
            for (Triple<V, Integer, ResourceLocation> entry : this.backups) {
                this.names.put(entry.getRight(), entry.getLeft());
                this.ids.put(entry.getMiddle(), entry.getLeft());
                this.owners.put(entry.getRight(), entry.getLeft());
                this.dummies.remove(entry.getRight());
            }
            this.backups = null;
        }
    }

    public void hollowcore$putDummy(V entry, ResourceLocation rl, Integer id, Object owner) {
        if (entry == null || rl == null || id == null) return;
        V dummy = hollowcore$getDummy(rl);
        if (dummy != null) {
            this.names.put(rl, dummy);
            this.ids.put(id, dummy);
            if (owner != null) {
                this.owners.put(owner, dummy);
            }
            this.dummies.add(rl);
        }
    }

    @Unique
    public void hollowcore$removeEntry(ResourceLocation rl) {
        V dummy = this.names.remove(rl);
        if (dummy != null) {
            int id = this.ids.inverse().remove(dummy);
            this.owners.inverse().remove(dummy);
            this.availabilityMap.clear(id);
        }
        this.dummies.remove(rl);
    }

    @Nullable
    public V hollowcore$getDummy(ResourceLocation rl) {
        if (dummyFactory != null) {
            return dummyFactory.createDummy(rl);
        }
        if (dummySupplier == null) {
            dummySupplier = ReloadableRegistryManager.getDummySupplier(getRegistryName());
        }
        return dummySupplier.get();
    }
}
