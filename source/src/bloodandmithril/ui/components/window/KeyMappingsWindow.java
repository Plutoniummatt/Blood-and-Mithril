package bloodandmithril.ui.components.window;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Color;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import bloodandmithril.control.Controls;
import bloodandmithril.control.Controls.MappedKey;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.persistence.ConfigPersistenceService;
import bloodandmithril.ui.Refreshable;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;

/**
 * Window to remap controls
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class KeyMappingsWindow extends Window implements Refreshable {

	@Inject
	private Controls controls;

	private ScrollableListingPanel<MappedKey, String> keyMappings;
	private Button saveButton = new Button(
		"Save and close",
		defaultFont,
		0,
		0,
		140,
		16,
		() -> {
			ConfigPersistenceService.saveConfig();
			KeyMappingsWindow.this.setClosing(true);
		},
		Color.GREEN,
		Color.WHITE,
		Color.GREEN,
		UIRef.BL
	);

	private static Comparator<MappedKey> mapabilityComparator = new Comparator<MappedKey>() {
		@Override
		public int compare(final MappedKey o1, final MappedKey o2) {
			return new Boolean(o1.canChange).compareTo(o2.canChange);
		}

	};

	private static Comparator<MappedKey> stringComparator = new Comparator<MappedKey>() {
		@Override
		public int compare(final MappedKey o1, final MappedKey o2) {
			return ComparisonChain.start().compare(o1, o2, mapabilityComparator).compare(o1.description, o2.description).result();
		}
	};

	/**
	 * Constructor
	 */
	public KeyMappingsWindow() {
		super(500, 400, "Controls", true, false, true, true);

		keyMappings = new ScrollableListingPanel<MappedKey, String>(this, stringComparator, false, 100, null) {

			@Override
			protected String getExtraString(final Entry<ListingMenuItem<MappedKey>, String> item) {
				return Controls.keyName.get(Integer.parseInt(item.getValue()));
			}

			@Override
			protected int getExtraStringOffset() {
				return 90;
			}

			@Override
			protected void populateListings(final List<HashMap<ListingMenuItem<MappedKey>, String>> listings) {
				populateKeyMappingsPairs(listings);
			}

			@Override
			public boolean keyPressed(final int keyCode) {
				return false;
			}
		};
	}


	@Override
	protected void internalWindowRender(final Graphics graphics) {
		keyMappings.x = x;
		keyMappings.y = y;
		keyMappings.width = width;
		keyMappings.height = height - 50;

		keyMappings.render(graphics);
		saveButton.render(x + width / 2, y - height + 40, isActive(), getAlpha(), graphics);
	}


	@Override
	protected void internalLeftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
		keyMappings.leftClick(copy, windowsCopy);
		saveButton.click();
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public Object getUniqueIdentifier() {
		return KeyMappingsWindow.class;
	}


	@Override
	public void leftClickReleased() {
		keyMappings.leftClickReleased();
	}


	@Override
	public boolean scrolled(final int amount) {
		return keyMappings.scrolled(amount);
	}


	@Override
	public void refresh() {
		keyMappings.getListing().clear();
		populateKeyMappingsPairs(keyMappings.getListing());
	}


	private void populateKeyMappingsPairs(final List<HashMap<ListingMenuItem<MappedKey>, String>> listings) {
		final HashMap<ListingMenuItem<MappedKey>, String> map = Maps.newHashMap();

		for (final MappedKey mappedKey : controls.getFunctionalKeyMappings().values()) {
			final ContextMenu contextMenu = new ContextMenu(
				getMouseScreenX(),
				getMouseScreenY(),
				true,
				new ContextMenu.MenuItem(
					"Show info",
					() -> {
						UserInterface.addLayeredComponentUnique(
							new MessageWindow(
								mappedKey.showInfo,
								Color.ORANGE,
								300,
								200,
								mappedKey.description,
								true
							)
						);
					},
					Color.ORANGE,
					Color.GREEN,
					Color.WHITE,
					null
				)
			);

			if (mappedKey.canChange) {
				contextMenu.addMenuItem(
					new ContextMenu.MenuItem(
						"Change",
						() -> {
							UserInterface.addLayeredComponentUnique(
								new ChangeKeyWindow(mappedKey)
							);
						},
						Color.ORANGE,
						Color.GREEN,
						Color.WHITE,
						null
					)
				);
			}

			map.put(
				new ListingMenuItem<MappedKey>(
					mappedKey,
					new Button(
						mappedKey.description,
						defaultFont,
						0,
						0,
						mappedKey.description.length() * 10,
						16,
						() -> {
						},
						Color.ORANGE,
						Color.GREEN,
						Color.WHITE,
						UIRef.BL
					),
					() -> { return contextMenu;}
				),
				Integer.toString(mappedKey.keyCode)
			);
		}

		for (final MappedKey mappedKey : controls.getUnmappableKeys()) {
			final ContextMenu contextMenu = new ContextMenu(
				getMouseScreenX(),
				getMouseScreenY(),
				true,
				new ContextMenu.MenuItem(
					"Show info",
					() -> {
						UserInterface.addLayeredComponentUnique(
							new MessageWindow(
								mappedKey.showInfo,
								Color.ORANGE,
								300,
								200,
								mappedKey.description,
								true
							)
						);
					},
					Color.ORANGE,
					Color.GREEN,
					Color.WHITE,
					null
				)
			);

			map.put(
				new ListingMenuItem<MappedKey>(
					mappedKey,
					new Button(
						mappedKey.description,
						defaultFont,
						0,
						0,
						mappedKey.description.length() * 10,
						16,
						() -> {
						},
						Color.PURPLE,
						Color.GREEN,
						Color.WHITE,
						UIRef.BL
					),
					() -> { return contextMenu;}
				),
				Integer.toString(mappedKey.keyCode)
			);
		}

		listings.add(map);
	}


	/**
	 * Window used for changing key binding.
	 *
	 * @author Matt
	 */
	@Copyright("Matthew Peck 2015")
	public static class ChangeKeyWindow extends Window {
		@Inject
		private Controls controls;

		private MappedKey mappedKey;

		/**
		 * Constructor
		 */
		public ChangeKeyWindow(final MappedKey mappedKey) {
			super(200, 100, "Change key", true, false, false, true);
			this.mappedKey = mappedKey;
		}

		@Override
		public boolean keyPressed(final int keyCode) {
			if (Controls.disallowedKeys.contains(keyCode)) {
				UserInterface.addGlobalMessage("Disallowed", "Can not remap this key.");
				setClosing(true);
			} else {
				if (controls.getFunctionalKeyMappings().containsKey(keyCode)) {
					UserInterface.addGlobalMessage("Conflict", "Key already mapped to " + controls.getFunctionalKeyMappings().get(keyCode).description);
				} else {
					mappedKey.keyCode = keyCode;
				}
				setClosing(true);
			}

			return super.keyPressed(keyCode);
		}

		@Override
		protected void internalWindowRender(final Graphics graphics) {
			defaultFont.setColor(isActive() ? Colors.modulateAlpha(Color.ORANGE, getAlpha()) : Colors.modulateAlpha(Color.ORANGE, 0.6f * getAlpha()));
			final String messageToDisplay = Util.fitToWindow("Press Key", width, (height - 75) / 25);
			defaultFont.drawMultiLine(graphics.getSpriteBatch(), messageToDisplay, x + 6, y - 25);

			UserInterface.refreshRefreshableWindows();
		}

		@Override
		protected void internalLeftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
		}

		@Override
		protected void uponClose() {
		}

		@Override
		public Object getUniqueIdentifier() {
			return "ChangeKey" + mappedKey.keyCode;
		}

		@Override
		public void leftClickReleased() {
		}
	}
}