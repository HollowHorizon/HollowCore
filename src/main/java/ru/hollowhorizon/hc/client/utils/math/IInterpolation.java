package ru.hollowhorizon.hc.client.utils.math;

import net.minecraft.client.resources.I18n;

public interface IInterpolation {
    float interpolate(float a, float b, float x);

    double interpolate(double a, double b, double x);

    default String getName() {
        return I18n.get(this.getKey());
    }

    String getKey();

    default String getTooltip() {
        return I18n.get(this.getTooltipKey());
    }

    String getTooltipKey();
}