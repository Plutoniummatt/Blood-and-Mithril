package bloodandmithril.core;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Copyright
 *
 * @author Matt
 */
@Retention(RUNTIME)
@Target(TYPE)
@Copyright("Matthew Peck 2014")
public @interface Copyright {
	String value();
}