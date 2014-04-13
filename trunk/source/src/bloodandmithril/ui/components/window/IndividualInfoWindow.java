package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.List;

import bloodandmithril.character.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;

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
		super(x, y, length, height, borderColor, backGroundColor, title, active, minLength, minHeight, true, true);
		individual = indivudual;
	}


	/**
	 * Constructor
	 */
	public IndividualInfoWindow(Individual indivudual, int x, int y, int length, int height, String title, boolean active, int minLength, int minHeight) {
		super(x, y, length, height, title, active, minLength, minHeight, true, true);
		individual = indivudual;
	}


	@Override
	protected void internalWindowRender() {
		Color activeTitle = Colors.modulateAlpha(Color.YELLOW, getAlpha());
		Color inactiveTitle = Colors.modulateAlpha(Color.ORANGE, getAlpha());
		Color activeWhite = Colors.modulateAlpha(Color.WHITE, getAlpha());
		Color inactiveWhite = Colors.modulateAlpha(Color.WHITE, 0.6f * getAlpha());

		defaultFont.setColor(isActive() ? activeTitle : inactiveTitle);
		if (!drawLine("Name: ", 25)) {
			return;
		}

		defaultFont.setColor(isActive() ? activeWhite : inactiveWhite);
		if (!drawLine(truncate(individual.getId().getSimpleName()), 45)) {
			return;
		}

		defaultFont.setColor(isActive() ? activeTitle : inactiveTitle);
		if (!drawLine("Nickname: ", 75)) {
			return;
		}

		defaultFont.setColor(isActive() ? activeWhite : inactiveWhite);
		if (!drawLine(truncate(individual.getId().getNickName()), 95)) {
			return;
		}

		defaultFont.setColor(isActive() ? activeTitle : inactiveTitle);
		if (!drawLine("Age: ", 125)) {
			return;
		}

		defaultFont.setColor(isActive() ? activeWhite : inactiveWhite);
		if (!drawLine(truncate(Integer.toString(individual.getAge())), 145)) {
			return;
		}

		defaultFont.setColor(isActive() ? activeTitle : inactiveTitle);
		if (!drawLine("Description: ", 175)) {
			return;
		}

		String messageToDisplay = Util.fitToWindow(individual.getDescription(), width, (height - 250) / 25);

		defaultFont.setColor(isActive() ? activeWhite : inactiveWhite);
		defaultFont.drawMultiLine(BloodAndMithrilClient.spriteBatch, messageToDisplay, x + 6, y - 195);
	}


	private boolean drawLine(String string, int yOff) {
		if (y - yOff < y - height + 60) {
			defaultFont.draw(BloodAndMithrilClient.spriteBatch, "...", x + 6, y - yOff);
			return false;
		} else {
			defaultFont.draw(BloodAndMithrilClient.spriteBatch, truncate(string), x + 6, y - yOff);
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


	@Override
	public boolean keyPressed(int keyCode) {
		return false;
	}


	@Override
	public Object getUniqueIdentifier() {
		return "IndiInfoWindow" + individual.getId().getId();
	}
}
