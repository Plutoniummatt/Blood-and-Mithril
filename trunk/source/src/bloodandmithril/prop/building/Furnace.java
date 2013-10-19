package bloodandmithril.prop.building;


import bloodandmithril.Fortress;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.ContextMenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.util.Task;
import bloodandmithril.util.Util;
import bloodandmithril.world.GameWorld;
import bloodandmithril.world.GameWorld.Light;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A Furnace
 *
 * @author Matt
 */
public class Furnace extends Building {

	/** {@link TextureRegion} of the {@link Furnace} */
	private static TextureRegion furnace = new TextureRegion(GameWorld.gameWorldTexture, 350, 175, 57, 68);

	/** Furnace lighting */
	private final Light light;

	/**
	 * Constructor
	 */
	public Furnace(float x, float y) {
		super(x, y, 57, 68, true);
		light = new Light(300, x + 10, y + 32, Color.ORANGE, 0.2f);
		GameWorld.lights.add(light);
	}


	@Override
	public ContextMenu getContextMenu() {

		ContextMenu menu = new ContextMenu(Fortress.getMouseScreenX(), Fortress.getMouseScreenY(),
			new ContextMenuItem(
				"Oh hai I'm a furnace!",
				new Task() {
					@Override
					public void execute() {
						UserInterface.addLayeredComponent(
							new MessageWindow(
								"You clicked me",
								Color.ORANGE,
								Fortress.getMouseScreenX(),
								Fortress.getMouseScreenY(),
								350,
								200,
								"Furnace",
								true,
								350,
								200
							)
						);
					}
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
	public void render() {
		float alpha = 0.9f + 0.01f * (Util.getRandom().nextBoolean() ? -1f : 1f);
		light.color.a = alpha < 0.8f ? 0.8f : alpha > 1f ? 1f : alpha;
		Fortress.spriteBatch.draw(furnace, position.x - width / 2, position.y);
	}
}