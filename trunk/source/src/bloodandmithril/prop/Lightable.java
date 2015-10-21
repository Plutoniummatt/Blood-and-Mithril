package bloodandmithril.prop;

import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine.EntityVisible;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.util.datastructure.WrapperForTwo;

/**
 * Something that can be lit
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
@Name(name = "Lightable")
public interface Lightable extends Visible {

	/**
	 * Lights this thing
	 */
	public void light();

	/**
	 * Extinguishes this thing
	 */
	public void extinguish();

	/**
	 * True if is lit
	 */
	public boolean isLit();

	/**
	 * True if is lit
	 */
	public boolean canLight();


	public static class LightableUnlit extends EntityVisible {
		private static final long serialVersionUID = -211448711527852658L;
		private WrapperForTwo<Class<? extends Lightable>, Lightable> wrapper = WrapperForTwo.wrap(Lightable.class, null);

		@Override
		public Boolean apply(Visible input) {
			if (input instanceof Lightable) {
				return !((Lightable)input).isLit();
			}

			return false;
		}

		@Override
		public String getDetailedDescription(Individual host) {
			return "This routine occurs when a lightable prop is visible to " + host.getId().getSimpleName();
		}

		@Override
		@SuppressWarnings("unchecked")
		public WrapperForTwo<Class<? extends Lightable>, Lightable> getEntity() {
			return wrapper;
		}
	}
}