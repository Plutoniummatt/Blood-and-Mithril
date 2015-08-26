package bloodandmithril.character.ai.routine.condition;

import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.ai.routine.Condition;
import bloodandmithril.core.Copyright;
import bloodandmithril.prop.Lightable;
import bloodandmithril.util.SerializableFunction;

/**
 * Condition to test whether a {@link Lightable} is lit
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class LightableUnlit implements Condition {
	private static final long serialVersionUID = -4916637727455845007L;
	private SerializableFunction<Visible> lightableGenerator;
	
	public LightableUnlit(SerializableFunction<Visible> lightableGenerator) {
		this.lightableGenerator = lightableGenerator;
	}
	
	@Override
	public boolean met() {
		Visible lightable = lightableGenerator.call();
		if (lightable instanceof Lightable) {
			return !((Lightable) lightable).isLit();
		}
		
		return false;
	}
}