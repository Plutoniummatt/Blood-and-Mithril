package bloodandmithril.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Simple annotation to attach a name to a class
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
@Retention(RetentionPolicy.RUNTIME)
public @interface Name {
	public String name();
}