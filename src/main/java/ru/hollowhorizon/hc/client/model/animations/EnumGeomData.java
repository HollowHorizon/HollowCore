package ru.hollowhorizon.hc.client.model.animations;

public enum EnumGeomData {
    xloc,
    yloc,
    zloc,
    xrot,
    yrot,
    zrot;

    private EnumGeomData() {
    }

    public boolean isRotation() {
        return this == xrot || this == yrot || this == zrot;
    }
}
