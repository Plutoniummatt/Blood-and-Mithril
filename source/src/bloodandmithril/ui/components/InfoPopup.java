package bloodandmithril.ui.components;

import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;

import java.util.Deque;
import java.util.List;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.Function;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;

/**
 * Displays mouse-over popup info
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class InfoPopup extends Component {

	private final Color borderColor, backGroundColor;
	public final Function<Boolean> expiryFunction;
	private Panel panel;

	/**
	 * Constructor
	 */
	public InfoPopup(Panel panel, Function<Boolean> expiryFunction) {
		this.panel = panel;
		this.expiryFunction = expiryFunction;
		this.borderColor = Color.GRAY;
		this.backGroundColor = Color.BLACK;
	}


	@Override
	public boolean keyPressed(int keyCode) {
		return false;
	}


	@Override
	public boolean leftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		return false;
	}


	@Override
	public void leftClickReleased() {
	}


	@Override
	protected void internalComponentRender() {
		BloodAndMithrilClient.spriteBatch.begin();
		renderRectangle(
			getMouseScreenX() + bottomLeft.getRegionWidth(),
			getMouseScreenY() + bottomLeft.getRegionHeight(),
			panel.width,
			panel.height,
			true,
			backGroundColor
		);

		renderBox(
			getMouseScreenX(),
			getMouseScreenY(),
			panel.width,
			panel.height,
			isActive(),
			borderColor
		);
		BloodAndMithrilClient.spriteBatch.end();

		BloodAndMithrilClient.spriteBatch.begin();
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		panel.render();
		Gdx.gl.glDisable(GL20.GL_BLEND);
		BloodAndMithrilClient.spriteBatch.end();
	}
}