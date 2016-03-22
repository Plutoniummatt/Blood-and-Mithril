package bloodandmithril.prop.plant;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.WorldRenderer;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.furniture.MedievalWallTorchProp.NotEmptyTile;
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
public class GreenGrass extends PlantProp {
	private static final long serialVersionUID = 9061021746445536182L;

	public static TextureRegion textureRegionBig, textureRegionSmall;

	static {
		if (ClientServerInterface.isClient()) {
			textureRegionBig = new TextureRegion(WorldRenderer.gameWorldTexture, 1166, 119, 76, 12);
			textureRegionSmall = new TextureRegion(WorldRenderer.gameWorldTexture, 1243, 119, 34, 12);
		}
	}

	private boolean small;

	public GreenGrass(float x, float y, boolean small) {
		super(x, y, small ? 34 : 76, 12, Depth.FRONT, new NotEmptyTile(), false);
		this.small = small;
	}


	@Override
	public void render(Graphics graphics) {
		if (small) {
			graphics.getSpriteBatch().draw(textureRegionSmall, position.x - width / 2, position.y - 5);
		} else {
			graphics.getSpriteBatch().draw(textureRegionBig, position.x - width / 2, position.y - 5);
		}
	}


	@Override
	public void synchronizeProp(Prop other) {
	}


	@Override
	public ContextMenu getContextMenu() {
		ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			new MenuItem(
				"Show info",
				() -> {
					UserInterface.addLayeredComponent(
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
	public void update(float delta) {
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
}