package com.deepblue.h2echo.annotations;

import com.deepblue.h2echo.dialects.ScriptSyntax;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface EnableH2Echo {
    ScriptSyntax syntax() default ScriptSyntax.MARIA_DB;
    String scriptPath() default "SQL/migrations";
}
