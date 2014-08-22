package bloodandmithril.prop.construction.farm;

import static bloodandmithril.networking.ClientServerInterface.isServer;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.item.items.container.ContainerImpl;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.CropWindow;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.prop.Growable;

import java.util.Set;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A Farm is a {@link Construction} that can be used to produce and harvest {@link Item}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Farm extends Construction {
	private static final long serialVersionUID = -6115136894543038411L;
	private Container crops = new ContainerImpl(2000f, false);

	/**
	 * Constructor
	 */
	protected Farm(float x, float y, int width, int height, float constructionRate, SerializableMappingFunction<Tile, Boolean> canBuildOnTopOf) {
		super(x, y, width, height, true, constructionRate, canBuildOnTopOf);
	}

	/** Returns the {@link TextureRegion} of the {@link Farm} */
	protected abstract TextureRegion getTextureRegion();


	@Override
	protected ContextMenu getCompletedContextMenu() {
		ContextMenu menu = new ContextMenu(BloodAndMithrilClient.getMouseScreenX(), BloodAndMithrilClient.getMouseScreenY(), true);
		Farm thisFarm = this;

		menu.addMenuItem(
			new ContextMenu.MenuItem(
				"Show info",
				() -> {
					UserInterface.addLayeredComponent(
						new MessageWindow(
							getDescription(),
							Color.ORANGE,
							BloodAndMithrilClient.WIDTH/2 - 250,
							BloodAndMithrilClient.HEIGHT/2 + 125,
							500,
							250,
							getTitle(),
							true,
							300,
							150
						)
					);
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);

		menu.addMenuItem(
			new ContextMenu.MenuItem(
				"Show current crop",
				() -> {
					UserInterface.addLayeredComponentUnique(
						new CropWindow(thisFarm)
					);
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);


		menu.addMenuItem(
			new ContextMenu.MenuItem(
				"Harvest",
				() -> {
					if (Domain.getSelectedIndividuals().size() > 1) {
						return;
					}

					if (isServer()) {
						Individual indi = Domain.getSelectedIndividuals().iterator().next();
						indi.getAI().setCurrentTask(
							new TradeWith(indi, thisFarm)
						);
					} else {
						Individual indi = Domain.getSelectedIndividuals().iterator().next();
						ClientServerInterface.SendRequest.sendTradeWithPropRequest(indi, id);
					}
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				new ContextMenu(0, 0,
					true,
					new MenuItem(
						"You have multiple individuals selected",
						() -> {},
						Colors.UI_GRAY,
						Colors.UI_GRAY,
						Colors.UI_GRAY,
						null
					)
				),
				() -> {
					return Domain.getSelectedIndividuals().size() > 1;
				}
			)
		);

		return menu;
	}
	
	
	/**
	 * @return A set of {@link Growable}s that are able to be grown on this farm
	 */
	public abstract Set<Growable> getGrowables();


	@Override
	public Container getContainerImpl() {
		if (getConstructionProgress() == 1f) {
			return crops;
		} else {
			return super.getContainerImpl();
		}
	}
}