package bloodandmithril.prop.plant;

import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;

import java.util.Map;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.furniture.MedievalWallTorch.NotEmptyTile;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Domain.Depth;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

/**
 * A cactus
 * 
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class CactusProp extends PlantProp {
	private static final long serialVersionUID = -7472982320467390007L;

	private final int texture;
	
	private static Map<Integer, TextureRegion> textures;
	static {
		if (ClientServerInterface.isClient()) {
			textures = Maps.newHashMap();
			
			textures.put(1, new TextureRegion(Domain.gameWorldTexture, 499, 41, 58, 90));
			textures.put(2, new TextureRegion(Domain.gameWorldTexture, 559, 5, 64, 126));
			textures.put(3, new TextureRegion(Domain.gameWorldTexture, 625, 33, 58, 98));
			textures.put(4, new TextureRegion(Domain.gameWorldTexture, 685, 33, 58, 89));
			textures.put(5, new TextureRegion(Domain.gameWorldTexture, 745, 5, 64, 126));
			textures.put(6, new TextureRegion(Domain.gameWorldTexture, 811, 41, 58, 90));
		}
	}
	
	/**
	 * Constructor
	 */
	public CactusProp(float x, float y) {
		super(x, y, 0, 0, Depth.MIDDLEGROUND, new NotEmptyTile());
		this.texture = Util.getRandom().nextInt(6) + 1;

		switch (texture) {
		case 1:
		case 6:
			this.width = 58;
			this.height = 90;
		case 2:
		case 5:
			this.width = 64;
			this.height = 126;
		case 3:
		case 4:
			this.width = 58;
			this.height = 98;
		}
	}


	@Override
	public void render() {
		spriteBatch.draw(textures.get(texture), position.x - width / 2, position.y);
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
							"A cactus.",
							Color.ORANGE,
							BloodAndMithrilClient.WIDTH/2 - 250,
							BloodAndMithrilClient.HEIGHT/2 + 125,
							500,
							250,
							"Cactus",
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
		return "Cactus";
	}


	@Override
	public void preRender() {
	}
}