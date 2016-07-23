package bloodandmithril.character.ai.task.lightlightable;

import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.ai.task.lightlightable.LightLightable.LightFire;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.FireLighter;
import bloodandmithril.prop.Lightable;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Domain;

/**
 * Executes {@link LightFire}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class LightFireExecutor implements AITaskExecutor {

	@Override
	public void execute(AITask aiTask, float delta) {
		LightFire task = (LightFire) aiTask;
		LightLightable parent = task.getParent();
		
		final Individual host = Domain.getIndividual(task.getHostId().getId());

		if (!Domain.getWorld(host.getWorldId()).props().hasProp(parent.lightableId)) {
			return;
		}

		final Lightable lightable = (Lightable) Domain.getWorld(host.getWorldId()).props().getProp(parent.lightableId);

		if (!lightable.canLight()) {
			task.lit = true;
			return;
		}

		if (host.getInteractionBox().isWithinBox(((Prop) lightable).position)) {
			final FireLighter fireLighter = host.getFireLighter();
			if (fireLighter != null) {
				fireLighter.fireLightingEffect((Prop) lightable);
				lightable.light();
			} else {
				host.speak("I need fire lighting equipment", 2000);
			}
			task.lit = true;
		}
	}

	
	@Override
	public boolean isComplete(AITask aiTask) {
		LightFire task = (LightFire) aiTask;
		LightLightable parent = task.getParent();
		
		final Prop prop = Domain.getWorld(task.getHost().getWorldId()).props().getProp(parent.lightableId);
		return prop != null && (task.lit || ((Lightable) prop).isLit()) || !((Lightable) prop).canLight();
	}
	

	@Override
	public boolean uponCompletion(AITask aiTask) {
		return false;
	}
}
