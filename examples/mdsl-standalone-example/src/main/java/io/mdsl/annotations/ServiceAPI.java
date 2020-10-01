package io.mdsl.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.MODULE, ElementType.PACKAGE})
public @interface ServiceAPI {
	public String visibility() default "";
	public String direction() default "";
	public String version() default "";
}
