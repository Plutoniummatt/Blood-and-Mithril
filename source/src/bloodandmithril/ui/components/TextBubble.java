package bloodandmithril.ui.components;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.List;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util.Colors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.math.Vector2;

/**
 * Generic text bubble
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class TextBubble extends Component {
	private String text;
	private Vector2 position;
	public final SerializableFunction<Boolean> removalFunction;
	private int xOffset;
	private int yOffset;

	/**
	 * Constructor
	 */
	public TextBubble(String text, Vector2 position, SerializableFunction<Boolean> removalFunction, int xOffset, int yOffset) {
		this.removalFunction = removalFunction;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.setText(text);
		this.position = position;
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
		Vector2 screen = BloodAndMithrilClient.worldToScreen(position);

		int width = 300;
		TextBounds bounds = new TextBounds();
		bounds.width = width;
		TextBounds wrappedBounds = defaultFont.getWrappedBounds(text, width - 5);
		int height = (int) wrappedBounds.height + 10;

		if (wrappedBounds.width < 300) {
			width = (int) wrappedBounds.width + 10;
		}

		renderRectangle((int) screen.x + 2 + xOffset - width / 2, (int) screen.y + 2 + yOffset+ height / 2, width, height, isActive(), Color.BLACK);
		renderBox((int) screen.x + xOffset - width / 2, (int) screen.y + yOffset + height / 2, width, height, isActive(), Color.GRAY);

		spriteBatch.setShader(Shaders.text);
		defaultFont.setColor(Colors.modulateAlpha(Color.WHITE, getAlpha()));
		defaultFont.drawWrapped(
			spriteBatch,
			text,
			(int) screen.x + 5 + xOffset - width / 2,
			(int) screen.y - 5 + yOffset + height / 2,
			width - 5
		);
		spriteBatch.flush();
	}


	public String getText() {
		return text;
	}


	public void setText(String text) {
		this.text = text;
	}
}