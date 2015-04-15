package bloodandmithril.prop.construction.craftingstation;

import static bloodandmithril.core.BloodAndMithrilClient.isOnScreen;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.ai.task.LightLightable;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.food.animal.ChickenLegItem;
import bloodandmithril.item.items.material.PlankItem;
import bloodandmithril.item.items.material.StickItem;
import bloodandmithril.item.material.wood.StandardWood;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Lightable;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;
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
public class Campfire extends CraftingStation implements Lightable {
	private static final long serialVersionUID = -8876217926271589078L;

	public static TextureRegion CAMPFIRE;
	private boolean lit = false;

	private static final Map<Item, Integer> craftables = Maps.newHashMap();
	static {
		craftables.put(new ChickenLegItem(true), 1);
	}

	/**
	 * Constructor
	 */
	public Campfire(float x, float y) {
		super(x, y, 64, 32, 0.2f);
		setConstructionProgress(0f);
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
		requiredItems.put(StickItem.stick(StandardWood.class), 10);
		requiredItems.put(PlankItem.plank(StandardWood.class), 2);
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
		return lit;
	}


	@Override
	public void update(float delta) {
		super.update(delta);

		if (lit && isOnScreen(position, 50f)) {
			float size1 = Util.getRandom().nextFloat();
			float size2 = Util.getRandom().nextFloat();

			ParticleService.randomVelocityDiminishing(position.cpy().add(0, 13f), 8f, 15f, Colors.FIRE_START, Colors.FIRE_START, size1 * 3.5f, size1 * 16f + 5f, MovementMode.EMBER, Util.getRandom().nextInt(1000), Depth.MIDDLEGROUND, false, Colors.FIRE_END);
			ParticleService.randomVelocityDiminishing(position.cpy().add(0, 13f), 8f, 15f, Colors.FIRE_START, Colors.FIRE_START, size2 * 3.5f, size2 * 16f + 5f, MovementMode.EMBER, Util.getRandom().nextInt(1200), Depth.MIDDLEGROUND, false, Colors.FIRE_END);
			ParticleService.randomVelocityDiminishing(position.cpy().add(0, 13f), 7f, 30f, Colors.LIGHT_SMOKE, Colors.LIGHT_SMOKE, 10f, 0f, MovementMode.EMBER, Util.getRandom().nextInt(4000), Depth.BACKGROUND, false, null);
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
		return lit;
	}


	@Override
	protected int getCraftingSound() {
		return SoundService.campfireCooking;
	}


	@Override
	public void light() {
		this.lit = true;
	}


	@Override
	public void extinguish() {
		this.lit = false;
	}


	@Override
	protected String internalGetTitle() {
		return "Campfire";
	}


	@Override
	public boolean isLit() {
		return lit;
	}


	@Override
	public boolean canDeconstruct() {
		return !lit;
	}


	@Override
	public boolean requiresConstruction() {
		return false;
	}


	@Override
	public boolean canLight() {
		return getConstructionProgress() == 1f;
	}


	@Override
	public void affectIndividual(Individual individual, float delta) {
		individual.decreaseThirst(delta / 600f);
	}
}