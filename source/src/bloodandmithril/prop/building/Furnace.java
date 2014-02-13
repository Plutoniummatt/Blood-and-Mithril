package bloodandmithril.prop.building;


import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.ContextMenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.util.Task;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A Furnace
 *
 * @author Matt
 */
public class Furnace extends Construction {

	/** {@link TextureRegion} of the {@link Furnace} */
	public static TextureRegion furnace;

	/**
	 * Constructor
	 */
	public Furnace(float x, float y) {
		super(x, y, 49, 76, false);
	}


	@Override
	public ContextMenu getContextMenu() {

		ContextMenu menu = new ContextMenu(BloodAndMithrilClient.getMouseScreenX(), BloodAndMithrilClient.getMouseScreenY(),
			new ContextMenuItem(
				"Show info",
				new Task() {
					@Override
					public void execute() {
						UserInterface.addLayeredComponent(
							new MessageWindow(
								"A furnace, able to achieve temperatures hot enough to melt most metals",
								Color.ORANGE,
								BloodAndMithrilClient.WIDTH/2 - 175,
								BloodAndMithrilClient.HEIGHT/2 + 100,
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
	public void synchronize(Prop other) {
	}


	@Override
	protected void internalRender(float constructionProgress) {
		BloodAndMithrilClient.spriteBatch.draw(furnace, position.x - width / 2, position.y);
	}
}