package bloodandmithril.core;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import bloodandmithril.prop.Prop;
import bloodandmithril.prop.updateservice.PropUpdateService;

/**
 * Annotates a {@link Prop} class to specify which {@link PropUpdateService} implementation to use for updates
 * 
 * @author Matt
 */
@Retention(RUNTIME)
@Target(TYPE)
@Copyright("Matthew Peck")
public @interface UpdatedBy {
	public Class<? extends PropUpdateService> value();
}