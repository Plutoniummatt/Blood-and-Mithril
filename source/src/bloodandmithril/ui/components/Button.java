package bloodandmithril.ui.components;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.isButtonPressed;
import static bloodandmithril.core.BloodAndMithrilClient.getGraphics;
import static bloodandmithril.util.Util.fitToTextInputBox;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.util.Function;
import bloodandmithril.util.JITTask;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Task;

/**
 * Class representing a clickable button
 *
 * @author Matt, Sam
 */
@Copyright("Matthew Peck 2014")
public class Button {

	/** Texture regions used to draw this button */
	private TextureRegion idle, over, down;

	/** The colors of the button states */
	private Color idleColor, overColor, downColor;

	/** The text this button displays */
	public Function<String> text;

	/** The font of this button */
	private BitmapFont font;

	/** Position of this button, relative to the bottom left corner of the screen */
	private int offsetX, offsetY;

	/** Width and height of the button */
	public int width, height;

	/** Sound to be played when the button is clicked */
	private final Sound mouseClickSound;

	/** The task that this button will execute when pressed */
	private final JITTask jitTask;
	private Task task;

	/** The relative position this button is rendered from */
	private UIRef ref;

	/** THe popup that will be displayed from this button */
	private Function<InfoPopup> popup;

	/**
	 * Constructor for text button
	 */
	public Button(String text, BitmapFont font, int offsetX, int offsetY, int width, int height, Sound mouseClickedSound, Task task, Color idle, Color over, Color down, UIRef ref) {
		this.text = () -> {return text;};
		this.font = font;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.width = width;
		this.height = height;
		this.mouseClickSound = mouseClickedSound;
		this.task = task;
		this.jitTask = null;
		this.idleColor = idle;
		this.overColor = over;
		this.downColor = down;
		this.ref = ref;
	}


	/**
	 * Constructor for text button no sound
	 */
	public Button(String text, BitmapFont font, int offsetX, int offsetY, int width, int height, Task task, Color idle, Color over, Color down, UIRef ref) {
		this.text = () -> {return text;};
		this.font = font;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.width = width;
		this.height = height;
		this.task = task;
		this.jitTask = null;
		this.idleColor = idle;
		this.overColor = over;
		this.downColor = down;
		this.ref = ref;
		this.mouseClickSound = null;
	}


	/**
	 * Constructor for text button no sound
	 */
	public Button(Function<String> text, BitmapFont font, int offsetX, int offsetY, int width, int height, Task task, Color idle, Color over, Color down, UIRef ref) {
		this.text = text;
		this.font = font;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.width = width;
		this.height = height;
		this.task = task;
		this.jitTask = null;
		this.idleColor = idle;
		this.overColor = over;
		this.downColor = down;
		this.ref = ref;
		this.mouseClickSound = null;
	}


	/**
	 * Constructor for text button no sound - with a {@link JITTask}
	 */
	public Button(String text, BitmapFont font, int offsetX, int offsetY, int width, int height, JITTask task, Color idle, Color over, Color down, UIRef ref) {
	  this.text = () -> {return text;};
	  this.font = font;
	  this.offsetX = offsetX;
	  this.offsetY = offsetY;
	  this.width = width;
	  this.height = height;
	  this.task = null;
	  this.jitTask = task;
	  this.idleColor = idle;
	  this.overColor = over;
	  this.downColor = down;
	  this.ref = ref;
	  this.mouseClickSound = null;
	}


	/**
	 * Constructor
	 */
	public Button(Texture buttonAtlas, int offsetX, int offsetY, int atlasX, int atlasY, int width, int height, Sound mouseClickedSound, Task task, UIRef ref) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.width = width;
		this.height = height;
		this.mouseClickSound = mouseClickedSound;
		this.task = task;
		this.jitTask = null;
		this.ref = ref;
		idle = new TextureRegion(buttonAtlas, atlasX, atlasY, width, height);
		over = new TextureRegion(buttonAtlas, atlasX, atlasY + height, width, height);
		down = new TextureRegion(buttonAtlas, atlasX, atlasY + 2 * height, width, height);
	}


	/**
	 * Overloaded constructor, no sound required
	 */
	public Button(Texture buttonAtlas, int offsetX, int offsetY, int atlasX, int atlasY, int width, int height, Task task, UIRef ref) {
		this(buttonAtlas, offsetX, offsetY, atlasX, atlasY, width, height, null, task, ref);
	}


	public Button mouseOverPopup(Function<InfoPopup> popup) {
		this.popup = popup;
		return this;
	}


	/**
	 * Renders this button
	 */
	public void render(boolean active, float alpha, int maxWidth) {
		if (popup != null) {
			InfoPopup p = popup.call();
			if (!p.expiryFunction.call() && UserInterface.getInfoPopup() == null) {
				UserInterface.setInfoPopup(popup.call());
			}
		}

		Vector2 vec = new Vector2();
		morph(vec);

		if (idle == null) {

			getGraphics().getSpriteBatch().setShader(Shaders.text);
			Color downColorToUse = active ? downColor : idle == null ? downColor.cpy() : downColor;
			Color overColorToUse = active ? overColor : idle == null ? overColor.cpy() : overColor;
			Color idleColorToUse = active ? idleColor : idle == null ? idleColor.cpy() : idleColor;

			if (isMouseOver() && active) {
				if (isButtonPressed(Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).getKeyMappings().leftClick.keyCode)) {
					font.setColor(downColorToUse.r, downColorToUse.g, downColorToUse.b, alpha * (active ? 1f : 0.3f));
					font.draw(getGraphics().getSpriteBatch(), maxWidth == 0 ? text.call() : fitToTextInputBox(text.call(), maxWidth, 0, false), vec.x, vec.y);
				} else {
					font.setColor(overColorToUse.r, overColorToUse.g, overColorToUse.b, alpha * (active ? 1f : 0.3f));
					font.draw(getGraphics().getSpriteBatch(),  maxWidth == 0 ? text.call() : fitToTextInputBox(text.call(), maxWidth, 0, false), vec.x, vec.y);
				}
			} else {
				font.setColor(idleColorToUse.r, idleColorToUse.g, idleColorToUse.b, alpha * (active ? 1f : 0.3f));
				font.draw(getGraphics().getSpriteBatch(),  maxWidth == 0 ? text.call() : fitToTextInputBox(text.call(), maxWidth, 0, false), vec.x, vec.y);
			}
		} else {
			getGraphics().getSpriteBatch().setShader(Shaders.filter);
			Shaders.filter.setUniformf("color", 1f, 1f, 1f, alpha);
			if (isMouseOver() && active) {
				if (isButtonPressed(Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).getKeyMappings().leftClick.keyCode)) {
					getGraphics().getSpriteBatch().draw(down, vec.x, vec.y);
				} else {
					getGraphics().getSpriteBatch().draw(over, vec.x, vec.y);
				}
			} else {
				getGraphics().getSpriteBatch().draw(idle, vec.x, vec.y);
			}
		}

		getGraphics().getSpriteBatch().flush();
	}


	public void render(boolean active, float alpha) {
		render(active, alpha, 0);
	}


	/** Gets the text content of this button */
	public String getText() {
		return text.call();
	}


	/**
	 * Renders this button at an override location
	 */
	public void render(int x, int y) {
		offsetX = x;
		offsetY = y;
		ref = UIRef.BL;

		render(true, 1f);
	}


	/**
	 * Renders this button at an override location and whether or not this button is 'active'
	 */
	public void render(int x, int y, boolean active) {
		offsetX = x;
		offsetY = y;
		ref = UIRef.BL;

		render(active, 1f);
	}


	/**
	 * Renders this button at an override location and whether or not this button is 'active', plus an alpha value
	 */
	public void render(int x, int y, boolean active, float alpha) {
		offsetX = x;
		offsetY = y;
		ref = UIRef.BL;

		render(active, alpha);
	}


	/**
	 * Renders this button at an override location and whether or not this button is 'active', plus an alpha value, and maximum width, truncating the rest
	 */
	public void render(int x, int y, boolean active, float alpha, int maxWidth) {
		offsetX = x;
		offsetY = y;
		ref = UIRef.BL;

		render(active, alpha, maxWidth);
	}


	/**
	 * Morphs the render position of a button.
	 */
	private void morph(Vector2 vec) {
		switch (ref) {
		case BL:
			vec.x = offsetX - width/2;
			vec.y = offsetY - height/2;
			break;

		case BM:
			vec.x = getGraphics().getWidth()/2 + offsetX - width/2;
			vec.y = offsetY - height/2;
			break;

		case BR:
			vec.x = getGraphics().getWidth() + offsetX - width/2;
			vec.y = offsetY - height/2;
			break;

		case M:
			vec.x = getGraphics().getWidth()/2 + offsetX - width/2;
			vec.y = getGraphics().getHeight()/2 + offsetY - height/2;
			break;

		case TL:
			vec.x = offsetX - width/2;
			vec.y = getGraphics().getHeight() + offsetY - height/2;
			break;

		case TM:
			vec.x = getGraphics().getWidth()/2 + offsetX - width/2;
			vec.y = getGraphics().getHeight() + offsetY - height/2;
			break;

		case TR:
			vec.x = getGraphics().getWidth() + offsetX - width/2;
			vec.y = getGraphics().getHeight() + offsetY - height/2;
			break;

		default:
			throw new RuntimeException("Button reference not recognised");
		}
	}


	/**
	 * Called when this button is clicked
	 */
	public boolean click() {
		if (isMouseOver()) {
		  if (mouseClickSound != null) {
		    mouseClickSound.play();
		  }
		  if (task != null) {
		    task.execute();
		  }
		  return true;
		}
		return false;
	}


	/**
	 * Called when this button is clicked
	 */
	public boolean click(Object... args) {
	  if (isMouseOver()) {
	    if (mouseClickSound != null) {
	      mouseClickSound.play();
	    }
	    if (jitTask != null) {
	      jitTask.execute(args);
	    }
	    return true;
	  }
	  return false;
	}


	/**
	 * @return true if the mouse is over the button.
	 */
	public boolean isMouseOver() {

		Vector2 vec = new Vector2();
		morph(vec);

		int mouseX = getMouseScreenX();
		int mouseY = getMouseScreenY();

		if (idle == null) {
			return mouseX >= vec.x && mouseX <= vec.x + width && mouseY <= vec.y && mouseY >= vec.y - height;
		} else {
			return mouseX >= vec.x && mouseX <= vec.x + width && mouseY <= vec.y + height && mouseY >= vec.y;
		}
	}


	public Color getIdle() {
		return idleColor;
	}


	public Color getOverColor() {
		return overColor;
	}


	public Color getDownColor() {
		return downColor;
	}


	public void setOverColor(Color overColor) {
		this.overColor = overColor;
	}


	public void setDownColor(Color downColor) {
		this.downColor = downColor;
	}


	public void setIdleColor(Color color) {
		this.idleColor = color;
	}


	public void setTask(Task task) {
		this.task = task;
	}


	public Task getTask() {
		return task;
	}


	public InfoPopup getPopup() {
		return popup.call();
	}
}
