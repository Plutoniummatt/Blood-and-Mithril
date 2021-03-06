package bloodandmithril.prop.plant;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import bloodandmithril.character.ai.task.harvest.Harvest;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.UpdatedBy;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.RenderPropWith;
import bloodandmithril.graphics.Textures;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.StickItem;
import bloodandmithril.item.material.wood.StandardWood;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Harvestable;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.StaticallyRenderedProp;
import bloodandmithril.prop.furniture.MedievalWallTorchProp.NotEmptyTile;
import bloodandmithril.prop.renderservice.StaticSpritePropRenderingService;
import bloodandmithril.prop.updateservice.NoOpPropUpdateService;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.util.Util;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * A dead bush
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
@UpdatedBy(NoOpPropUpdateService.class)
@RenderPropWith(StaticSpritePropRenderingService.class)
public class DeadDesertBush extends PlantProp implements Harvestable, StaticallyRenderedProp {
	private static final long serialVersionUID = -7472982320467390007L;

	private final int texture;
	private int numberOfSticksLeft;

	private static Map<Integer, TextureRegion> textures;
	static {
		if (ClientServerInterface.isClient()) {
			textures = Maps.newHashMap();

			textures.put(1, new TextureRegion(Textures.GAME_WORLD_TEXTURE, 870, 51, 69, 80));
			textures.put(2, new TextureRegion(Textures.GAME_WORLD_TEXTURE, 940, 78, 54, 53));
			textures.put(3, new TextureRegion(Textures.GAME_WORLD_TEXTURE, 995, 83, 50, 48));
			textures.put(4, new TextureRegion(Textures.GAME_WORLD_TEXTURE, 1046, 68, 42, 63));
		}
	}

	/**
	 * Constructor
	 */
	public DeadDesertBush(final float x, final float y) {
		super(x, y, 0, 0, Depth.MIDDLEGROUND, new NotEmptyTile(), true);
		this.texture = Util.getRandom().nextInt(4) + 1;

		switch (texture) {
		case 1:
			this.width = 69;
			this.height = 80;
			break;
		case 2:
			this.width = 54;
			this.height = 53;
			break;
		case 3:
			this.width = 50;
			this.height = 48;
			break;
		case 4:
			this.width = 42;
			this.height = 63;
			break;
		}

		numberOfSticksLeft = 10 + Util.getRandom().nextInt(11);
	}


	@Override
	public TextureRegion getTextureRegion() {
		return textures.get(texture);
	}


	@Override
	public void synchronizeProp(final Prop other) {
		this.numberOfSticksLeft = ((DeadDesertBush) other).numberOfSticksLeft;
	}


	@Override
	public ContextMenu getContextMenu() {
		final GameClientStateTracker gameClientStateTracker = Wiring.injector().getInstance(GameClientStateTracker.class);

		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);
		menu.addMenuItem(
			new MenuItem(
				"Show info",
				() -> {
					Wiring.injector().getInstance(UserInterface.class).addLayeredComponent(
						new MessageWindow(
							"A rather dead looking bush, a good source of sticks..",
							Color.ORANGE,
							500,
							250,
							"Dead bush",
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

		if (gameClientStateTracker.getSelectedIndividuals().size() == 1) {
			menu.addMenuItem(
				new MenuItem(
					"Harvest sticks",
					() -> {
						final Individual individual = gameClientStateTracker.getSelectedIndividuals().iterator().next();
						if (ClientServerInterface.isServer()) {
							try {
								individual.getAI().setCurrentTask(
									new Harvest(individual, DeadDesertBush.this)
								);
							} catch (final NoTileFoundException e) {
							}
						} else {
							ClientServerInterface.SendRequest.sendHarvestRequest(individual.getId().getId(), id);
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
	public boolean canBeUsedAsFireSource() {
		return false;
	}


	@Override
	public String getContextMenuItemLabel() {
		return "Dead bush";
	}


	@Override
	public Collection<Item> harvest(final boolean canReceive) {
		final List<Item> sticks = Lists.newLinkedList();
		for (int i = numberOfSticksLeft; i > 0; i--) {
			sticks.add(StickItem.stick(StandardWood.class));
		}
		return sticks;
	}


	@Override
	public boolean destroyUponHarvest() {
		return true;
	}
}