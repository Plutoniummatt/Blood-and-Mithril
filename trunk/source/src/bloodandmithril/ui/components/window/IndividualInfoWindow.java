package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.List;

import bloodandmithril.Fortress;
import bloodandmithril.character.Individual;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.Util;

import com.badlogic.gdx.graphics.Color;

/**
 * {@link Window} used to display the info of an {@link Individual}
 *
 * @author Matt
 */
public class IndividualInfoWindow extends Window {

	/** The {@link Individual} this window displays info for */
	private final Individual individual;

	/**
	 * Constructor
	 */
	@Deprecated
	public IndividualInfoWindow(Individual indivudual, int x, int y, int length, int height, Color borderColor, Color backGroundColor, String title, boolean active, int minLength, int minHeight) {
		super(x, y, length, height, borderColor, backGroundColor, title, active, minLength, minHeight, true);
		individual = indivudual;
	}


	/**
	 * Constructor
	 */
	public IndividualInfoWindow(Individual indivudual, int x, int y, int length, int height, String title, boolean active, int minLength, int minHeight) {
		super(x, y, length, height, title, active, minLength, minHeight, true);
		individual = indivudual;
	}


	@Override
	protected void internalWindowRender() {
		Color activeGreen = new Color(0.6f, 0f, 0.4f, 1f * alpha);
		Color inactiveGreen = new Color(0.45f, 0f, 0.32f, 0.6f * alpha);
		Color activeWhite = new Color(1f, 1f, 1f, 1f * alpha);
		Color inactiveWhite = new Color(1f, 1f, 1f, 0.6f * alpha);

		defaultFont.setColor(active ? activeGreen : inactiveGreen);
		if (!drawLine("Name: ", 25)) {
			return;
		}

		defaultFont.setColor(active ? activeWhite : inactiveWhite);
		if (!drawLine(truncate(individual.id.getSimpleName()), 45)) {
			return;
		}

		defaultFont.setColor(active ? activeGreen : inactiveGreen);
		if (!drawLine("Nickname: ", 75)) {
			return;
		}

		defaultFont.setColor(active ? activeWhite : inactiveWhite);
		if (!drawLine(truncate(individual.id.nickName), 95)) {
			return;
		}

		defaultFont.setColor(active ? activeGreen : inactiveGreen);
		if (!drawLine("Age: ", 125)) {
			return;
		}

		defaultFont.setColor(active ? activeWhite : inactiveWhite);
		if (!drawLine(truncate(Integer.toString(individual.getAge())), 145)) {
			return;
		}

		defaultFont.setColor(active ? activeGreen : inactiveGreen);
		if (!drawLine("Description: ", 175)) {
			return;
		}

		String messageToDisplay = Util.fitToWindow(individual.getDescription(), length, (height - 250) / 25);

		defaultFont.setColor(active ? activeWhite : inactiveWhite);
		defaultFont.drawMultiLine(Fortress.spriteBatch, messageToDisplay, x + 6, y - 195);
	}


	private boolean drawLine(String string, int yOff) {
		if (y - yOff < y - height + 60) {
			defaultFont.draw(Fortress.spriteBatch, "...", x + 6, y - yOff);
			return false;
		} else {
			defaultFont.draw(Fortress.spriteBatch, truncate(string), x + 6, y - yOff);
			return true;
		}
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
	}


	@Override
	public void leftClickReleased() {
	}


	@Override
	protected void uponClose() {
	}
}
