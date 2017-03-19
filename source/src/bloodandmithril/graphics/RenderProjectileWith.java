package bloodandmithril.graphics;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.equipment.weapon.ranged.projectile.DummyProjectileRenderer;

/**
 * Annotates a class to specify which class will render the annotated class
 * 
 * @author Matt
 */
@Retention(RUNTIME)
@Target(TYPE)
@Copyright("Matthew Peck 2017")
public @interface RenderProjectileWith {
	Class<? extends ProjectileRenderer> value() default DummyProjectileRenderer.class;
}