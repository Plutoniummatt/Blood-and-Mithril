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
import com.google.inject.Inject;

import bloodandmithril.character.faction.Faction;
import bloodandmithril.character.faction.FactionControlService;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
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

	@Inject private FactionControlService factionControlService;
	@Inject private UserInterface userInterface;

	/**
	 * Constructor
	 */
	public FactionsWindow(final int length, final int height, final boolean active, final int minLength, final int minHeight) {
		super(length, height, "Factions", active, minLength, minHeight, true, false, true);

		factionsPanel = new ScrollableListingPanel<String, Object>(this, Comparator.<String>naturalOrder(), false, 35, null) {

			@Override
			protected String getExtraString(final Entry<ListingMenuItem<String>, Object> item) {
				return "";
			}

			@Override
			protected int getExtraStringOffset() {
				return 0;
			}

			@Override
			public boolean keyPressed(final int keyCode) {
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
	protected void internalWindowRender(final Graphics graphics) {
		factionsPanel.height = height;
		factionsPanel.width = width;
		factionsPanel.x = x;
		factionsPanel.y = y;

		factionsPanel.render(graphics);
	}


	@Override
	protected void internalLeftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
		factionsPanel.leftClick(copy, windowsCopy);
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public boolean scrolled(final int amount) {
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


	private HashMap<ListingMenuItem<String>, Object> buildMap(final boolean controlled) {
		final HashMap<ListingMenuItem<String>, Object> map = Maps.newHashMap();

		Collection<Faction> newList = Lists.newLinkedList(Domain.getFactions().values());
		if (controlled) {
			newList = Collections2.filter(newList, new Predicate<Faction>() {
				@Override
				public boolean apply(final Faction input) {
					return factionControlService.isUnderControl(input.factionId);
				}
			});
		} else {
			newList = Collections2.filter(newList, new Predicate<Faction>() {
				@Override
				public boolean apply(final Faction input) {
					return !factionControlService.isUnderControl(input.factionId);
				}
			});
		}

		for (final Faction faction : newList) {
			final ContextMenu.MenuItem control = new ContextMenu.MenuItem(
				"Control",
				() -> {
					if (StringUtils.isEmpty(faction.controlPassword)) {
						factionControlService.control(faction.factionId);
						if (ClientServerInterface.isClient() && !ClientServerInterface.isServer()) {
							ClientServerInterface.SendRequest.sendSynchronizeFactionsRequest();
						}
						refreshWindow();
					} else {
						userInterface.addLayeredComponentUnique(
							new TextInputWindow(
								"enterFactionControlPassword",
								250,
								100,
								"Enter password",
								250,
								100,
								args -> {
									if (args[0].toString().equals(faction.controlPassword)) {
										factionControlService.control(faction.factionId);
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

			final ContextMenu.MenuItem showUnits = new ContextMenu.MenuItem(
				"Show Units",
				() -> {
					userInterface.addLayeredComponentUnique(
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

			final ContextMenu.MenuItem showInfo = new ContextMenu.MenuItem(
				"Show info",
				() -> {
					userInterface.addLayeredComponentUnique(
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

			final ContextMenu.MenuItem changePassword = new ContextMenu.MenuItem(
				"Change control password",
				() -> {
					userInterface.addLayeredComponentUnique(
						new TextInputWindow(
							"changeFactionControlPassword",
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
			if (!factionControlService.isUnderControl(faction.factionId)) {
				if (faction.controllable) {
					menu.addMenuItem(control);
				}
			} else {
				menu.addMenuItem(showUnits);
				menu.addMenuItem(changePassword);
			}

			final ListingMenuItem<String> menuItem = new ListingMenuItem<String>(
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
					factionControlService.isUnderControl(faction.factionId) ? Color.GREEN : Color.ORANGE,
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