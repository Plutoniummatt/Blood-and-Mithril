package bloodandmithril.prop.building;


import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.character.ai.task.Trading;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.ContextMenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.util.Task;
import bloodandmithril.util.Util;
import bloodandmithril.world.GameWorld;
import bloodandmithril.world.GameWorld.Light;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A Furnace
 *
 * @author Matt
 */
public class Furnace extends ConstructionWithContainer {

	/** {@link TextureRegion} of the {@link Furnace} */
	public static TextureRegion furnace, furnaceBurning;

	public static final float minTemp = 1400f, maxTemp = 2500f;

	/** The duration which this furnace will combust, in seconds */
	private float combustionDurationRemaining;
	
	/** The {@link Light} that will be rendered if this {@link Furnace} is lit */
	private Light bottomLight; 

	/** Temperature of the {@link Furnace} */
	private float temperature;

	/** True if burning */
	private boolean burning;

	/**
	 * Constructor
	 */
	public Furnace(float x, float y) {
		super(x, y, 49, 76, false, 500f);
	}


	@Override
	public ContextMenu getContextMenu() {

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
		
		if (GameWorld.selectedIndividuals.size() == 1 && !(GameWorld.selectedIndividuals.iterator().next().getAI().getCurrentTask() instanceof Trading)) {
			final Individual selected = GameWorld.selectedIndividuals.iterator().next();
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


	@Override
	public void synchronize(Prop other) {
		if (other instanceof Furnace) {
			this.setTemperature(((Furnace) other).getTemperature());
			this.container.synchronize(((Furnace)other).container);
		} else {
			throw new RuntimeException("Can not synchronize Furnace with " + other.getClass().getSimpleName());
		}
	}


	/**
	 * Ignites this furnace
	 */
	public void ignite() {
		burning = true;
		setTemperature(1400f);
		
		bottomLight = new Light(500, position.x, position.y + 4, Color.ORANGE, 1f);
		
		GameWorld.lights.add(bottomLight);
	}


	@Override
	protected void internalRender(float constructionProgress) {
		if (burning) {
			float intensity = 0.75f + 0.25f * Util.getRandom().nextFloat();
			bottomLight.intensity = intensity;
		}
		
		if (burning) {
			BloodAndMithrilClient.spriteBatch.draw(furnaceBurning, position.x - width / 2, position.y);
		} else {
			BloodAndMithrilClient.spriteBatch.draw(furnace, position.x - width / 2, position.y);
		}
	}
	

	/**
	 * Returns the {@link #temperature} of this furnace.
	 */
	public float getTemperature() {
		return temperature;
	}


	public float getCombustionDurationRemaining() {
		return combustionDurationRemaining;
	}


	public void setCombustionDurationRemaining(float combustionDurationRemaining) {
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
			}
		}
	}


	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}
}