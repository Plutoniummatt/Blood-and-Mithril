package bloodandmithril.ui.components.window;

import static bloodandmithril.core.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.WIDTH;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.util.Fonts.defaultFont;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.skill.Proficiencies;
import bloodandmithril.character.skill.Proficiency;
import bloodandmithril.core.Copyright;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;

import com.badlogic.gdx.graphics.Color;

/**
 * {@link Window} for displaying {@link Proficiencies} of an {@link Individual}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class ProficienciesWindow extends Window {

	private ScrollableListingPanel<Proficiency, Integer> skills;
	private int individualId;

	private static Comparator<Proficiency> sortingComparator = new Comparator<Proficiency>() {
		@Override
		public int compare(Proficiency o1, Proficiency o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};

	/**
	 * Constructor
	 */
	public ProficienciesWindow(Individual individual) {
		super(
			WIDTH / 2 - 150,
			HEIGHT / 2 + 200,
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

		skills = new ScrollableListingPanel<Proficiency, Integer>(this, sortingComparator, false, 80) {
			@Override
			protected String getExtraString(Entry<ListingMenuItem<Proficiency>, Integer> item) {
				return Integer.toString(item.getKey().t.getLevel());
			}

			@Override
			protected int getExtraStringOffset() {
				return 80;
			}

			@Override
			protected void populateListings(List<HashMap<ListingMenuItem<Proficiency>, Integer>> listings) {
				HashMap<ListingMenuItem<Proficiency>, Integer> newHashMap = newHashMap();

				for (Proficiency skill : individual.getSkills().getAllProficiencies()) {
					ContextMenu.MenuItem showInfo = new ContextMenu.MenuItem(
						"Show info",
						() -> {
							UserInterface.addLayeredComponentUnique(
								new MessageWindow(
									skill.getDescription(),
									Color.ORANGE,
									WIDTH / 2 - 250,
									HEIGHT / 2 + 150,
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
								skill.getName(),
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
							new ContextMenu(
								getMouseScreenX(),
								getMouseScreenY(),
								true ,
								showInfo
							)
						),
						0
					);
				}

				listings.add(newHashMap);
			}

			@Override
			public boolean keyPressed(int keyCode) {
				return false;
			}
		};

		this.individualId = individual.getId().getId();
	}


	@Override
	protected void internalWindowRender() {
		skills.x = x;
		skills.y = y;
		skills.width = width;
		skills.height = height;

		skills.render();
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
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
	public boolean scrolled(int amount) {
		return skills.scrolled(amount);
	}
}