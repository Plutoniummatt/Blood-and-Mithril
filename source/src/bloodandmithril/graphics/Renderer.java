package bloodandmithril.graphics;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.inject.Singleton;

import bloodandmithril.character.individuals.characters.Elf;
import bloodandmithril.character.individuals.characters.Hare;
import bloodandmithril.character.individuals.characters.Wolf;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.renderers.ElfRenderer;
import bloodandmithril.graphics.renderers.IndividualRenderer;
import bloodandmithril.graphics.renderers.WolfRenderer;

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
	public static <T> void render(T t, Graphics graphics) {
		((Renderer<T>) Wiring.injector().getInstance(Renderers.class).map.get(t.getClass())).internalRender(t, graphics);
	}

	public void internalRender(T t, Graphics graphics);


	@Singleton
	public static class Renderers {
		public Map<Class<?>, Renderer<?>> map = Maps.newHashMap();

		Renderers() {
			map.put(Elf.class, new ElfRenderer());
			map.put(Wolf.class, new WolfRenderer());
			map.put(Hare.class, new IndividualRenderer<Hare>());
		}
	}
}
