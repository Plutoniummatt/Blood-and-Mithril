package bloodandmithril.prop.construction.craftingstation;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.ai.task.lightlightable.LightLightable;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Name;
import bloodandmithril.core.UpdatedBy;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.RenderPropWith;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.food.animal.ChickenLegItem;
import bloodandmithril.item.items.material.PlankItem;
import bloodandmithril.item.items.material.StickItem;
import bloodandmithril.item.material.wood.StandardWood;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Lightable;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.renderservice.ConstructionRenderingService;
import bloodandmithril.prop.updateservice.CampfireUpdateService;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * A campfire, provides light, warmth, and something to cook with.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@Name(name = "Campfire")
@UpdatedBy(CampfireUpdateService.class)
@RenderPropWith(ConstructionRenderingService.class)
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
	public Campfire(final float x, final float y) {
		super(x, y, 64, 32, 0.2f);
		setConstructionProgress(0f);
	}


	@Override
	public void synchronizeProp(final Prop other) {
		if (other instanceof Campfire) {
			super.synchronizeProp(other);
		} else {
			throw new RuntimeException("Can not synchronize Campfire with " + other.getClass().getSimpleName());
		}
	}


	@Override
	public TextureRegion getTextureRegion() {
		return CAMPFIRE;
	}


	@Override
	public String getAction() {
		return "Cook";
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		final Map<Item, Integer> requiredItems = newHashMap();
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
	protected ContextMenu getCompletedContextMenu() {
		final GameClientStateTracker gameClientStateTracker = Wiring.injector().getInstance(GameClientStateTracker.class);
		final ContextMenu superCompletedContextMenu = super.getCompletedContextMenu();
		final Campfire thisCampfire = this;

		final MenuItem ignite = new MenuItem(
			"Ignite",
			() -> {
				if (gameClientStateTracker.getSelectedIndividuals().size() > 1) {
					return;
				}

				final Individual selected = gameClientStateTracker.getSelectedIndividuals().iterator().next();
				if (ClientServerInterface.isServer()) {
					try {
						selected.getAI().setCurrentTask(new LightLightable(selected, thisCampfire, false));
					} catch (final NoTileFoundException e) {}
				} else {
					ClientServerInterface.SendRequest.sendLightLightableRequest(selected, thisCampfire);
				}
			},
			gameClientStateTracker.getSelectedIndividuals().size() > 1 ? Colors.UI_GRAY : Color.WHITE,
			gameClientStateTracker.getSelectedIndividuals().size() > 1 ? Colors.UI_GRAY : Color.GREEN,
			gameClientStateTracker.getSelectedIndividuals().size() > 1 ? Colors.UI_GRAY : Color.GRAY,
			() -> { return new ContextMenu(0, 0,
				true,
				new MenuItem(
					"You have multiple individuals selected",
					() -> {},
					Colors.UI_GRAY,
					Colors.UI_GRAY,
					Colors.UI_GRAY,
					null
				)
			);},
			() -> {
				return gameClientStateTracker.getSelectedIndividuals().size() > 1;
			}
		);

		if (gameClientStateTracker.getSelectedIndividuals().size() == 1) {
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
	public void affectIndividual(final Individual individual, final float delta) {
		individual.decreaseThirst(delta / 600f);
	}
}