package de.hype.hypenotify;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigField {
    String description() default "";

    boolean allowNull() default false;
}