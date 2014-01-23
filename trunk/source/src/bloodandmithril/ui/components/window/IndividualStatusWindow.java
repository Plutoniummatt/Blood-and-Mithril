package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.Individual;
import bloodandmithril.character.Individual.Condition;
import bloodandmithril.character.skill.Skills;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Task;
import bloodandmithril.world.GameWorld;

import com.badlogic.gdx.graphics.Color;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Shows the status of an {@link Individual}
 *
 * @author Matt
 */
public class IndividualStatusWindow extends Window {

	private final Individual individual;
	private ScrollableListingPanel<Condition> conditionsPanel;
	private float time, identificationTime;
	private String vitals;
	private boolean identified, identifying;

	private final Button identify = new Button(
		"Identify",
		Fonts.defaultFont,
		0,
		0,
		80,
		16,
		new Task() {
			@Override
			public void execute() {
				identifying = true;
				identified = false;
			}
		},
		Color.GREEN,
		Color.WHITE,
		Color.GRAY,
		UIRef.BL
	);

	/** Constructor */
	public IndividualStatusWindow(final Individual individual, int x, int y, int length, int height, String title, boolean active) {
		super(x, y, length, height, title, active, 400, 400, true);
		this.individual = individual;
		this.conditionsPanel = new ScrollableListingPanel<Condition>(this) {
			@Override
			protected String getExtraString(Entry<ListingMenuItem<Condition>, Integer> item) {
				return "";
			}

			@Override
			protected int getExtraStringOffset() {
				return 0;
			}

			@Override
			protected void onSetup(List<HashMap<ListingMenuItem<Condition>, Integer>> listings) {
				refreshLisitng(individual, listings);
			}

			@Override
			public boolean keyPressed(int keyCode) {
				return false;
			}
		};
	}


	@Override
	protected void internalWindowRender() {
		if (identifying) {
			time = time + 1f/60f;
		}

		Color activeTitle = new Color(0.6f, 0f, 0.4f, 1f * alpha);
		Color inactiveTitle = new Color(0.45f, 0f, 0.32f, 0.6f * alpha);
		Color activeWhite = new Color(1f, 1f, 1f, 1f * alpha);
		Color inactiveWhite = new Color(1f, 1f, 1f, 0.6f * alpha);

		defaultFont.setColor(active ? activeTitle : inactiveTitle);
		if (!drawLine("Vital signs: ", 25)) {
			return;
		}

		defaultFont.setColor(active ? activeWhite : inactiveWhite);

		float percentageHealth = 100 * (individual.state.health/individual.state.maxHealth);
		int category = Math.round(percentageHealth)/10;

		if (percentageHealth == 0f) {
			vitals = "Dead";
		} else if (individual.isControllable()) {
			setVitalsString(category);
		} else {
			float highestObservationSkill = 0;
			if (!individual.isControllable()) {
				for (Individual indi : Sets.newHashSet(GameWorld.individuals.values())) {
					int skill = indi.getSkills().getObservation();
					if (skill > highestObservationSkill) {
						highestObservationSkill = skill;
					}
				}
			}

			identificationTime = (1 - highestObservationSkill/Skills.MAX_LEVEL) * 10f;

			if (time > identificationTime || individual.isControllable()) {
				identified = true;
				identifying = false;
				time = 0f;
				setVitalsString(category);
			} else {
				if (!identified) {
					vitals = "Unknown";
				}
			}
		}

		if (identifying) {
			vitals = "Identifying...";
		}

		if (!drawLine(truncate(vitals), 45)) {
			return;
		}

		defaultFont.setColor(active ? activeTitle : inactiveTitle);
		if (!drawLine("Conditions: ", 105)) {
			return;
		}

		BloodAndMithrilClient.spriteBatch.flush();
		renderConditionsPanel();
		identify.render(x + width - 50, y - 37, !GameWorld.selectedIndividuals.isEmpty() && active && !individual.isControllable(), alpha);
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
		refreshLisitng(individual, conditionsPanel.getListings());

		conditionsPanel.x = x;
		conditionsPanel.y = y - 100;
		conditionsPanel.width = width;
		conditionsPanel.height = height - 100;

		conditionsPanel.render();
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		conditionsPanel.leftClick(copy, windowsCopy);
		if (!GameWorld.selectedIndividuals.isEmpty() && !individual.isControllable()) {
			identify.click();
		}
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public boolean keyPressed(int keyCode) {
		return false;
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


	private void refreshLisitng(final Individual individual, List<HashMap<ListingMenuItem<Condition>, Integer>> listings) {
		listings.clear();
		HashMap<ListingMenuItem<Condition>, Integer> map = Maps.newHashMap();
		for (final Condition condition : Lists.newArrayList(individual.state.currentConditions)) {
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
						new Task() {
							@Override
							public void execute() {
								// Do nothing, as a context menu will open
							}
						},
						condition.isNegative() ? Color.RED : Color.GREEN,
						Color.WHITE,
						Color.GRAY,
						UIRef.BM
					),
					new ContextMenu(
						BloodAndMithrilClient.getMouseScreenX(),
						BloodAndMithrilClient.getMouseScreenY(),
						new ContextMenu.ContextMenuItem(
							"Info",
							new Task() {
								@Override
								public void execute() {
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
								}
							},
							new Color(0.8f, 0.8f, 0.8f, 1f),
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
}