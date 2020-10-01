package io.mdsl.annotations;

import java.lang.annotation.*;

// not sure whether this is needed/convenient (many, so make "in" the default?) 
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE})
public @interface ServiceParameter {
	public String mapDecorator() default "";
}
