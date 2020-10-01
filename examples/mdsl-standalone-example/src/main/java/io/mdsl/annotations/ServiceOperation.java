package io.mdsl.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ServiceOperation {
	public String responsibility() default "";
	public ParameterClassifier parameters() default ParameterClassifier.all;
}
