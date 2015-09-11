package bloodandmithril.graphics;

import java.util.Map;

import bloodandmithril.character.individuals.characters.Elf;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.renderers.ElfRenderer;

import com.google.common.collect.Maps;
import com.google.inject.Singleton;

/**
 * Renders entities
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface Renderer<T> {

	/**
	 * Renders an instance of T
	 */
	@SuppressWarnings("unchecked")
	public default void render(T t) {
		((Renderer<T>) Wiring.injector.getInstance(Renderers.class).map.get(t.getClass())).internalRender(t);
	}

	public void internalRender(T t);


	@Singleton
	public static class Renderers {
		public Map<Class<?>, Renderer<?>> map = Maps.newHashMap();

		Renderers() {
			map.put(Elf.class, new ElfRenderer());
		}
	}
}
