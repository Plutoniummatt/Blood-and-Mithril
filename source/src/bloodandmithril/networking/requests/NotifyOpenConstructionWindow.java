package bloodandmithril.networking.requests;

import com.google.inject.Inject;

import bloodandmithril.character.individuals.Individual;
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
	private static final long serialVersionUID = 5622673656804734492L;

	@Inject private transient UserInterface userInterface;

	private final int constructionId;
	private final int constructorId;

	/**
	 * Constructor
	 */
	public NotifyOpenConstructionWindow(final int constructorId, final int constructionId) {
		this.constructorId = constructorId;
		this.constructionId = constructionId;
	}


	@Override
	public void acknowledge() {
		final Individual constructor = Domain.getIndividual(constructorId);
		final Construction construction = (Construction) Domain.getWorld(constructor.getWorldId()).props().getProp(constructionId);

		userInterface.addLayeredComponentUnique(
			new ConstructionWindow(
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