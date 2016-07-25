package bloodandmithril.graphics;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import bloodandmithril.core.Copyright;
import bloodandmithril.prop.DummyPropRenderingService;
import bloodandmithril.prop.renderservice.PropRenderingService;

/**
 * Annotations a class to specify which class will render the annotated class
 * 
 * @author Matt
 */
@Retention(RUNTIME)
@Target(TYPE)
@Copyright("Matthew Peck 2016")
public @interface RenderPropWith {
	Class<? extends PropRenderingService> value() default DummyPropRenderingService.class;
}