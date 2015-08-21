package bloodandmithril.ui.components.window;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.objectives.Mission;
import bloodandmithril.objectives.Objective;
import bloodandmithril.objectives.Objective.ObjectiveStatus;
import bloodandmithril.ui.Refreshable;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Function;

import com.badlogic.gdx.graphics.Color;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * The window displaying missions etc.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class MissionsWindow extends Window implements Refreshable {

	private ScrollableListingPanel<Mission, String> activeMissions;
	private ScrollableListingPanel<Mission, String> completedMissions;
	private boolean renderActive = true;

	private static Comparator<Mission> alphabeticalSortingComparator = new Comparator<Mission>() {
		@Override
		public int compare(Mission o1, Mission o2) {
			return o1.getTitle().compareTo(o2.getTitle());
		}
	};

	/**
	 * Constructor
	 */
	public MissionsWindow() {
		super(800, 500, "Journal", true, true, true, true);

		activeMissions = new ScrollableListingPanel<Mission, String>(this, alphabeticalSortingComparator, false, 100) {
			@Override
			protected String getExtraString(Entry<ListingMenuItem<Mission>, String> item) {
				switch (item.getKey().t.getStatus()) {
					case ACTIVE:
						return "Active";
					case COMPLETE:
						return "Complete";
					case FAILED:
						return "Failed";
					default:
						return "";
				}
			}

			@Override
			protected int getExtraStringOffset() {
				return 115;
			}

			@Override
			protected void populateListings(List<HashMap<ListingMenuItem<Mission>, String>> listings) {
				listings.add(buildMap(ObjectiveStatus.ACTIVE));
			}

			@Override
			public boolean keyPressed(int keyCode) {
				return false;
			}
		};

		completedMissions = new ScrollableListingPanel<Mission, String>(this, alphabeticalSortingComparator, false, 0) {
			@Override
			protected String getExtraString(Entry<ListingMenuItem<Mission>, String> item) {
				switch (item.getKey().t.getStatus()) {
					case ACTIVE:
						return "Active";
					case COMPLETE:
						return "Complete";
					case FAILED:
						return "Failed";
					default:
						return "";
				}
			}

			@Override
			protected int getExtraStringOffset() {
				return 100;
			}

			@Override
			protected void populateListings(List<HashMap<ListingMenuItem<Mission>, String>> listings) {
				listings.add(buildMap(ObjectiveStatus.COMPLETE, ObjectiveStatus.FAILED));
			}

			@Override
			public boolean keyPressed(int keyCode) {
				return false;
			}
		};
	}


	@Override
	protected void internalWindowRender() {
		activeMissions.width = width;
		activeMissions.height = height;
		activeMissions.x = x;
		activeMissions.y = y;

		completedMissions.width = width;
		completedMissions.height = height;
		completedMissions.x = x;
		completedMissions.y = y;

		if (renderActive) {
			activeMissions.render();
		} else {
			completedMissions.render();
		}
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		if (renderActive) {
			activeMissions.leftClick(copy, windowsCopy);
		} else {
			completedMissions.leftClick(copy, windowsCopy);
		}
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public Object getUniqueIdentifier() {
		return "MissionsWindow";
	}


	@Override
	public void leftClickReleased() {
	}


	@Override
	public void refresh() {
		List<HashMap<ListingMenuItem<Mission>, String>> activeListing = Lists.newLinkedList();
		activeListing.add(buildMap(ObjectiveStatus.ACTIVE));
		activeMissions.refresh(activeListing);

		List<HashMap<ListingMenuItem<Mission>, String>> completeListing = Lists.newLinkedList();
		activeListing.add(buildMap(ObjectiveStatus.COMPLETE, ObjectiveStatus.FAILED));
		completedMissions.refresh(completeListing);
	}


	private HashMap<ListingMenuItem<Mission>, String> buildMap(ObjectiveStatus... statuses) {
		HashMap<ListingMenuItem<Mission>, String> map = Maps.newHashMap();
		for (Mission mission : BloodAndMithrilClient.getMissions()) {
			if (Sets.newHashSet(statuses).contains(mission.getStatus())) {
				final Function<String> objectivesFunction = () -> {
					String objectives = "";
					for (Objective obj : mission.getObjectives()) {
						objectives = objectives + obj.getTitle() + " (" + obj.getStatus().getDescription() + ") \n";
					}

					return objectives;
				};

				map.put(
					new ListingMenuItem<Mission>(
						mission,
						new Button(
							mission.getTitle(),
							Fonts.defaultFont,
							0,
							0,
							mission.getTitle().length() * 10,
							16,
							() -> {
								UserInterface.addClientMessage(mission.getTitle(), mission.getDescription() + "\n\n" + objectivesFunction.call());
							},
							Color.ORANGE,
							Color.WHITE,
							Color.GREEN,
							UIRef.BL
						),
						null
					),
					mission.getStatus().getDescription()
				);
			}
		}

		return map;
	}
}