package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.defaultFont;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Math.min;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.TradeService;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.Refreshable;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.InfoPopup;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.ui.components.panel.TextInputFieldPanel;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.util.datastructure.WrapperForTwo;
import bloodandmithril.world.Domain;

/**
 * Trade window, used when transferring items between {@link Container}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class TradeWindow extends Window implements Refreshable {

	/** Panels of involved traders */
	protected ScrollableListingPanel<Item, Integer> proposerPanel;
	protected ScrollableListingPanel<Item, Integer> proposeePanel;

	protected ScrollableListingPanel<Item, Integer> proposerTradingPanel;
	protected ScrollableListingPanel<Item, Integer> proposeeTradingPanel;

	/** Listings of items to display */
	private final HashMap<ListingMenuItem<Item>, Integer> proposerItemsToTrade = Maps.newHashMap();
	private final HashMap<ListingMenuItem<Item>, Integer> proposerItemsNotToTrade = Maps.newHashMap();
	private final Map<String, WrapperForTwo<Predicate<Item>, Boolean>> proposerFilters = Maps.newHashMap();
	private final ScrollableListingPanel<Button, String> proposerFilterButtons;

	private final HashMap<ListingMenuItem<Item>, Integer> proposeeItemsToTrade = Maps.newHashMap();
	private final HashMap<ListingMenuItem<Item>, Integer> proposeeItemsNotToTrade = Maps.newHashMap();
	private final Map<String, WrapperForTwo<Predicate<Item>, Boolean>> proposeeFilters = Maps.newHashMap();
	private final ScrollableListingPanel<Button, String> proposeeFilterButtons;

	/** Traders */
	protected Container proposer;
	protected final Container proposee;

	/** Used to process trade rejections */
	private boolean rejected = false;
	private float tradeRejectionTimer = 1f;

	private final Button tradeButton = new Button(
		"Trade",
		defaultFont,
		0,
		0,
		130,
		16,
		() -> {
			proposeTrade();
		},
		Color.GREEN,
		Color.GRAY,
		Color.WHITE,
		UIRef.BL
	);

	private final Button proposerButton, proposeeButton;

	/** The text search predicate */
	private String searchString = "";
	private final Predicate<Item> textSearch = new Predicate<Item>() {
		@Override
		public boolean apply(Item input) {
			return input.getSingular(true).toUpperCase().contains(searchString.toUpperCase());
		}
	};

	/** Input for text searching */
	private final TextInputFieldPanel textInput = new TextInputFieldPanel(this, "");

	/**
	 * Constructor
	 */
	public TradeWindow(String title, boolean active, Individual proposer, Container proposee, Comparator<Item> sortingComparator) {
		super(
			1300,
			600,
			title,
			active,
			1300,
			500,
			false,
			true,
			true
		);

		this.proposer = proposer;
		this.proposee = proposee;

		populate(proposerItemsToTrade, proposerItemsNotToTrade, proposer.getInventory());
		populate(proposeeItemsToTrade, proposeeItemsNotToTrade, proposee.getInventory());

		if (proposee instanceof Prop) {
			tradeButton.text = () -> {return "Transfer Items";};
		}

		createPanels(sortingComparator);

		proposerFilterButtons = InventoryWindow.setupFilters(proposerFilters, this).canScroll(false);
		proposeeFilterButtons = InventoryWindow.setupFilters(proposeeFilters, this).canScroll(false);

		String proposerTitle = "---- " + proposer.getId().getSimpleName() + " ----";
		proposerButton = new Button(
			proposerTitle,
			defaultFont,
			0,
			0,
			10 * proposer.getId().getSimpleName().length(),
			16,
			() -> {
			},
			Color.WHITE,
			Color.WHITE,
			Color.WHITE,
			UIRef.BL
		);

		String proposeeTitle = "---- " + (proposee instanceof Individual ? ((Individual)proposee).getId().getSimpleName() : ((Prop) proposee).getTitle()) + " ----";
		proposeeButton = new Button(
			proposeeTitle,
			defaultFont,
			0,
			0,
			10 * proposeeTitle.length(),
			16,
			() -> {
			},
			Color.WHITE,
			Color.WHITE,
			Color.WHITE,
			UIRef.BL
		);
	}


	/**
	 * Called when the "Propose Trade" button is clicked.
	 */
	private void proposeTrade() {

		boolean proposalAccepted = false;

		if (proposee instanceof Prop) {
			proposalAccepted = true;
		}


		if (proposer instanceof Individual && proposee instanceof Individual && !proposalAccepted) {
			HashMap<Item, Integer> tradeThis = Maps.newHashMap();
			HashMap<Item, Integer> forThis = Maps.newHashMap();

			for (Entry<ListingMenuItem<Item>, Integer> entry : proposerItemsToTrade.entrySet()) {
				tradeThis.put(entry.getKey().t, entry.getValue());
			}

			for (Entry<ListingMenuItem<Item>, Integer> entry : proposeeItemsToTrade.entrySet()) {
				forThis.put(entry.getKey().t, entry.getValue());
			}

			proposalAccepted = TradeService.evaluate(
				(Individual)proposer,
				tradeThis,
				(Individual)proposee,
				forThis
			);
		}

		if (proposalAccepted) {
			HashMap<Item, Integer> proposerItemsToTransfer = Maps.newHashMap();
			for (Entry<ListingMenuItem<Item>, Integer> entry : proposerItemsToTrade.entrySet()) {
				proposerItemsToTransfer.put(entry.getKey().t, entry.getValue());
			}

			HashMap<Item, Integer> proposeeItemsToTransfer = Maps.newHashMap();
			for (Entry<ListingMenuItem<Item>, Integer> entry : proposeeItemsToTrade.entrySet()) {
				proposeeItemsToTransfer.put(entry.getKey().t, entry.getValue());
			}

			TradeService.transferItems(proposerItemsToTransfer, proposer, proposeeItemsToTransfer, proposee);

			if (ClientServerInterface.isServer() && ClientServerInterface.isClient()) {
				UserInterface.refreshRefreshableWindows();
			}

			proposerItemsToTrade.clear();
			proposeeItemsToTrade.clear();
			refresh();
		} else {
			rejectTradeProposal();
		}
	}


	/**
	 * Refreshes this window and synchronises it with the trader inventories
	 */
	@SuppressWarnings("unchecked")
	@Override
	public synchronized void refresh() {
		proposer = Domain.getIndividual(((Individual) proposer).getId().getId());

		HashMap<ListingMenuItem<Item>, Integer> proposerToTrade = newHashMap(proposerItemsToTrade);
		HashMap<ListingMenuItem<Item>, Integer> proposeeToTrade = newHashMap(proposeeItemsToTrade);

		proposerItemsToTrade.clear();
		proposeeItemsToTrade.clear();
		proposerItemsNotToTrade.clear();
		proposeeItemsNotToTrade.clear();

		populate(proposerItemsToTrade, proposerItemsNotToTrade, proposer.getInventory());
		populate(proposeeItemsToTrade, proposeeItemsNotToTrade, proposee.getInventory());

		for (Entry<ListingMenuItem<Item>, Integer> entry : proposerToTrade.entrySet()) {
			Optional<Entry<ListingMenuItem<Item>, Integer>> tryFind = tryFind(proposerItemsNotToTrade.entrySet(), e -> {
				return e.getKey().t.sameAs(entry.getKey().t);
			});

			changeList(
				entry.getKey().t,
				min(
					entry.getValue(),
					tryFind.isPresent() ? tryFind.get().getValue() : 0
				),
				proposerItemsToTrade,
				proposerItemsNotToTrade,
				false
			);
		}

		for (Entry<ListingMenuItem<Item>, Integer> entry : proposeeToTrade.entrySet()) {
			Optional<Entry<ListingMenuItem<Item>, Integer>> tryFind = tryFind(proposeeItemsNotToTrade.entrySet(), e -> {
				return e.getKey().t.sameAs(entry.getKey().t);
			});

			changeList(
				entry.getKey().t,
				min(
					entry.getValue(),
					tryFind.isPresent() ? tryFind.get().getValue() : 0
				),
				proposeeItemsToTrade,
				proposeeItemsNotToTrade,
				false
			);
		}


		proposerPanel.getFilters().clear();
		proposerPanel.getFilters().addAll(Collections2.transform(
			Collections2.filter(proposerFilters.entrySet(), toKeep -> {
				return toKeep.getValue().b;
			}),
			value -> {
				return value.getValue().a;
			}
		));

		proposeePanel.getFilters().clear();
		proposeePanel.getFilters().addAll(Collections2.transform(
			Collections2.filter(proposeeFilters.entrySet(), toKeep -> {
				return toKeep.getValue().b;
			}),
			value -> {
				return value.getValue().a;
			}
		));

		proposerPanel.refresh(Lists.newArrayList(proposerItemsNotToTrade));
		proposeePanel.refresh(Lists.newArrayList(proposeeItemsNotToTrade));
		proposerTradingPanel.refresh(Lists.newArrayList(proposerItemsToTrade));
		proposeeTradingPanel.refresh(Lists.newArrayList(proposeeItemsToTrade));
	}


	/**
	 * The proposee has rejected the proposers proposal.
	 */
	private void rejectTradeProposal() {
		rejected = true;
	}


	private void createPanels(Comparator<Item> sortingComparator) {
		proposerPanel = new ScrollableListingPanel<Item, Integer>(this, sortingComparator, true, 35, textSearch) {

			@Override
			protected void populateListings(List<HashMap<ListingMenuItem<Item>, Integer>> listings) {
				listings.add(proposerItemsNotToTrade);
			}

			@Override
			protected int getExtraStringOffset() {
				return 80;
			}

			@Override
			protected String getExtraString(Entry<ListingMenuItem<Item>, Integer> item) {
				return Integer.toString(item.getValue());
			}

			@Override
			public boolean keyPressed(int keyCode) {
				return false;
			}
		};

		proposeePanel = new ScrollableListingPanel<Item, Integer>(this, sortingComparator, true, 35, textSearch) {

			@Override
			protected void populateListings(List<HashMap<ListingMenuItem<Item>, Integer>> listings) {
				listings.add(proposeeItemsNotToTrade);
			}

			@Override
			protected int getExtraStringOffset() {
				return 80;
			}

			@Override
			protected String getExtraString(Entry<ListingMenuItem<Item>, Integer> item) {
				return Integer.toString(item.getValue());
			}

			@Override
			public boolean keyPressed(int keyCode) {
				return false;
			}
		};

		proposerTradingPanel = new ScrollableListingPanel<Item, Integer>(this, sortingComparator, false, 35, null) {

			@Override
			protected void populateListings(List<HashMap<ListingMenuItem<Item>, Integer>> listings) {
				listings.add(proposerItemsToTrade);
			}

			@Override
			protected int getExtraStringOffset() {
				return 80;
			}


			@Override
			protected String getExtraString(Entry<ListingMenuItem<Item>, Integer> item) {
				return Integer.toString(item.getValue());
			}

			@Override
			public boolean keyPressed(int keyCode) {
				return false;
			}
		};

		proposeeTradingPanel = new ScrollableListingPanel<Item, Integer>(this, sortingComparator, false, 35, null) {

			@Override
			protected void populateListings(List<HashMap<ListingMenuItem<Item>, Integer>> listings) {
				listings.add(proposeeItemsToTrade);
			}

			@Override
			protected int getExtraStringOffset() {
				return 80;
			}


			@Override
			protected String getExtraString(Entry<ListingMenuItem<Item>, Integer> item) {
				return Integer.toString(item.getValue());
			}

			@Override
			public boolean keyPressed(int keyCode) {
				return false;
			}
		};
	}


	/**
	 * Populates a listing
	 */
	private void populate(final HashMap<ListingMenuItem<Item>, Integer> trading, final HashMap<ListingMenuItem<Item>, Integer> notTrading, Map<Item, Integer> toPopulateFrom) {
		for (final Entry<Item, Integer> entry : toPopulateFrom.entrySet()) {

			Button button = new Button(
				() -> {return entry.getKey().getSingular(true);},
				defaultFont,
				0,
				0,
				entry.getKey().getSingular(true).length() * 10,
				16,
				() -> {
					if (!isItemAvailableToTrade(proposer, proposee, entry.getKey())) {
						return;
					}

					if (Gdx.input.isKeyPressed(BloodAndMithrilClient.getKeyMappings().bulkTrade.keyCode)) {
						UserInterface.addLayeredComponent(
							new TextInputWindow(
								250,
								100,
								"Enter quantity",
								250,
								100,
								args -> {
									try {
										changeList(entry.getKey(), Integer.parseInt(args[0].toString()), trading, notTrading, false);
										setActive(true);
									} catch (NumberFormatException e) {
										UserInterface.addGlobalMessage("Error", "Cannot recognise " + args[0].toString() + " as a quantity.");
									}
								},
								"Confirm",
								true,
								""
							)
						);
					} else {
						changeList(entry.getKey(), 1, trading, notTrading, false);
					}
				},
				isItemAvailableToTrade(proposer, proposee, entry.getKey()) ? entry.getKey().getType().getColor() : Color.DARK_GRAY,
				isItemAvailableToTrade(proposer, proposee, entry.getKey()) ? Color.GREEN : Colors.UI_DARKER_GRAY,
				isItemAvailableToTrade(proposer, proposee, entry.getKey()) ? Color.WHITE : Colors.UI_DARKER_GRAY,
				UIRef.BL
			);

			button.mouseOverPopup(
				() -> {
					return new InfoPopup(
						entry.getKey().getInfoPanel(),
						() -> {
							return !button.isMouseOver();
						}
					);
				}
			);

			final ListingMenuItem<Item> listingMenuItem = new ListingMenuItem<Item>(
				entry.getKey(),
				button,
				null
			);

			notTrading.put(
				listingMenuItem,
				entry.getValue()
			);
		}
	}


	/**
	 * @return whether or not a listing item is able to be selected for trade
	 */
	protected boolean isItemAvailableToTrade(Container proposer, Container proposee, Item item) {
		return true; // by default
	}


	private void changeList(final Item key, int numberToChange, final HashMap<ListingMenuItem<Item>, Integer> transferTo, final HashMap<ListingMenuItem<Item>, Integer> transferFrom, final boolean toTrade) {
		Button button = new Button(
			() -> {return key.getSingular(true);},
			defaultFont,
			0,
			0,
			key.getSingular(true).length() * 10,
			16,
			() -> {
				if (Gdx.input.isKeyPressed(BloodAndMithrilClient.getKeyMappings().bulkTrade.keyCode)) {
					UserInterface.addLayeredComponent(
						new TextInputWindow(
							250,
							100,
							"Enter quantity",
							250,
							100,
							args -> {
								try {
									changeList(key, Integer.parseInt(args[0].toString()), transferFrom, transferTo, !toTrade);
									setActive(true);
								} catch (NumberFormatException e) {
									UserInterface.addGlobalMessage("Error", "Cannot recognise " + args[0].toString() + " as a quantity.");
								}
							},
							"Confirm",
							true,
							""
						)
					);
				} else {
					changeList(key, 1, transferFrom, transferTo, !toTrade);
				}
			},
			toTrade ? Color.ORANGE : Color.MAGENTA,
			Color.GREEN,
			Color.WHITE,
			UIRef.BL
		);

		button.mouseOverPopup(
			() -> {
				return new InfoPopup(
					key.getInfoPanel(),
					() -> {
						return !button.isMouseOver();
					}
				);
			}
		);

		final ListingMenuItem<Item> listingMenuItem = new ListingMenuItem<Item>(
			key,
			button,
			null
		);

		for (Entry<ListingMenuItem<Item>, Integer> entry : Lists.newArrayList(transferFrom.entrySet())) {
			if (entry.getKey().t.sameAs(key)) {
				int transferred = 0;

				if (transferFrom.get(entry.getKey()) <= numberToChange) {
					transferred = entry.getValue();
					transferFrom.remove(entry.getKey());
				} else {
					transferred = numberToChange;
					transferFrom.put(entry.getKey(), transferFrom.get(entry.getKey()) - numberToChange);
				}

				boolean found = false;
				for (Entry<ListingMenuItem<Item>, Integer> innerEntry : Lists.newArrayList(transferTo.entrySet())) {
					if (innerEntry.getKey().t.sameAs(key)) {
						transferTo.remove(innerEntry.getKey());
						transferTo.put(listingMenuItem, innerEntry.getValue() + transferred);
						found = true;
						break;
					}
				}
				if (!found) {
					transferTo.put(listingMenuItem, transferred);
				}

				break;
			}
		}
	}


	@Override
	protected void internalWindowRender() {
		if (proposer instanceof Individual) {
			if (!((Individual) proposer).isAlive()) {
				setClosing(true);
			}

			if (proposee instanceof Individual) {
				if (((Individual) proposee).getState().position.cpy().sub(((Individual) proposer).getState().position.cpy()).len() > 64) {
					setClosing(true);
				}

				if (!((Individual) proposee).isAlive()) {
					setClosing(true);
				}
			} else if (proposee instanceof Prop) {
				if (((Prop) proposee).position.cpy().sub(((Individual) proposer).getState().position.cpy()).len() > 64) {
					setClosing(true);
				}
			}
		}

		renderListingPanels();

		if (rejected) {
			if (tradeRejectionTimer > 0f) {
				tradeRejectionTimer = tradeRejectionTimer - 0.01f;
				tradeButton.text = () -> {return "Trade Rejected";};
				tradeButton.setIdleColor(Color.RED);
			} else {
				if (proposee instanceof Prop) {
					tradeButton.text = () -> {return "Transfer";};
				} else {
					tradeButton.text = () -> {return "Propose Trade";};
				}
				tradeRejectionTimer = 1f;
				rejected = false;
				tradeButton.setIdleColor(Color.GREEN);
			}
		}

		float proposerMass = (float) proposerItemsToTrade.entrySet().stream().mapToDouble(entry -> {
			return entry.getValue() * entry.getKey().t.getMass();
		}).sum();

		float proposeeMass = (float) proposeeItemsToTrade.entrySet().stream().mapToDouble(entry -> {
			return entry.getValue() * entry.getKey().t.getMass();
		}).sum();

		int proposerVolume = (int) proposerItemsToTrade.entrySet().stream().mapToLong(entry -> {
			return entry.getValue() * entry.getKey().t.getVolume();
		}).sum();

		int proposeeVolume = (int) proposeeItemsToTrade.entrySet().stream().mapToLong(entry -> {
			return entry.getValue() * entry.getKey().t.getVolume();
		}).sum();

		InventoryWindow.renderCapacityIndicationText(proposer, this, 6, -height, " (+" + String.format("%.2f", proposeeMass) + ")", " (+" + proposeeVolume + ")");
		InventoryWindow.renderCapacityIndicationText(proposee, this, width / 2 + 6, -height, " (+" + String.format("%.2f", proposerMass) + ")", " (+" + proposerVolume + ")");

		// Render the text search
		textInput.x = x + 6;
		textInput.y = y - height + 90;
		textInput.width = 180;
		textInput.height = 20;
		textInput.render();

		tradeButton.render(
			x + 71,
			y - height + 70,
			tradeButtonClickable(),
			getAlpha()
		);

		proposerButton.render(
			x + width / 4,
			y - 20,
			isActive(),
			getAlpha()
		);

		proposeeButton.render(
			x + 3 * width / 4,
			y - 20,
			isActive(),
			getAlpha()
		);
	}


	protected boolean tradeButtonClickable() {
		return isActive() && !rejected && (!proposeeItemsToTrade.isEmpty() || !proposerItemsToTrade.isEmpty());
	}


	protected boolean isProposeeItemsEmpty() {
		return proposeeItemsToTrade.isEmpty() && !proposeeItemsNotToTrade.isEmpty();
	}


	/**
	 * Renders the listing panels
	 */
	protected synchronized void renderListingPanels() {
		int lineWidth = 23;

		proposerPanel.x = x + 200;
		proposerPanel.y = y - (proposerItemsToTrade.isEmpty() ? 0 : (2 + min(5,proposerItemsToTrade.size())) * lineWidth) - 30;
		proposerPanel.height = height - 85 - (proposerItemsToTrade.isEmpty() ? 0 : (3 + min(5, proposerItemsToTrade.size())) * lineWidth);
		proposerPanel.width = width / 2 - 210;

		proposeePanel.x = x + width / 2 + 10 + 200;
		proposeePanel.y = y - (proposeeItemsToTrade.isEmpty() ? 0 : (2 + min(5,proposeeItemsToTrade.size())) * lineWidth) - 30;
		proposeePanel.height = height - 85 - (proposeeItemsToTrade.isEmpty() ? 0 : (3 + min(5, proposeeItemsToTrade.size())) * lineWidth);
		proposeePanel.width = width / 2 - 210;

		proposerTradingPanel.x = x + 200;
		proposerTradingPanel.y = y - 30;
		proposerTradingPanel.height = 45 + (proposerItemsToTrade.isEmpty() ? 0 : (1 + min(5, proposerItemsToTrade.size())) * lineWidth);
		proposerTradingPanel.width = width / 2 - 210;

		proposeeTradingPanel.x = x + width / 2 + 210;
		proposeeTradingPanel.y = y - 30;
		proposeeTradingPanel.height = 45 + (proposeeItemsToTrade.isEmpty() ? 0 : (1 + min(5, proposeeItemsToTrade.size())) * lineWidth);
		proposeeTradingPanel.width = width / 2 - 210;

		proposerFilterButtons.x = x;
		proposerFilterButtons.y = y - 30;
		proposerFilterButtons.height = height - 85;
		proposerFilterButtons.width = 180;

		proposeeFilterButtons.x = x + width / 2 + 10;
		proposeeFilterButtons.y = y - 30;
		proposeeFilterButtons.height = height - 85;
		proposeeFilterButtons.width = 180;

		proposerFilterButtons.render();
		proposeeFilterButtons.render();
		proposerPanel.render();
		proposeePanel.render();

		if (!proposerItemsToTrade.isEmpty()) {
			proposerTradingPanel.render();
		}
		if (!proposeeItemsToTrade.isEmpty()) {
			proposeeTradingPanel.render();
		}
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		proposerPanel.leftClick(copy, windowsCopy);
		proposeePanel.leftClick(copy, windowsCopy);
		proposerTradingPanel.leftClick(copy, windowsCopy);
		proposeeTradingPanel.leftClick(copy, windowsCopy);
		proposeeFilterButtons.leftClick(copy, windowsCopy);
		proposerFilterButtons.leftClick(copy, windowsCopy);

		if (tradeButtonClickable()) {
			tradeButton.click();
		}
	}


	@Override
	public boolean keyPressed(int keyCode) {
		boolean keyPressed = textInput.keyPressed(keyCode);
		this.searchString = textInput.getInputText();
		refresh();

		return super.keyPressed(keyCode) || keyPressed;
	}


	@Override
	public boolean scrolled(int amount) {
		return
			proposerTradingPanel.scrolled(amount) ||
			proposeeTradingPanel.scrolled(amount) ||
			proposerPanel.scrolled(amount) ||
			proposeePanel.scrolled(amount) ||
			proposeeFilterButtons.scrolled(amount) ||
			proposerFilterButtons.scrolled(amount);
	}


	@Override
	public void leftClickReleased() {
		proposerPanel.leftClickReleased();
		proposeePanel.leftClickReleased();
		proposerTradingPanel.leftClickReleased();
		proposeeTradingPanel.leftClickReleased();
		proposeeFilterButtons.leftClickReleased();
		proposerFilterButtons.leftClickReleased();
	}


	@Override
	protected void uponClose() {
		if (ClientServerInterface.isServer()) {
			if (proposer instanceof Individual) {
				((Individual) proposer).getAI().setCurrentTask(new Idle());
			}
			if (proposee instanceof Individual) {
				((Individual) proposee).getAI().setCurrentTask(new Idle());
			}
		} else {
			if (Domain.isIndividualSelected((Individual) proposer)) {
				ClientServerInterface.SendRequest.sendClearAITaskRequest(((Individual)proposer).getId().getId());
			}
			if (proposee instanceof Individual && Domain.isIndividualSelected((Individual) proposee)) {
				ClientServerInterface.SendRequest.sendClearAITaskRequest(((Individual)proposee).getId().getId());
			}
		}
	}


	@Override
	public Object getUniqueIdentifier() {
		return "tradeWindow" + (proposer.hashCode() + proposee.hashCode());
	}
}