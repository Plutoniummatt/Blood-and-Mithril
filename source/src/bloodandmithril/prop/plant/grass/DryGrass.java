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
public class DryGrass extends PlantProp implements StaticallyRenderedProp {
	private static final long serialVersionUID = 9061021746445536182L;

	public static TextureRegion textureRegion;

	static {
		if (ClientServerInterface.isClient()) {
			textureRegion = new TextureRegion(Textures.GAME_WORLD_TEXTURE, 1089, 119, 76, 12);
		}
	}

	public DryGrass(final float x, final float y) {
		super(x, y, 76, 12, Depth.FRONT, new NotEmptyTile(), false);
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
							"A patch of dry grass.",
							Color.ORANGE,
							500,
							250,
							"Dry grass",
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
		return "Dry grass";
	}


	@Override
	public void preRender() {
	}


	@Override
	public TextureRegion getTextureRegion() {
		return textureRegion;
	}
}