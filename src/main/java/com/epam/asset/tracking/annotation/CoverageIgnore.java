package com.epam.asset.tracking.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ ElementType.METHOD, ElementType.TYPE, ElementType.PACKAGE })
public @interface CoverageIgnore {
}