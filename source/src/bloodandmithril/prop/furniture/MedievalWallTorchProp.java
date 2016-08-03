package bloodandmithril.prop.furniture;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import bloodandmithril.character.ai.task.lightlightable.LightLightable;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.UpdatedBy;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.RenderPropWith;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Lightable;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.StaticallyRenderedProp;
import bloodandmithril.prop.renderservice.StaticSpritePropRenderingService;
import bloodandmithril.prop.updateservice.MedievalWallTorchPropUpdateService;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
@UpdatedBy(MedievalWallTorchPropUpdateService.class)
@RenderPropWith(StaticSpritePropRenderingService.class)
public class MedievalWallTorchProp extends Furniture implements Lightable, StaticallyRenderedProp {
	private static final long serialVersionUID = -7830128026417134792L;
	private static final float BURN_DURATION = 300f;

	/** {@link TextureRegion} of the {@link MedievalWallTorchProp} */
	public static TextureRegion MEDIEVAL_WALL_TORCH;
	private boolean lit = false;
	private float burnDurationRemaining = BURN_DURATION;

	public static class NotEmptyTile extends SerializableMappingFunction<Tile, Boolean> {
		private static final long serialVersionUID = -7241384309597437080L;

		public NotEmptyTile() {
		}

		@Override
		public Boolean apply(final Tile input) {
			return !(input instanceof EmptyTile);
		}
	}


	/**
	 * Constructor
	 */
	public MedievalWallTorchProp(final float x, final float y) {
		super(x, y, 13, 30, false);
		canPlaceInFrontOf(new NotEmptyTile());
	}


	@Override
	public void synchronizeProp(final Prop other) {
	}


	@Override
	public ContextMenu getContextMenu() {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);
		final MedievalWallTorchProp thisCampfire = this;
		final GameClientStateTracker gameClientStateTracker = Wiring.injector().getInstance(GameClientStateTracker.class);

		menu.addMenuItem(
			new MenuItem(
				"Show info",
				() -> {
					Wiring.injector().getInstance(UserInterface.class).addLayeredComponent(
						new MessageWindow(
							description(),
							Color.ORANGE,
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
			menu.addMenuItem(ignite);
		}

		return menu;
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
	public void light() {
		this.setBurnDurationRemaining(BURN_DURATION);
		this.setLit(true);
	}


	@Override
	public void extinguish() {
		this.setLit(false);
	}


	@Override
	public boolean isLit() {
		return lit;
	}


	@Override
	public boolean canLight() {
		return true;
	}


	public float getBurnDurationRemaining() {
		return burnDurationRemaining;
	}


	public void setBurnDurationRemaining(final float burnDurationRemaining) {
		this.burnDurationRemaining = burnDurationRemaining;
	}


	public void setLit(final boolean lit) {
		this.lit = lit;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return MEDIEVAL_WALL_TORCH;
	}
}