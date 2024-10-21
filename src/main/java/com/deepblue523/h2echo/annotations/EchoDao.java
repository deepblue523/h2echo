package com.deepblue523.h2echo.annotations;

import com.deepblue523.h2echo.dialects.ScriptSyntax;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface EchoDao {
    ScriptSyntax syntax() default ScriptSyntax.MARIA_DB;
    String scriptPath() default "";
}
