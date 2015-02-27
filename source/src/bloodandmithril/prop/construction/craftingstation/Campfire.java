package bloodandmithril.prop.construction.craftingstation;

import static bloodandmithril.core.BloodAndMithrilClient.isOnScreen;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.ai.task.LightLightable;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.item.Fuel;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.ContainerImpl;
import bloodandmithril.item.items.food.animal.ChickenLeg;
import bloodandmithril.item.items.material.Log;
import bloodandmithril.item.items.material.Plank;
import bloodandmithril.item.items.material.Stick;
import bloodandmithril.item.material.wood.StandardWood;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Lightable;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Domain.Depth;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

/**
 * A campfire, provides light, warm, and something to cook with.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Campfire extends FueledCraftingStation implements Lightable {
	private static final long serialVersionUID = -8876217926271589078L;

	public static TextureRegion CAMPFIRE;

	private static final Map<Item, Integer> craftables = Maps.newHashMap();
	static {
		craftables.put(new ChickenLeg(true), 1);
	}

	/**
	 * Constructor
	 */
	public Campfire(float x, float y) {
		super(x, y, 64, 32, 0.2f, new ContainerImpl(100f, 200));
		setConstructionProgress(1f);
	}


	@Override
	public void synchronizeProp(Prop other) {
		if (other instanceof Campfire) {
			super.synchronizeProp(other);
		} else {
			throw new RuntimeException("Can not synchronize Campfire with " + other.getClass().getSimpleName());
		}
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return CAMPFIRE;
	}


	@Override
	public String getAction() {
		return "Cook";
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		Map<Item, Integer> requiredItems = newHashMap();
		requiredItems.put(Stick.stick(StandardWood.class), 1);
		requiredItems.put(Plank.plank(StandardWood.class), 1);
		return requiredItems;
	}



	@Override
	public Map<Item, Integer> getCraftables() {
		return craftables;
	}


	@Override
	public String getDescription() {
		return "A campfire, can be used for some basic cooking, as well as provide some warmth and light.";
	}


	@Override
	public void preRender() {
	}


	@Override
	public String getCustomMessage() {
		return "The campfire is not lit.";
	}


	@Override
	public boolean customCanCraft() {
		return isBurning();
	}


	@Override
	public void update(float delta) {
		super.update(delta);

		if (isBurning() && isOnScreen(position, 50f)) {
			ParticleService.randomVelocityDiminishing(position.cpy().add(0, 13f), 10f, 15f, Colors.FIRE_START, Util.getRandom().nextFloat() * 3.5f, Util.getRandom().nextFloat() * 10f + 5f, MovementMode.EMBER, Util.getRandom().nextInt(1000), Depth.MIDDLEGROUND, false, Colors.FIRE_END);
			ParticleService.randomVelocityDiminishing(position.cpy().add(0, 13f), 10f, 15f, Colors.FIRE_START, Util.getRandom().nextFloat() * 3.5f, Util.getRandom().nextFloat() * 10f + 5f, MovementMode.EMBER, Util.getRandom().nextInt(1000), Depth.MIDDLEGROUND, false, Colors.FIRE_END);
			ParticleService.randomVelocityDiminishing(position.cpy().add(0, 13f), 7f, 30f, Colors.LIGHT_SMOKE, 10f, 0f, MovementMode.EMBER, Util.getRandom().nextInt(4000), Depth.BACKGROUND, false, null);
		}
	}


	@Override
	protected ContextMenu getCompletedContextMenu() {
		ContextMenu superCompletedContextMenu = super.getCompletedContextMenu();
		final Campfire thisCampfire = this;

		MenuItem ignite = new MenuItem(
			"Ignite",
			() -> {
				if (Domain.getSelectedIndividuals().size() > 1) {
					return;
				}

				Individual selected = Domain.getSelectedIndividuals().iterator().next();
				if (ClientServerInterface.isServer()) {
					try {
						selected.getAI().setCurrentTask(new LightLightable(selected, thisCampfire, false));
					} catch (NoTileFoundException e) {}
				} else {
					ClientServerInterface.SendRequest.sendLightLightableRequest(selected, thisCampfire);
				}
			},
			Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_GRAY : Color.WHITE,
			Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_GRAY : Color.GREEN,
			Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_GRAY : Color.GRAY,
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
		);

		if (Domain.getSelectedIndividuals().size() == 1) {
			superCompletedContextMenu.addMenuItem(ignite);
		}

		return superCompletedContextMenu;
	}


	@Override
	public boolean canBeUsedAsFireSource() {
		return isBurning();
	}


	@Override
	protected int getCraftingSound() {
		return SoundService.campfireCooking;
	}


	@Override
	public void light() {
		ignite();
	}


	@Override
	public void extinguish() {
		super.extinguish();
	}


	@Override
	protected String internalGetTitle() {
		return "Campfire";
	}


	@Override
	public boolean isLit() {
		return isBurning();
	}


	@Override
	public boolean canDeconstruct() {
		return !isBurning();
	}


	@Override
	public boolean requiresConstruction() {
		return false;
	}


	@Override
	public boolean isValidFuel(Item item) {
		return item instanceof Plank || item instanceof Stick || item instanceof Log;
	}


	@Override
	public float deriveCombustionDuration(Item item) {
		if (isValidFuel(item)) {
			return ((Fuel) item).getCombustionDuration();
		}
		return 0;
	}
}