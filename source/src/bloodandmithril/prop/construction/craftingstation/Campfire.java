package bloodandmithril.prop.construction.craftingstation;

import java.util.Map;

import bloodandmithril.character.ai.task.LightCampfire;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.food.animal.ChickenLeg;
import bloodandmithril.networking.ClientServerInterface;
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
public class Campfire extends CraftingStation {
	private static final long serialVersionUID = -8876217926271589078L;
	private boolean lit;

	public static TextureRegion CAMPFIRE;

	private static final Map<Item, Integer> craftables = Maps.newHashMap();
	static {
		craftables.put(new ChickenLeg(true), 1);
	}

	/**
	 * Constructor
	 */
	public Campfire(float x, float y) {
		super(x, y, 64, 32, 0f);
		setConstructionProgress(1f);
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
	public Map<Item, Integer> getCraftables() {
		return craftables;
	}


	@Override
	public String getDescription() {
		return "A campfire, can be used for some basic cooking, as well as provide some warmth and light.";
	}


	@Override
	public String getTitle() {
		return "Campfire";
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


	public void setLit(boolean lit) {
		this.lit = lit;
	}


	@Override
	public void update(float delta) {
		super.update(delta);

		if (lit) {
			ParticleService.randomVelocityDiminishing(position.cpy().add(0, 15f), 7f, 30f, Color.ORANGE, Util.getRandom().nextFloat() * 3f, Util.getRandom().nextFloat() * 8f, MovementMode.EMBER, Util.getRandom().nextInt(1000));
			ParticleService.randomVelocityDiminishing(position.cpy().add(0, 15f), 7f, 30f, Color.GRAY, 1f, 0f, MovementMode.EMBER, Util.getRandom().nextInt(1500));
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
						selected.getAI().setCurrentTask(new LightCampfire(selected, thisCampfire));
					} catch (NoTileFoundException e) {}
				} else {
					ClientServerInterface.SendRequest.sendLightCampfireRequest(selected, thisCampfire);
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
}