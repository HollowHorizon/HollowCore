package ru.hollowhorizon.hc.common.objects.tiles;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public abstract class HollowTileEntity extends TileEntity {
    public HollowTileEntity(TileEntityType<?> tileEntityType) {
        super(tileEntityType);
    }

    //при загрузке тайла отправляет на клиент
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT nbt = new CompoundNBT();
        this.save(nbt);

        return new SUpdateTileEntityPacket(this.worldPosition, 42, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        if (level != null) {
            BlockState blockState = level.getBlockState(worldPosition);
            this.load(blockState, pkt.getTag());
        }
    }

    //создаёт тег
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbt = new CompoundNBT();
        this.save(nbt);
        return nbt;
    }

    //считывает информацию из nbt в этот класс
    @Override
    public void handleUpdateTag(BlockState blockState, CompoundNBT lastTag) {
        this.load(blockState, lastTag);
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        saveNBT(nbt);
        return super.save(nbt);
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        loadNBT(nbt);
        super.load(state, nbt);
    }

    public abstract void saveNBT(CompoundNBT nbt);
    public abstract void loadNBT(CompoundNBT nbt);
}
