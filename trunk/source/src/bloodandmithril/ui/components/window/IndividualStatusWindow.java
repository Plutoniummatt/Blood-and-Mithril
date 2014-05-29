package bloodandmithril.ui.components.window;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.util.Fonts.defaultFont;
import static bloodandmithril.util.Util.Colors.modulateAlpha;
import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.FilledRectangle;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import bloodandmithril.character.conditions.Condition;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util.Colors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Shows the status of an {@link Individual}
 *
 * @author Matt
 */
public class IndividualStatusWindow extends Window {

	private final Individual individual;

	private ScrollableListingPanel<Condition, Object> conditionsPanel;
	private String vitals;
	public static TextureRegion icons = new TextureRegion(UserInterface.uiTexture, 253, 0, 12, 85);;

	private static Comparator<Condition> sortingOrder = new Comparator<Condition>() {
		@Override
		public int compare(Condition o1, Condition o2) {
			return o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
		}
	};

	/** Constructor */
	public IndividualStatusWindow(final Individual individual, int x, int y, int length, int height, String title, boolean active) {
		super(x, y, length, height, title, active, 400, 400, true, true);
		this.individual = individual;
		this.conditionsPanel = new ScrollableListingPanel<Condition, Object>(this, sortingOrder) {
			@Override
			protected String getExtraString(Entry<ListingMenuItem<Condition>, Object> item) {
				return "";
			}

			@Override
			protected int getExtraStringOffset() {
				return 0;
			}

			@Override
			protected void onSetup(List<HashMap<ListingMenuItem<Condition>, Object>> listings) {
				refreshLisitng(individual, listings);
			}

			@Override
			public boolean keyPressed(int keyCode) {
				return false;
			}
		};

		conditionsPanel.setScrollWheelActive(true);
	}


	@Override
	protected void internalWindowRender() {
		Color activeTitle = Colors.modulateAlpha(Color.GREEN, getAlpha());
		Color inactiveTitle = Colors.modulateAlpha(Color.GREEN, getAlpha());
		Color activeWhite = Colors.modulateAlpha(Color.WHITE, getAlpha());
		Color inactiveWhite = Colors.modulateAlpha(Color.WHITE, 0.6f * getAlpha());

		defaultFont.setColor(isActive() ? activeTitle : inactiveTitle);
		if (!drawLine("Vital signs: ", 25)) {
			return;
		}

		defaultFont.setColor(isActive() ? activeWhite : inactiveWhite);

		float percentageHealth = 100 * (individual.getState().health/individual.getState().maxHealth);
		int category = Math.round(percentageHealth)/10;

		if (percentageHealth == 0f) {
			vitals = "Dead";
		} else {
			setVitalsString(category);
		}

		if (!drawLine(truncate(vitals), 45)) {
			return;
		}

		defaultFont.setColor(isActive() ? activeTitle : inactiveTitle);
		if (!drawLine("Conditions: ", 175)) {
			return;
		}

		spriteBatch.setShader(Shaders.filter);
		Shaders.filter.setUniformf("color", 1f, 1f, 1f, getAlpha() * (isActive() ? 1.0f : 0.7f));
		spriteBatch.draw(icons, x + 20, y - 162);

		defaultFont.setColor(isActive() ? activeWhite : inactiveWhite);
		BloodAndMithrilClient.spriteBatch.flush();
		renderConditionsPanel();

		renderBars();
	}


	/**
	 * Renders health, hunger, thirst, stamina bars etc
	 */
	private void renderBars() {

		int barThickness = 7;
		int barOffsetY = 85;
		int barOffsetX = 40;
		int barSeparation = 15;
		int barLengthModifier = 80;

		shapeRenderer.begin(FilledRectangle);
		float health = individual.getState().health / individual.getState().maxHealth;
		shapeRenderer.filledRect(x + barOffsetX, y - barOffsetY, (width - barLengthModifier) * health, barThickness,
			modulateAlpha(Color.WHITE, getAlpha() * (isActive() ? 1.0f : 0.7f)),
			modulateAlpha(Color.WHITE, getAlpha() * (isActive() ? 1.0f : 0.7f)),
			modulateAlpha(Color.WHITE, getAlpha() * (isActive() ? 1.0f : 0.7f)),
			modulateAlpha(Color.WHITE, getAlpha() * (isActive() ? 1.0f : 0.7f))
		);

		float stamina = individual.getState().stamina;
		shapeRenderer.filledRect(x + barOffsetX, y - barOffsetY - 1 * barSeparation, (width - barLengthModifier) * stamina, barThickness,
			modulateAlpha(Color.WHITE, getAlpha() * (isActive() ? 1.0f : 0.7f)),
			modulateAlpha(Color.WHITE, getAlpha() * (isActive() ? 1.0f : 0.7f)),
			modulateAlpha(Color.WHITE, getAlpha() * (isActive() ? 1.0f : 0.7f)),
			modulateAlpha(Color.WHITE, getAlpha() * (isActive() ? 1.0f : 0.7f))
		);

		float mana = individual.getState().maxMana == 0f ? 0f : individual.getState().mana / individual.getState().maxMana;
		shapeRenderer.filledRect(x + barOffsetX, y - barOffsetY - 2 * barSeparation, (width - barLengthModifier) * mana, barThickness,
			modulateAlpha(Color.WHITE, getAlpha() * (isActive() ? 1.0f : 0.7f)),
			modulateAlpha(Color.WHITE, getAlpha() * (isActive() ? 1.0f : 0.7f)),
			modulateAlpha(Color.WHITE, getAlpha() * (isActive() ? 1.0f : 0.7f)),
			modulateAlpha(Color.WHITE, getAlpha() * (isActive() ? 1.0f : 0.7f))
		);

		float hunger = individual.getState().hunger;
		shapeRenderer.filledRect(x + barOffsetX, y - barOffsetY - 4 * barSeparation, (width - barLengthModifier) * hunger, barThickness,
			modulateAlpha(Color.WHITE, getAlpha() * (isActive() ? 1.0f : 0.7f)),
			modulateAlpha(Color.WHITE, getAlpha() * (isActive() ? 1.0f : 0.7f)),
			modulateAlpha(Color.WHITE, getAlpha() * (isActive() ? 1.0f : 0.7f)),
			modulateAlpha(Color.WHITE, getAlpha() * (isActive() ? 1.0f : 0.7f))
		);

		float thirst = individual.getState().thirst;
		shapeRenderer.filledRect(x + barOffsetX, y - barOffsetY - 5 * barSeparation, (width - barLengthModifier) * thirst, barThickness,
			modulateAlpha(Color.WHITE, getAlpha() * (isActive() ? 1.0f : 0.7f)),
			modulateAlpha(Color.WHITE, getAlpha() * (isActive() ? 1.0f : 0.7f)),
			modulateAlpha(Color.WHITE, getAlpha() * (isActive() ? 1.0f : 0.7f)),
			modulateAlpha(Color.WHITE, getAlpha() * (isActive() ? 1.0f : 0.7f))
		);
		shapeRenderer.end();

		shapeRenderer.begin(ShapeType.Rectangle);
		shapeRenderer.setColor(modulateAlpha(Color.WHITE, getAlpha() * (isActive() ? 1.0f : 0.7f)));
		shapeRenderer.rect(x + barOffsetX, y - barOffsetY, width - barLengthModifier, barThickness);
		shapeRenderer.rect(x + barOffsetX, y - barOffsetY - 1 * barSeparation, width - barLengthModifier, barThickness);
		shapeRenderer.rect(x + barOffsetX, y - barOffsetY - 2 * barSeparation, width - barLengthModifier, barThickness);
		shapeRenderer.rect(x + barOffsetX, y - barOffsetY - 4 * barSeparation, width - barLengthModifier, barThickness);
		shapeRenderer.rect(x + barOffsetX, y - barOffsetY - 5 * barSeparation, width - barLengthModifier, barThickness);
		shapeRenderer.end();
	}


	private void setVitalsString(int category) {
		switch(category) {
			case 0:		vitals = "Near death"; break;
			case 1:		vitals = "Near death"; break;
			case 2:		vitals = "Extremely weak"; break;
			case 3:		vitals = "Very Weak"; break;
			case 4:		vitals = "Weak"; break;
			case 5:		vitals = "Not so good"; break;
			case 6:		vitals = "Adequate"; break;
			case 7:		vitals = "Normal"; break;
			case 8:		vitals = "Strong"; break;
			case 9:		vitals = "Very Strong"; break;
			case 10:	vitals = "Extremely strong"; break;
			default : throw new RuntimeException("Health percentage is not correct");
		}
	}


	private void renderConditionsPanel() {
		refreshLisitng(individual, conditionsPanel.getListing());

		conditionsPanel.x = x;
		conditionsPanel.y = y - 160;
		conditionsPanel.width = width;
		conditionsPanel.height = height - 160;

		conditionsPanel.render();
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		conditionsPanel.leftClick(copy, windowsCopy);
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public boolean scrolled(int amount) {
		return conditionsPanel.scrolled(amount);
	}


	@Override
	public void leftClickReleased() {
		conditionsPanel.leftClickReleased();
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


	private void refreshLisitng(final Individual individual, List<HashMap<ListingMenuItem<Condition>, Object>> listings) {
		listings.clear();
		HashMap<ListingMenuItem<Condition>, Object> map = Maps.newHashMap();
		for (final Condition condition : Lists.newArrayList(individual.getState().currentConditions)) {
			map.put(
				new ListingMenuItem<Condition>(
					condition,
					new Button(
						condition.getName(),
						Fonts.defaultFont,
						0,
						0,
						condition.getName().length() * 9,
						16,
						() -> {},
						condition.isNegative() ? Color.RED : Color.GREEN,
						Color.WHITE,
						Color.GRAY,
						UIRef.BM
					),
					new ContextMenu(
						BloodAndMithrilClient.getMouseScreenX(),
						BloodAndMithrilClient.getMouseScreenY(),
						new ContextMenu.MenuItem(
							"Info",
							() -> {
								UserInterface.addLayeredComponent(
									new MessageWindow(
										condition.getHelpText(),
										Color.YELLOW,
										BloodAndMithrilClient.getMouseScreenX(),
										BloodAndMithrilClient.getMouseScreenY(),
										350,
										200,
										condition.getName(),
										true,
										350,
										200
									)
								);
							},
							Color.WHITE,
							Color.GREEN,
							Color.WHITE,
							null
						)
					)
				),
				0
			);
		}
		listings.add(map);
	}


	@Override
	public Object getUniqueIdentifier() {
		return "statusWindow" + individual.getId().getId();
	}
}