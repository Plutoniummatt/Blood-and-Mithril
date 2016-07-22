package bloodandmithril.character.ai;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import bloodandmithril.core.Copyright;

/**
 * Annotations a class to specify which class will execute the annotated class
 *
 * @author Matt
 */
@Retention(RUNTIME)
@Target(TYPE)
@Copyright("Matthew Peck 2016")
public @interface ExecutedBy {
	Class<? extends AITaskExecutor> value() default DummyAITaskExecutor.class;
}
