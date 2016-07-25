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
 * Green grass with a yellow flower
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
@UpdatedBy(NoOpPropUpdateService.class)
@RenderPropWith(GrassRenderer.class)
public class GrassWithYellowFlower extends PlantProp implements StaticallyRenderedProp {
	private static final long serialVersionUID = -8965555997544274732L;
	
	public static TextureRegion TEXTURE;

	static {
		if (ClientServerInterface.isClient()) {
			TEXTURE = new TextureRegion(Textures.GAME_WORLD_TEXTURE, 1178, 68, 37, 50);
		}
	}

	public GrassWithYellowFlower(final float x, final float y, final boolean small) {
		super(x, y, 37, 50, Depth.FRONT, new NotEmptyTile(), false);
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
							"Some grass, there's a yellow flower growing here.",
							Color.ORANGE,
							500,
							250,
							"Flower",
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
		return "Flower";
	}


	@Override
	public void preRender() {
	}


	@Override
	public TextureRegion getTextureRegion() {
		return TEXTURE;
	}
}