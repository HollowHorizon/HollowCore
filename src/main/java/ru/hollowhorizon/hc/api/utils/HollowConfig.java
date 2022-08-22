package ru.hollowhorizon.hc.api.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HollowConfig {
    String value();
    String description() default "";

    float min() default 0.0F;
    float max() default 1.0F;

}
