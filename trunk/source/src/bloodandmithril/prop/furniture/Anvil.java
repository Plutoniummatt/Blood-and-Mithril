package bloodandmithril.prop.furniture;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.Smith;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.Container;
import bloodandmithril.item.ContainerImpl;
import bloodandmithril.item.Item;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.building.Construction;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * An iron anvil, used to smith metallic items
 *
 * @author Matt
 */
public class Anvil extends Construction implements Container {
	private static final long serialVersionUID = -2103140298239675830L;

	/** {@link TextureRegion} of the {@link Anvil} */
	public static TextureRegion anvil;

	/** Container of this Anvil */
	private ContainerImpl container = new ContainerImpl(0f, true);

	/**
	 * Constructor
	 */
	public Anvil(float x, float y) {
		super(x, y, 44, 18, true, 0f);
		setConstructionProgress(1f);
	}


	@Override
	protected void internalRender(float constructionProgress) {
		spriteBatch.draw(anvil, position.x - width / 2, position.y);
	}


	@Override
	protected Map<Item, Integer> getRequiredMaterials() {
		return newHashMap();
	}


	@Override
	protected ContextMenu getCompletedContextMenu() {
		ContextMenu menu = new ContextMenu(BloodAndMithrilClient.getMouseScreenX(), BloodAndMithrilClient.getMouseScreenY());

		menu.addMenuItem(
			new MenuItem(
				"Show info",
				() -> {
					UserInterface.addLayeredComponent(
						new MessageWindow(
							"An anvil is a basic tool, a block with a hard surface on which another object is struck.  Used to smith metallic objects.",
							Color.ORANGE,
							BloodAndMithrilClient.WIDTH/2 - 250,
							BloodAndMithrilClient.HEIGHT/2 + 125,
							500,
							250,
							"Anvil",
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


		if (Domain.getSelectedIndividuals().size() == 1) {
			final Individual selected = Domain.getSelectedIndividuals().iterator().next();
			menu.addMenuItem(
				new MenuItem(
					"Smith",
					() -> {
						if (ClientServerInterface.isServer()) {
							selected.getAI().setCurrentTask(new Smith(selected, this));
						} else {
							ClientServerInterface.SendRequest.sendSmithRequest(selected, this);
						}
					},
					Color.WHITE,
					Color.GREEN,
					Color.GRAY,
					null
				)
			);
		}

		return menu;
	}


	@Override
	public void synchronizeProp(Prop other) {
		if (other instanceof Anvil) {
			container.synchronizeContainer(((Anvil) other).container);
		} else {
			throw new RuntimeException("Can not synchronize Anvil with " + other.getClass().getSimpleName());
		}
	}


	@Override
	public void update(float delta) {
	}
}