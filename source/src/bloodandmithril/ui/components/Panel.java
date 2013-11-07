package bloodandmithril.ui.components;

import bloodandmithril.ui.components.window.Window;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/**
 * A {@link Panel} that lives on a {@link Window}
 *
 * @author Matt
 */
public abstract class Panel extends Component {

	/** The {@link Component} this {@link Panel} lives on */
	protected final Window parent;

	/** Dimensions of this {@link Panel} */
	public int width, height;

	/** Location of this {@link Panel} relative to the {@link #parent} {@link Window}*/
	public int parentOffsetX, parentOffsetY;

	/** Color of the panel */
	protected final Color color;

	/**
	 * Constructor
	 */
	public Panel(Window parent, Color color, int parentOffsetX, int parentOffsetY, int width, int height) {
		this.parent = parent;
		this.color = color;
		this.parentOffsetX = parentOffsetX;
		this.parentOffsetY = parentOffsetY;
		this.width = width;
		this.height = height;
	}


	/**
	 * @see bloodandmithril.ui.components.Component#internalComponentRender()
	 */
	@Override
	protected void internalComponentRender() {
		renderRectangle(
			parent.x + parentOffsetX + bottomLeft.getRegionWidth(),
			parent.y - parentOffsetY + bottomLeft.getRegionHeight(),
			width,
			height,
			active,
			color,
			parent.alpha
		);

		shapeRenderer.begin(ShapeType.Rectangle);
		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		shapeRenderer.setColor(color.r, color.g, color.b, parent.alpha);

		shapeRenderer.rect(
			parent.x + parentOffsetX + bottomLeft.getRegionWidth(),
			parent.y - parentOffsetY + bottomLeft.getRegionHeight() - height,
			width,
			height
		);

		shapeRenderer.end();
		internalPanelRender();
		Gdx.gl.glDisable(GL10.GL_BLEND);
	}


	/** Implementation-specific render method for {@link Panel}s */
	protected abstract void internalPanelRender();
}