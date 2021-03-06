package ru.hollowhorizon.hc.api.registy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HollowRegister {
    boolean auto_model() default false;

    String texture() default "";

    String model() default "null";
}
