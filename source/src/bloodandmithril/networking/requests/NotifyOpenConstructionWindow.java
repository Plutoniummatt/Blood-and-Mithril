package bloodandmithril.networking.requests;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Response;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.window.ConstructionWindow;
import bloodandmithril.world.Domain;

/**
 * Notification to notify a client to open the construction window
 * 
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class NotifyOpenConstructionWindow implements Response {
	private final int constructionId;
	private final int constructorId;

	/**
	 * Constructor
	 */
	public NotifyOpenConstructionWindow(int constructorId, int constructionId) {
		this.constructorId = constructorId;
		this.constructionId = constructionId;
	}

	
	@Override
	public void acknowledge() {
		Individual constructor = Domain.getIndividual(constructorId);
		Construction construction = (Construction) Domain.getWorld(constructor.getWorldId()).props().getProp(constructionId);
		
		UserInterface.addLayeredComponentUnique(
			new ConstructionWindow(
				BloodAndMithrilClient.WIDTH/2 - 300,
				BloodAndMithrilClient.HEIGHT/2 + 150,
				constructor.getId().getSimpleName() + " interacting with " + construction.getTitle(),
				true,
				constructor,
				construction
			)
		);
	}

	
	@Override
	public int forClient() {
		return -1;
	}

	
	@Override
	public void prepare() {
	}
}