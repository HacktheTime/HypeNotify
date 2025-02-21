package de.hype.hypenotify.layouts.autodetection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface Layout {
    String name();
}