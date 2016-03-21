package bloodandmithril.ui.components.window;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;

import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.graphics.Color;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import bloodandmithril.character.faction.Faction;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Fonts;
import bloodandmithril.world.Domain;

/**
 * Window that displays factions
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class FactionsWindow extends Window {

	private final ScrollableListingPanel<String, Object> factionsPanel;

	/**
	 * Constructor
	 */
	public FactionsWindow(int length, int height, boolean active, int minLength, int minHeight) {
		super(length, height, "Factions", active, minLength, minHeight, true, false, true);

		factionsPanel = new ScrollableListingPanel<String, Object>(this, Comparator.<String>naturalOrder(), false, 35, null) {

			@Override
			protected String getExtraString(Entry<ListingMenuItem<String>, Object> item) {
				return "";
			}

			@Override
			protected int getExtraStringOffset() {
				return 0;
			}

			@Override
			public boolean keyPressed(int keyCode) {
				return false;
			}

			@Override
			protected void populateListings(final List<HashMap<ListingMenuItem<String>, Object>> listings) {
				listings.clear();
				listings.add(buildMap(true));
				listings.add(buildMap(false));
			}
		};

		factionsPanel.setScrollWheelActive(true);
	}


	@Override
	protected void internalWindowRender() {
		factionsPanel.height = height;
		factionsPanel.width = width;
		factionsPanel.x = x;
		factionsPanel.y = y;

		factionsPanel.render();
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		factionsPanel.leftClick(copy, windowsCopy);
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public boolean scrolled(int amount) {
		return factionsPanel.scrolled(amount);
	}


	@Override
	public void leftClickReleased() {
		factionsPanel.leftClickReleased();
	}


	public void refreshWindow() {
		factionsPanel.getListing().clear();
		factionsPanel.getListing().add(buildMap(true));
		factionsPanel.getListing().add(buildMap(false));
	}


	private HashMap<ListingMenuItem<String>, Object> buildMap(boolean controlled) {
		HashMap<ListingMenuItem<String>, Object> map = Maps.newHashMap();

		Collection<Faction> newList = Lists.newLinkedList(Domain.getFactions().values());
		if (controlled) {
			newList = Collections2.filter(newList, new Predicate<Faction>() {
				@Override
				public boolean apply(Faction input) {
					return BloodAndMithrilClient.controlledFactions.contains(input.factionId);
				}
			});
		} else {
			newList = Collections2.filter(newList, new Predicate<Faction>() {
				@Override
				public boolean apply(Faction input) {
					return !BloodAndMithrilClient.controlledFactions.contains(input.factionId);
				}
			});
		}

		for (final Faction faction : newList) {
			ContextMenu.MenuItem control = new ContextMenu.MenuItem(
				"Control",
				() -> {
					if (StringUtils.isEmpty(faction.controlPassword)) {
						BloodAndMithrilClient.controlledFactions.add(faction.factionId);
						if (ClientServerInterface.isClient() && !ClientServerInterface.isServer()) {
							ClientServerInterface.SendRequest.sendSynchronizeFactionsRequest();
						}
						refreshWindow();
					} else {
						UserInterface.addLayeredComponent(
							new TextInputWindow(
								250,
								100,
								"Enter password",
								250,
								100,
								args -> {
									if (args[0].toString().equals(faction.controlPassword)) {
										BloodAndMithrilClient.controlledFactions.add(faction.factionId);
										if (ClientServerInterface.isClient() && !ClientServerInterface.isServer()) {
											ClientServerInterface.SendRequest.sendSynchronizeFactionsRequest();
										}
									}
									refreshWindow();
								},
								"Control",
								true,
								""
							)
						);
					}
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			);

			ContextMenu.MenuItem showUnits = new ContextMenu.MenuItem(
				"Show Units",
				() -> {
					UserInterface.addLayeredComponentUnique(
						new UnitsWindow(
							faction.factionId
						)
					);
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			);

			ContextMenu.MenuItem showInfo = new ContextMenu.MenuItem(
				"Show info",
				() -> {
					UserInterface.addLayeredComponentUnique(
						new MessageWindow(
							faction.description,
							Color.ORANGE,
							470,
							120,
							faction.name + " - Info",
							true,
							100,
							100
						)
					);
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			);

			ContextMenu.MenuItem changePassword = new ContextMenu.MenuItem(
				"Change control password",
				() -> {
					UserInterface.addLayeredComponent(
						new TextInputWindow(
							250,
							100,
							"Change password",
							250,
							100,
							args -> {
								if (ClientServerInterface.isServer()) {
									faction.changeControlPassword(args[0].toString());
								} else {
									ClientServerInterface.SendRequest.sendChangeFactionControlPasswordRequest(faction.factionId, args[0].toString());
								}
							},
							"Change",
							true,
							faction.controlPassword
						)
					);
					refreshWindow();
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			);

			final ContextMenu menu = new ContextMenu(
				getMouseScreenX(),
				getMouseScreenY(),
				true
			);

			menu.addFirst(showInfo);
			if (!BloodAndMithrilClient.controlledFactions.contains(faction.factionId)) {
				if (faction.controllable) {
					menu.addMenuItem(control);
				}
			} else {
				menu.addMenuItem(showUnits);
				menu.addMenuItem(changePassword);
			}

			ListingMenuItem<String> menuItem = new ListingMenuItem<String>(
				faction.name,
				new Button(
					faction.name,
					Fonts.defaultFont,
					0,
					0,
					faction.name.length() * 10,
					16,
					() -> {
						menu.x = getMouseScreenX();
						menu.y = getMouseScreenY();
					},
					BloodAndMithrilClient.controlledFactions.contains(faction.factionId) ? Color.GREEN : Color.ORANGE,
					Color.GREEN,
					Color.WHITE,
					UIRef.BL
				),
				() -> {
					return faction.controllable ? menu : null;
				}
		    );
			map.put(menuItem, faction.factionId);
		}
		return map;
	}


	@Override
	public Object getUniqueIdentifier() {
		return getClass();
	}
}