package bloodandmithril.prop.crafting;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static com.google.common.collect.Maps.newHashMap;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.Craft;
import bloodandmithril.character.ai.task.OpenCraftingStation;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.csi.requests.RefreshWindows.RefreshWindowsResponse;
import bloodandmithril.csi.requests.SynchronizePropRequest;
import bloodandmithril.item.Item;
import bloodandmithril.item.equipment.Craftable;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.building.Construction;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

/**
 * Superclass for all {@link Prop}s that craft {@link Item}s
 *
 * @author Matt
 */
public abstract class CraftingStation extends Construction {
	private static final long serialVersionUID = 2177296386331588828L;

	private float craftingProgress;
	private Item currentlyBeingCrafted;
	private Integer occupiedBy;
	private boolean finished;

	/**
	 * Constructor
	 */
	protected CraftingStation(float x, float y, int width, int height, float constructionRate) {
		super(x, y, width, height, true, constructionRate);
	}


	/** Returns the {@link TextureRegion} of the {@link CraftingStation} */
	protected abstract TextureRegion getTextureRegion();

	/** Returns the string description of this {@link CraftingStation} */
	protected abstract String getDescription();

	/** Returns the verb that describes the action of this {@link CraftingStation} */
	public abstract String getAction();

	/** Returns the list of {@link Craftable} {@link Item}s */
	public abstract List<Item> getCraftables();


	/**
	 * @return true by default, override for custom functions
	 */
	public boolean customCanCraft() {
		return true;
	}


	/**
	 * @return The string message to display indicating the reason why something can not be crafted
	 */
	public String getCustomMessage() {
		return "";
	}


	@Override
	protected void internalRender(float constructionProgress) {
		spriteBatch.draw(getTextureRegion(), position.x - width / 2, position.y);
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

		addCraftMenuItem(menu);

		return menu;
	}


	protected void addCraftMenuItem(ContextMenu menu) {
		if (Domain.getSelectedIndividuals().size() > 0) {
			final Individual selected = Domain.getSelectedIndividuals().iterator().next();
			menu.addMenuItem(
				new MenuItem(
					getAction(),
					() -> {
						if (Domain.getSelectedIndividuals().size() == 1) {
							if (ClientServerInterface.isServer()) {
								selected.getAI().setCurrentTask(new OpenCraftingStation(selected, this));
							} else {
								ClientServerInterface.SendRequest.sendOpenCraftingStationRequest(selected, this);
							}
						}
					},
					Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.WHITE,
					Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.GREEN,
					Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.GRAY,
					new ContextMenu(0, 0, new MenuItem("You have multiple individuals selected", () -> {}, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, null)),
					() -> {
						return Domain.getSelectedIndividuals().size() > 1;
					}
				)
			);
		}
	}


	@Override
	public void synchronizeProp(Prop other) {
		this.craftingProgress = ((CraftingStation)other).craftingProgress;
		this.currentlyBeingCrafted = ((CraftingStation)other).currentlyBeingCrafted;
		this.finished = ((CraftingStation)other).finished;
		this.occupiedBy = ((CraftingStation)other).occupiedBy;
	}


	@Override
	public void update(float delta) {
		if (currentlyBeingCrafted != null && occupiedBy != null) {
			occupiedBy = Domain.getIndividuals().get(occupiedBy).getAI().getCurrentTask() instanceof Craft ? occupiedBy : null;
		}
	}


	public float getCraftingProgress() {
		return craftingProgress;
	}


	public Item getCurrentlyBeingCrafted() {
		return currentlyBeingCrafted;
	}


	public void setCurrentlyBeingCrafted(Item currentlyBeingCrafted) {
		this.currentlyBeingCrafted = currentlyBeingCrafted;
	}


	/**
	 * Make progress on the crafting of some {@link Craftable}
	 *
	 * @return whether or not the crafting task should continue.
	 */
	public synchronized boolean craft(Item item, Individual individual, float aiTaskDelay) {
		if (!customCanCraft()) {
			return false;
		}

		if (occupiedBy == null) {
			occupiedBy = individual.getId().getId();
		}

		if (currentlyBeingCrafted == null) {
			currentlyBeingCrafted = item;

			if (enoughMaterialsToCraft(individual, (Craftable)item)) {
				for (Entry<Item, Integer> requiredItem : ((Craftable)item).getRequiredMaterials().entrySet()) {
					for (int i = requiredItem.getValue(); i > 0; i--) {
						individual.takeItem(requiredItem.getKey());
					}
				}
			}

			if (ClientServerInterface.isClient()) {
				UserInterface.refreshRefreshableWindows();
			} else {
				ClientServerInterface.sendNotification(-1, true, true,
					new SynchronizePropRequest.SynchronizePropResponse(this),
					new RefreshWindowsResponse()
				);
			}
		}

		if (occupiedBy == individual.getId().getId()) {
			craftingProgress += aiTaskDelay / ((Craftable)currentlyBeingCrafted).getCraftingDuration();
			if (craftingProgress >= 1f) {
				craftingProgress = 1f;
				finished = true;
			}
			return true;
		}

		return false;
	}


	/**
	 * @return whether the {@link Individual} has enough items to craft a {@link Craftable}
	 */
	public static boolean enoughMaterialsToCraft(Individual individual, Craftable craftable) {
		Map<Item, Integer> inventoryCopy = individual.getInventory();
		for (Entry<Item, Integer> requiredItem : craftable.getRequiredMaterials().entrySet()) {
			Optional<Entry<Item, Integer>> tryFind = Iterables.tryFind(inventoryCopy.entrySet(), toMatch -> {
				return toMatch.getKey().sameAs(requiredItem.getKey());
			});

			if (tryFind.isPresent()) {
				if (requiredItem.getValue() > tryFind.get().getValue()) {
					return false;
				}
			} else {
				return false;
			}
		}

		return true;
	}


	public boolean isFinished() {
		return finished;
	}


	public void takeItem(Individual individual) {
		if (currentlyBeingCrafted != null && finished) {
			individual.giveItem(currentlyBeingCrafted);
			setCurrentlyBeingCrafted(null);
			craftingProgress = 0f;
			finished = false;
		}
	}


	public boolean isOccupied() {
		return occupiedBy != null;
	}
}