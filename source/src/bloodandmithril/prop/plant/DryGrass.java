package bloodandmithril.prop.plant;

import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.WorldRenderer;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.furniture.MedievalWallTorchProp.NotEmptyTile;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.MessageWindow;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Dry grass that grows in the desert
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class DryGrass extends PlantProp {
	private static final long serialVersionUID = 9061021746445536182L;

	public static TextureRegion textureRegion;

	static {
		if (ClientServerInterface.isClient()) {
			textureRegion = new TextureRegion(WorldRenderer.gameWorldTexture, 1089, 119, 76, 12);
		}
	}

	public DryGrass(float x, float y) {
		super(x, y, 76, 12, Depth.FRONT, new NotEmptyTile(), false);
	}


	@Override
	public void render() {
		spriteBatch.draw(textureRegion, position.x - width / 2, position.y - 5);
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
							"A patch of dry grass.",
							Color.ORANGE,
							500,
							250,
							"Dead bush",
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
		return "Dry grass";
	}


	@Override
	public void preRender() {
	}
}