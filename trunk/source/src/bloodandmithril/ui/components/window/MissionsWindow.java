package bloodandmithril.ui.components.window;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.objectives.Mission;
import bloodandmithril.objectives.Objective.ObjectiveStatus;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;

/**
 * The window displaying missions etc.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class MissionsWindow extends Window {

	private ScrollableListingPanel<Mission, String> activeMissions;
	private ScrollableListingPanel<Mission, String> completedMissions;

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
		super(400, 500, "Journal", true, true, true, true);

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
				return 100;
			}

			@Override
			protected void populateListings(List<HashMap<ListingMenuItem<Mission>, String>> listings) {
				for (Mission mission : BloodAndMithrilClient.missions) {
					if (mission.getStatus() == ObjectiveStatus.ACTIVE) {

					}
				}
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
				for (Mission mission : BloodAndMithrilClient.missions) {
					if (mission.getStatus() != ObjectiveStatus.ACTIVE) {

					}
				}
			}

			@Override
			public boolean keyPressed(int keyCode) {
				return false;
			}
		};
	}


	@Override
	protected void internalWindowRender() {
		// TODO Auto-generated method stub
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		// TODO Auto-generated method stub
	}


	@Override
	protected void uponClose() {
		// TODO Auto-generated method stub
	}


	@Override
	public Object getUniqueIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void leftClickReleased() {
		// TODO Auto-generated method stub
	}
}