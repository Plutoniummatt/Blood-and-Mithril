package bloodandmithril.prop.plant.grass;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.UpdatedBy;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.RenderPropWith;
import bloodandmithril.graphics.Textures;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.StaticallyRenderedProp;
import bloodandmithril.prop.furniture.MedievalWallTorchProp.NotEmptyTile;
import bloodandmithril.prop.plant.PlantProp;
import bloodandmithril.prop.renderservice.GrassRenderer;
import bloodandmithril.prop.updateservice.NoOpPropUpdateService;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.MessageWindow;

/**
 * Dry grass that grows in the desert
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
@UpdatedBy(NoOpPropUpdateService.class)
@RenderPropWith(GrassRenderer.class)
public class GreenGrass extends PlantProp implements StaticallyRenderedProp {
	private static final long serialVersionUID = 9061021746445536182L;

	public static TextureRegion textureRegionBig, textureRegionSmall;

	static {
		if (ClientServerInterface.isClient()) {
			textureRegionBig = new TextureRegion(Textures.GAME_WORLD_TEXTURE, 1166, 119, 76, 12);
			textureRegionSmall = new TextureRegion(Textures.GAME_WORLD_TEXTURE, 1243, 119, 34, 12);
		}
	}

	private boolean small;

	public GreenGrass(final float x, final float y, final boolean small) {
		super(x, y, small ? 34 : 76, 12, Depth.FRONT, new NotEmptyTile(), false);
		this.small = small;
	}


	@Override
	public void synchronizeProp(final Prop other) {
	}


	@Override
	public ContextMenu getContextMenu() {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			new MenuItem(
				"Show info",
				() -> {
					Wiring.injector().getInstance(UserInterface.class).addLayeredComponent(
						new MessageWindow(
							"Some grass.",
							Color.ORANGE,
							500,
							250,
							"Grass",
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

		return menu;
	}


	@Override
	public boolean canBeUsedAsFireSource() {
		return false;
	}


	@Override
	public String getContextMenuItemLabel() {
		return "Grass";
	}


	@Override
	public void preRender() {
	}


	@Override
	public TextureRegion getTextureRegion() {
		if (small) {
			return textureRegionSmall;
		} else {
			return textureRegionBig;
		}
	}
}