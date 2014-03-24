package bloodandmithril.prop.building;

import static bloodandmithril.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.csi.ClientServerInterface.isClient;
import static bloodandmithril.ui.UserInterface.refreshInventoryWindows;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map.Entry;
import java.util.Set;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.csi.requests.AddLightRequest;
import bloodandmithril.csi.requests.SynchronizePropRequest;
import bloodandmithril.csi.requests.TransferItems;
import bloodandmithril.graphics.Light;
import bloodandmithril.item.Item;
import bloodandmithril.item.material.fuel.Coal;
import bloodandmithril.item.material.mineral.Ashes;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.ContextMenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.util.Task;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A Furnace
 *
 * @author Matt
 */
public class Furnace extends ConstructionWithContainer {

	/** {@link TextureRegion} of the {@link Furnace} */
	public static TextureRegion FURANCE, FURNACE_BURNING;

	/** The amount of time taken to smelt one batch of material, in seconds */
	private static final float SMELTING_DURATION = 10f;
	
	/** The heat level of {@link Furnace}s */
	private static final int HEAT_LEVEL = 2500;
	
	/** The duration which this furnace will combust/smelt, in seconds */
	private float combustionDurationRemaining, smeltingDurationRemaining;

	/** The {@link Light} that will be rendered if this {@link Furnace} is lit */
	private Light light;

	/** The ID of the {@link Light} that will be rendered if this {@link Furnace} is lit */
	private int lightId;

	/** True if burning */
	private boolean burning, smelting;

	/**
	 * Constructor
	 */
	public Furnace(float x, float y) {
		super(x, y, 49, 76, false, 500f);
	}


	@Override
	public void synchronize(Prop other) {
		if (other instanceof Furnace) {
			this.container.synchronize(((Furnace)other).container);
			this.burning = ((Furnace) other).burning;
			this.combustionDurationRemaining = ((Furnace) other).combustionDurationRemaining;
			this.smeltingDurationRemaining = ((Furnace) other).smeltingDurationRemaining;
			this.lightId = ((Furnace) other).lightId;
			this.light = Domain.getLights().get(((Furnace) other).lightId);
			this.smelting = ((Furnace) other).smelting;
		} else {
			throw new RuntimeException("Can not synchronize Furnace with " + other.getClass().getSimpleName());
		}
	}


	/**
	 * Ignites this furnace
	 */
	public synchronized void ignite() {
		if (burning) {
			return;
		}
		
		burning = true;
		
		lightId = ParameterPersistenceService.getParameters().getNextLightId();
		light = new Light(500, position.x, position.y + 4, Color.ORANGE, 1f, 0f, 1f);
		Domain.getLights().put(lightId, light);
		
		smelt();
	}
	
	
	/**
	 * Begin smelting
	 */
	public synchronized void smelt() {
		if (smelting) {
			return;
		}
		
		if (!container.getInventory().isEmpty()) {
			smelting = true;
			smeltingDurationRemaining = SMELTING_DURATION;
		}
	}


	@Override
	protected void internalRender(float constructionProgress) {
		if (burning && light != null) {
			float intensity = 0.75f + 0.25f * Util.getRandom().nextFloat();
			light.intensity = intensity;
		}

		if (burning) {
			spriteBatch.draw(FURNACE_BURNING, position.x - width / 2, position.y);
		} else {
			spriteBatch.draw(FURANCE, position.x - width / 2, position.y);
		}
	}


	public float getCombustionDurationRemaining() {
		return combustionDurationRemaining;
	}
	
	
	public float getSmeltingDurationRemaining() {
		return smeltingDurationRemaining;
	}


	public synchronized void setCombustionDurationRemaining(float combustionDurationRemaining) {
		synchronized (this) {
			this.combustionDurationRemaining = combustionDurationRemaining;
		}
	}


	public boolean isBurning() {
		return burning;
	}


	@Override
	public void update(float delta) {
		if (burning) {
			synchronized (this) {
				this.combustionDurationRemaining -= delta;
				
				if (smelting) {
					smeltingDurationRemaining -= delta;
				}
				
				if (this.smeltingDurationRemaining <= 0f) {
					smelting = false;
					smeltItems();
					if (!isClient()) {
						ClientServerInterface.sendNotification(
							-1,
							true,
							true,
							new AddLightRequest.RemoveLightNotification(lightId),
							new SynchronizePropRequest.SynchronizePropResponse(this),
							new TransferItems.RefreshWindowsResponse()
						);
					} else {
						refreshInventoryWindows();
					}
				}
				
				if (this.combustionDurationRemaining <= 0f) {
					burning = false;
					smelting = false;
					Domain.getLights().remove(lightId);
					if (!isClient()) {
						ClientServerInterface.sendNotification(
							-1,
							true,
							true,
							new AddLightRequest.RemoveLightNotification(lightId),
							new SynchronizePropRequest.SynchronizePropResponse(this),
							new TransferItems.RefreshWindowsResponse()
						);
					} else {
						refreshInventoryWindows();
					}
				}
			}
		}
	}


	/**
	 * Transmutes all items in the {@link Furnace} according to {@link Item#combust(float, float)}
	 */
	private synchronized void smeltItems() {
		synchronized(container) {
			Set<Entry<Item, Integer>> existing = newHashMap(container.getInventory()).entrySet();
			container.getInventory().clear();
			
			for (Entry<Item, Integer> entry : existing) {
				for (int i = 0; i < entry.getValue(); i++) {
					if (entry.getKey() instanceof Coal) {
						container.giveItem(new Ashes());
					} else {
						container.giveItem(entry.getKey().combust(HEAT_LEVEL));
					}
				}
			}
		}
	}


	public boolean isSmelting() {
		return smelting;
	}


	@Override
	protected ContextMenu getConstructionContextMenu() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	protected ContextMenu getCompletedContextMenu() {
		ContextMenu menu = new ContextMenu(BloodAndMithrilClient.getMouseScreenX(), BloodAndMithrilClient.getMouseScreenY(),
			new ContextMenuItem(
				"Show info",
				new Task() {
					@Override
					public void execute() {
						UserInterface.addLayeredComponent(
							new MessageWindow(
								"A furnace, able to achieve temperatures hot enough to melt most metals",
								Color.ORANGE,
								BloodAndMithrilClient.WIDTH/2 - 175,
								BloodAndMithrilClient.HEIGHT/2 + 100,
								350,
								200,
								"Furnace",
								true,
								350,
								200
							)
						);
					}
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);

		if (Domain.getSelectedIndividuals().size() == 1) {
			final Individual selected = Domain.getSelectedIndividuals().iterator().next();
			ContextMenuItem openChestMenuItem = new ContextMenuItem(
				"Open furnace",
				new Task() {
					@Override
					public void execute() {
						if (ClientServerInterface.isServer()) {
							selected.getAI().setCurrentTask(
								new TradeWith(selected, container)
							);
						} else {
							ConstructionContainer chestContainer = (ConstructionContainer) container;
							ClientServerInterface.SendRequest.sendTradeWithPropRequest(selected, chestContainer.propId);
						}
					}
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			);
			menu.addMenuItem(openChestMenuItem);
		}

		return menu;
	}
}