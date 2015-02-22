package bloodandmithril.prop.furniture;

import static bloodandmithril.core.BloodAndMithrilClient.isOnScreen;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import bloodandmithril.character.ai.task.LightLightable;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Lightable;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Domain.Depth;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class MedievalWallTorch extends Furniture implements Lightable {
	private static final long serialVersionUID = -7830128026417134792L;
	private static final float BURN_DURATION = 300f;

	/** {@link TextureRegion} of the {@link MedievalWallTorch} */
	public static TextureRegion medievalWallTorch;
	private boolean lit = false;
	private float burnDurationRemaining = BURN_DURATION;

	/**
	 * Constructor
	 */
	public MedievalWallTorch(float x, float y) {
		super(x, y, 13, 30, false);
		canPlaceInFrontOf(new SerializableMappingFunction<Tile, Boolean>() {
			private static final long serialVersionUID = -7241384309597437080L;

			@Override
			public Boolean apply(Tile input) {
				return !(input instanceof EmptyTile);
			}
		});
	}


	@Override
	public void render() {
		spriteBatch.draw(medievalWallTorch, position.x - width / 2, position.y);
	}


	@Override
	public void synchronizeProp(Prop other) {
	}


	@Override
	public ContextMenu getContextMenu() {
		ContextMenu menu = new ContextMenu(BloodAndMithrilClient.getMouseScreenX(), BloodAndMithrilClient.getMouseScreenY(), true);
		final MedievalWallTorch thisCampfire = this;

		menu.addMenuItem(
			new MenuItem(
				"Show info",
				() -> {
					UserInterface.addLayeredComponent(
						new MessageWindow(
							description(),
							Color.ORANGE,
							BloodAndMithrilClient.WIDTH/2 - 250,
							BloodAndMithrilClient.HEIGHT/2 + 125,
							500,
							250,
							"Medieval wall torch",
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
			menu.addMenuItem(ignite);
		}

		return menu;
	}


	@Override
	public void update(float delta) {
		if (lit) {

			if (isOnScreen(position, 50f)) {
				Vector2 firePosition = position.cpy().add(0, 23);
				ParticleService.randomVelocityDiminishing(firePosition, 3f, 15f, Color.ORANGE, Util.getRandom().nextFloat() * 3f, 7f, MovementMode.EMBER, Util.getRandom().nextInt(800), Depth.MIDDLEGROUND, false, Color.RED);
				ParticleService.randomVelocityDiminishing(firePosition, 3f, 10f, Colors.LIGHT_SMOKE, 8f, 0f, MovementMode.EMBER, Util.getRandom().nextInt(3000), Depth.BACKGROUND, false, null);
			}
			burnDurationRemaining -= delta;

			if (burnDurationRemaining <= 0f) {
				lit = false;
			}
		}
	}


	public String description() {
		return "A torch placed on a wall.";
	}


	@Override
	public boolean canBeUsedAsFireSource() {
		return true;
	}


	@Override
	public String getContextMenuItemLabel() {
		return "Medieval wall torch";
	}


	@Override
	public void preRender() {
	}


	@Override
	public void light() {
		this.burnDurationRemaining = BURN_DURATION;
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
}