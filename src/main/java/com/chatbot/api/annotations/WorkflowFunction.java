package com.chatbot.api.annotations;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WorkflowFunction {

    String name();

    String description() default "";

    String category() default "General";

    boolean async() default false;
}
