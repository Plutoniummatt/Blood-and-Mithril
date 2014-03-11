package bloodandmithril.ui.components.window;

import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.faction.Faction;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.JITTask;
import bloodandmithril.util.Task;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Window that displays factions
 *
 * @author Copyright (c) CHP Consulting Ltd. 2014
 */
public class FactionsWindow extends Window {

	private ScrollableListingPanel<String> factionsPanel;

	/**
	 * Constructor
	 */
	public FactionsWindow(int x, int y, int length, int height, boolean active, int minLength, int minHeight) {
		super(x, y, length, height, "Factions", active, minLength, minHeight, true, false);

		factionsPanel = new ScrollableListingPanel<String>(this) {

			@Override
			protected String getExtraString(Entry<ListingMenuItem<String>, Integer> item) {
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
			protected void onSetup(final List<HashMap<ListingMenuItem<String>, Integer>> listings) {
				listings.clear();
				listings.add(buildMap(true));
				listings.add(buildMap(false));
			}
		};
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
	public boolean keyPressed(int keyCode) {
		return false;
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


	private HashMap<ListingMenuItem<String>, Integer> buildMap(boolean controlled) {
		HashMap<ListingMenuItem<String>, Integer> map = Maps.newHashMap();

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
			ContextMenu.ContextMenuItem control = new ContextMenu.ContextMenuItem(
				"Control",
				new Task() {
					@Override
					public void execute() {
						if (StringUtils.isEmpty(faction.controlPassword)) {
							BloodAndMithrilClient.controlledFactions.add(faction.factionId);
							if (ClientServerInterface.isClient() && !ClientServerInterface.isServer()) {
								ClientServerInterface.SendRequest.sendSynchronizeFactionsRequest();
							}
							refreshWindow();
						} else {
							UserInterface.addLayeredComponent(
								new TextInputWindow(
									BloodAndMithrilClient.WIDTH / 2 - 125,
									BloodAndMithrilClient.HEIGHT/2 + 50,
									250,
									100,
									"Enter password",
									250,
									100,
									new JITTask() {
										@Override
										public void execute(Object... args) {
											if (args[0].toString().equals(faction.controlPassword)) {
												BloodAndMithrilClient.controlledFactions.add(faction.factionId);
												if (ClientServerInterface.isClient() && !ClientServerInterface.isServer()) {
													ClientServerInterface.SendRequest.sendSynchronizeFactionsRequest();
												}
											}
											refreshWindow();
										}
									},
									"Control",
									true,
									""
								)
							);
						}
					}
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			);

			ContextMenu.ContextMenuItem changePassword = new ContextMenu.ContextMenuItem(
				"Change control password",
				new Task() {
					@Override
					public void execute() {
						UserInterface.addLayeredComponent(
							new TextInputWindow(
								BloodAndMithrilClient.WIDTH / 2 - 125,
								BloodAndMithrilClient.HEIGHT/2 + 50,
								250,
								100,
								"Change password",
								250,
								100,
								new JITTask() {
									@Override
									public void execute(Object... args) {
										if (ClientServerInterface.isServer()) {
											faction.changeControlPassword(args[0].toString());
										} else {
											ClientServerInterface.SendRequest.sendChangeFactionControlPasswordRequest(faction.factionId, args[0].toString());
										}
									}
								},
								"Change",
								true,
								faction.controlPassword
							)
						);
						refreshWindow();
					}
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			);

			final ContextMenu menu = new ContextMenu(
				BloodAndMithrilClient.getMouseScreenX(),
				BloodAndMithrilClient.getMouseScreenY()
			);

			if (!BloodAndMithrilClient.controlledFactions.contains(faction.factionId)) {
				if (faction.controllable) {
					menu.addMenuItem(control);
				}
			} else {
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
					new Task() {
						@Override
						public void execute() {
							menu.x = BloodAndMithrilClient.getMouseScreenX();
							menu.y = BloodAndMithrilClient.getMouseScreenY();
						}
					},
					BloodAndMithrilClient.controlledFactions.contains(faction.factionId) ? Color.GREEN : Color.ORANGE,
					Color.GREEN,
					Color.WHITE,
					UIRef.BL
				),
				faction.controllable ? menu : null
		    );
			map.put(menuItem, faction.factionId);
		}
		return map;
	}
}