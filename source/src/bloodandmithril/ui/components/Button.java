package bloodandmithril.ui.components;


import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.util.JITTask;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Task;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * Class representing a clickable button
 *
 * @author Matt, Sam
 */
public class Button {

	/** Texture regions used to draw this button */
	private TextureRegion idle, over, down;

	/** The colors of the button states */
	private Color idleColor, overColor, downColor;

	/** The text this button displays */
	public String text;

	/** The font of this button */
	private BitmapFont font;

	/** Position of this button, relative to the bottom left corner of the screen */
	private int offsetX, offsetY;

	/** Width and height of the button */
	public int width, height;

	/** Sound to be played when the button is clicked */
	private final Sound mouseClickSound;

	/** The task that this button will execute when pressed */
	private final Task task;
	private final JITTask jitTask;

	/** The relative position this button is rendered from */
	private UIRef ref;


	/**
	 * Constructor for text button
	 */
	public Button(String text, BitmapFont font, int offsetX, int offsetY, int width, int height, Sound mouseClickedSound, Task task, Color idle, Color over, Color down, UIRef ref) {
		this.text = text;
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
	 * Constructor for text button no sound
	 */
	public Button(String text, BitmapFont font, int offsetX, int offsetY, int width, int height, JITTask task, Color idle, Color over, Color down, UIRef ref) {
	  this.text = text;
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


	public void setIdleColor(Color color) {
		this.idleColor = color;
	}


	/**
	 * Renders this button
	 */
	public void render(boolean active, float alpha) {

		Vector2 vec = new Vector2();
		morph(vec);

		if (idle == null) {
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.text);
			Color downColorToUse = active ? downColor : idle == null ? downColor.cpy() : downColor;
			Color overColorToUse = active ? overColor : idle == null ? overColor.cpy() : overColor;
			Color idleColorToUse = active ? idleColor : idle == null ? idleColor.cpy() : idleColor;

			if (isMouseOver() && active) {
				if (Gdx.input.isButtonPressed(KeyMappings.leftClick)) {
					font.setColor(downColorToUse.r, downColorToUse.g, downColorToUse.b, alpha * (active ? 1f : 0.3f));
					font.draw(BloodAndMithrilClient.spriteBatch, text, vec.x, vec.y);
				} else {
					font.setColor(overColorToUse.r, overColorToUse.g, overColorToUse.b, alpha * (active ? 1f : 0.3f));
					font.draw(BloodAndMithrilClient.spriteBatch, text, vec.x, vec.y);
				}
			} else {
				font.setColor(idleColorToUse.r, idleColorToUse.g, idleColorToUse.b, alpha * (active ? 1f : 0.3f));
				font.draw(BloodAndMithrilClient.spriteBatch, text, vec.x, vec.y);
			}
		} else {
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.filter);
			Shaders.filter.setUniformf("color", 1f, 1f, 1f, alpha);
			if (isMouseOver() && active) {
				if (Gdx.input.isButtonPressed(KeyMappings.leftClick)) {
					BloodAndMithrilClient.spriteBatch.draw(down, vec.x, vec.y);
				} else {
					BloodAndMithrilClient.spriteBatch.draw(over, vec.x, vec.y);
				}
			} else {
				BloodAndMithrilClient.spriteBatch.draw(idle, vec.x, vec.y);
			}
		}
	}


	/** Gets the text content of this button */
	public String getText() {
		return text;
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
	 * Renders this button at an override location and whether or not this button is 'active', plus al alpha value
	 */
	public void render(int x, int y, boolean active, float alpha) {
		offsetX = x;
		offsetY = y;
		ref = UIRef.BL;

		render(active, alpha);
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
			vec.x = Gdx.graphics.getWidth()/2 + offsetX - width/2;
			vec.y = offsetY - height/2;
			break;

		case BR:
			vec.x = Gdx.graphics.getWidth() + offsetX - width/2;
			vec.y = offsetY - height/2;
			break;

		case M:
			vec.x = Gdx.graphics.getWidth()/2 + offsetX - width/2;
			vec.y = Gdx.graphics.getHeight()/2 + offsetY - height/2;
			break;

		case TL:
			vec.x = offsetX - width/2;
			vec.y = Gdx.graphics.getHeight() + offsetY - height/2;
			break;

		case TM:
			vec.x = Gdx.graphics.getWidth()/2 + offsetX - width/2;
			vec.y = Gdx.graphics.getHeight() + offsetY - height/2;
			break;

		case TR:
			vec.x = Gdx.graphics.getWidth() + offsetX - width/2;
			vec.y = Gdx.graphics.getHeight() + offsetY - height/2;
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

		int mouseX = BloodAndMithrilClient.getMouseScreenX();
		int mouseY = BloodAndMithrilClient.getMouseScreenY();

		if (idle == null) {
			return mouseX >= vec.x && mouseX <= vec.x + width && mouseY <= vec.y && mouseY >= vec.y - height;
		} else {
			return mouseX >= vec.x && mouseX <= vec.x + width && mouseY <= vec.y + height && mouseY >= vec.y;
		}
	}
}
