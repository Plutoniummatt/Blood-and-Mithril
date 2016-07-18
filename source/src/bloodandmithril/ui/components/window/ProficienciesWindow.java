package bloodandmithril.ui.components.window;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.util.Fonts.defaultFont;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Color;
import com.google.inject.Inject;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.proficiency.Proficiencies;
import bloodandmithril.character.proficiency.Proficiency;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.world.Domain;

/**
 * {@link Window} for displaying {@link Proficiencies} of an {@link Individual}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class ProficienciesWindow extends Window {

	@Inject private UserInterface userInterface;

	private ScrollableListingPanel<Proficiency, Integer> skills;
	private int individualId;

	private static Comparator<Proficiency> sortingComparator = new Comparator<Proficiency>() {
		@Override
		public int compare(final Proficiency o1, final Proficiency o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};

	/**
	 * Constructor
	 */
	public ProficienciesWindow(final Individual individual) {
		super(
			300,
			500,
			individual.getId().getSimpleName() + " - Proficiencies",
			true,
			300,
			400,
			true,
			true,
			true
		);

		skills = new ScrollableListingPanel<Proficiency, Integer>(this, sortingComparator, false, 80, null) {
			@Override
			protected String getExtraString(final Entry<ListingMenuItem<Proficiency>, Integer> item) {
				return Integer.toString(item.getKey().t.getLevel());
			}

			@Override
			protected int getExtraStringOffset() {
				return 80;
			}

			@Override
			protected void populateListings(final List<HashMap<ListingMenuItem<Proficiency>, Integer>> listings) {
				final HashMap<ListingMenuItem<Proficiency>, Integer> newHashMap = buildMap(individual);

				listings.add(newHashMap);
			}

			@Override
			public boolean keyPressed(final int keyCode) {
				return false;
			}
		};

		this.individualId = individual.getId().getId();
	}


	private HashMap<ListingMenuItem<Proficiency>, Integer> buildMap(final Individual individual) {
		final HashMap<ListingMenuItem<Proficiency>, Integer> newHashMap = newHashMap();

		for (final Proficiency skill : individual.getProficiencies().getAllProficiencies()) {
			final ContextMenu.MenuItem showInfo = new ContextMenu.MenuItem(
				"Show info",
				() -> {
					userInterface.addLayeredComponentUnique(
						new MessageWindow(
							skill.getDescription(),
							Color.ORANGE,
							500,
							300,
							"Skill description - " + skill.getName(),
							true,
							500,
							300
						)
					);
				},
				Color.ORANGE,
				Color.WHITE,
				Color.ORANGE,
				null
			);

			newHashMap.put(
				new ListingMenuItem<Proficiency>(
					skill,
					new Button(
						() -> {return skill.getName() + " - " + String.format("%.2f", skill.getExperience());},
						defaultFont,
						0,
						0,
						skill.getName().length() * 10,
						16,
						() -> {
						},
						Color.WHITE.cpy().sub(new Color(0f, Proficiency.getRatioToMax(skill.getLevel()), 0f, 0f)),
						Color.GREEN,
						Color.WHITE,
						UIRef.BL
					),
					() -> { return new ContextMenu(
						getMouseScreenX(),
						getMouseScreenY(),
						true ,
						showInfo
					);}
				),
				0
			);
		}
		return newHashMap;
	}


	@Override
	protected void internalWindowRender(final Graphics graphics) {
		if (!Domain.getIndividual(individualId).isAlive()) {
			setClosing(true);
		}

		skills.x = x;
		skills.y = y;
		skills.width = width;
		skills.height = height;

		skills.render(graphics);
	}


	@Override
	protected void internalLeftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
		skills.leftClick(copy, windowsCopy);
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public Object getUniqueIdentifier() {
		return "SkillsWindow" + individualId;
	}


	@Override
	public void leftClickReleased() {
		skills.leftClickReleased();
	}


	@Override
	public boolean scrolled(final int amount) {
		return skills.scrolled(amount);
	}
}