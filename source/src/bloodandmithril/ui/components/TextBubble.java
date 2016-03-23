package bloodandmithril.ui.components;

import static bloodandmithril.control.InputUtilities.worldToScreen;
import static bloodandmithril.util.Fonts.defaultFont;

import java.io.Serializable;
import java.util.Deque;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util.Colors;

/**
 * Generic text bubble
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class TextBubble extends Component {
	private SerializableFunction<Vector2> position;
	private int xOffset;
	private int yOffset;
	private TextBubbleSerializableBean bean;

	/**
	 * Constructor
	 */
	public TextBubble(TextBubbleSerializableBean bean, SerializableFunction<Vector2> position, int xOffset, int yOffset) {
		this.bean = bean;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
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
	protected void internalComponentRender(Graphics graphics) {
		Vector2 screen = worldToScreen(position.call());

		int width = 300;
		TextBounds bounds = new TextBounds();
		bounds.width = width;
		TextBounds wrappedBounds = defaultFont.getWrappedBounds(bean.text, width - 5);
		int height = (int) wrappedBounds.height + 10;

		if (wrappedBounds.width < 300) {
			width = (int) wrappedBounds.width + 10;
		}

		renderRectangle((int) screen.x + 2 + xOffset - width / 2, (int) screen.y + 2 + yOffset+ height / 2, width, height, isActive(), Color.BLACK, graphics);
		renderBox((int) screen.x + xOffset - width / 2, (int) screen.y + yOffset + height / 2, width, height, isActive(), Color.GRAY, graphics);

		graphics.getSpriteBatch().setShader(Shaders.text);
		defaultFont.setColor(Colors.modulateAlpha(Color.WHITE, getAlpha()));
		defaultFont.drawWrapped(
			graphics.getSpriteBatch(),
			bean.text,
			(int) screen.x + 5 + xOffset - width / 2,
			(int) screen.y - 5 + yOffset + height / 2,
			width - 5
		);
		graphics.getSpriteBatch().flush();
	}


	public String getText() {
		return bean.text;
	}


	public TextBubbleSerializableBean getBean() {
		return bean;
	}


	public static class TextBubbleSerializableBean implements Serializable {
		private static final long serialVersionUID = 6096707551728218035L;

		public final SerializableFunction<Boolean> removalFunction;
		public final String text;

		public TextBubbleSerializableBean(String text, SerializableFunction<Boolean> removalFunction) {
			this.text = text;
			this.removalFunction = removalFunction;
		}
	}
}