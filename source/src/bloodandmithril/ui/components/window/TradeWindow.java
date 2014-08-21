package bloodandmithril.ui.components.window;

import static bloodandmithril.core.BloodAndMithrilClient.HEIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.WIDTH;
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

import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.character.individuals.Individual;
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
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
	protected final HashMap<ListingMenuItem<Item>, Integer> proposerItemsToTrade = Maps.newHashMap();
	private final HashMap<ListingMenuItem<Item>, Integer> proposerItemsNotToTrade = Maps.newHashMap();

	private final HashMap<ListingMenuItem<Item>, Integer> proposeeItemsToTrade = Maps.newHashMap();
	private final HashMap<ListingMenuItem<Item>, Integer> proposeeItemsNotToTrade = Maps.newHashMap();

	/** Traders */
	protected Container proposer;
	protected final Container proposee;

	/** Used to process trade rejections */
	private boolean rejected = false;
	private float tradeRejectionTimer = 1f;

	private final Button tradeButton = new Button(
		"Propose Trade",
		defaultFont,
		0,
		0,
		130,
		16,
		() -> {
			proposeTrade();
		},
		Color.GREEN,
		Color.ORANGE,
		Color.WHITE,
		UIRef.BL
	);

	/**
	 * Constructor
	 */
	public TradeWindow(int x, int y, int length, int height, String title, boolean active, int minLength, int minHeight, Individual proposer, Container proposee, Comparator<Item> sortingComparator) {
		super(x, y, length, height, title, active, minLength, minHeight, false, true, true);

		this.proposer = proposer;
		this.proposee = proposee;

		populate(proposerItemsToTrade, proposerItemsNotToTrade, proposer.getInventory());
		populate(proposeeItemsToTrade, proposeeItemsNotToTrade, proposee.getInventory());

		if (proposee instanceof Prop) {
			tradeButton.text = "Transfer Items";
		}

		createPanels(sortingComparator);
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
		proposer = Domain.getIndividuals().get(((Individual) proposer).getId().getId());

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
		proposerPanel = new ScrollableListingPanel<Item, Integer>(this, sortingComparator, false, 35) {

			@Override
			protected void onSetup(List<HashMap<ListingMenuItem<Item>, Integer>> listings) {
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

		proposeePanel = new ScrollableListingPanel<Item, Integer>(this, sortingComparator, false, 35) {

			@Override
			protected void onSetup(List<HashMap<ListingMenuItem<Item>, Integer>> listings) {
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

		proposerTradingPanel = new ScrollableListingPanel<Item, Integer>(this, sortingComparator, false, 35) {

			@Override
			protected void onSetup(List<HashMap<ListingMenuItem<Item>, Integer>> listings) {
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

		proposeeTradingPanel = new ScrollableListingPanel<Item, Integer>(this, sortingComparator, false, 35) {

			@Override
			protected void onSetup(List<HashMap<ListingMenuItem<Item>, Integer>> listings) {
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

			final ListingMenuItem<Item> listingMenuItem = new ListingMenuItem<Item>(
				entry.getKey(),
				new Button(
					entry.getKey().getSingular(true),
					defaultFont,
					0,
					0,
					entry.getKey().getSingular(true).length() * 10,
					16,
					() -> {
						if (!isItemAvailableToTrade(entry.getKey())) {
							return;
						}

						if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
							UserInterface.addLayeredComponent(
								new TextInputWindow(
									WIDTH / 2 - 125,
									HEIGHT / 2 + 100,
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
											UserInterface.addMessage("Error", "Cannot recognise " + args[0].toString() + " as a quantity.");
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
					isItemAvailableToTrade(entry.getKey()) ? Colors.UI_GRAY : Colors.UI_DARKER_GRAY,
					isItemAvailableToTrade(entry.getKey()) ? Color.GREEN : Colors.UI_DARKER_GRAY,
					isItemAvailableToTrade(entry.getKey()) ? Color.WHITE : Colors.UI_DARKER_GRAY,
					UIRef.BL
				),
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
	protected boolean isItemAvailableToTrade(Item item) {
		return true; // by default
	}


	private void changeList(final Item key, int numberToChange, final HashMap<ListingMenuItem<Item>, Integer> transferTo, final HashMap<ListingMenuItem<Item>, Integer> transferFrom, final boolean toTrade) {
		final ListingMenuItem<Item> listingMenuItem = new ListingMenuItem<Item>(
			key,
			new Button(
				key.getSingular(true),
				defaultFont,
				0,
				0,
				key.getSingular(true).length() * 10,
				16,
				() -> {
					if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
						UserInterface.addLayeredComponent(
							new TextInputWindow(
								WIDTH / 2 - 125,
								HEIGHT / 2 + 100,
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
										UserInterface.addMessage("Error", "Cannot recognise " + args[0].toString() + " as a quantity.");
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
				toTrade ? Colors.UI_GRAY : Colors.UI_DARK_ORANGE,
				toTrade ? Color.GREEN : Color.ORANGE,
				Color.WHITE,
				UIRef.BL
			),
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
			if (proposee instanceof Individual) {
				if (((Individual) proposee).getState().position.cpy().sub(((Individual) proposer).getState().position.cpy()).len() > 64) {
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
				tradeButton.text = "Trade Rejected";
				tradeButton.setIdleColor(Color.RED);
			} else {
				if (proposee instanceof Prop) {
					tradeButton.text = "Transfer";
				} else {
					tradeButton.text = "Propose Trade";
				}
				tradeRejectionTimer = 1f;
				rejected = false;
				tradeButton.setIdleColor(Color.GREEN);
			}
		}

		tradeButton.render(
			x + width/2,
			y - height + 40,
			tradeButtonClickable(),
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
	protected void renderListingPanels() {
		int lineWidth = 23;

		proposerPanel.x = x;
		proposerPanel.y = y - (proposerItemsToTrade.isEmpty() ? 0 : (1 + min(5,proposerItemsToTrade.size())) * lineWidth);
		proposerPanel.height = height - 50 - (proposerItemsToTrade.isEmpty() ? 0 : (1 + min(5, proposerItemsToTrade.size())) * lineWidth);
		proposerPanel.width = width / 2 - 10;

		proposeePanel.x = x + width / 2 + 10;
		proposeePanel.y = y - (proposeeItemsToTrade.isEmpty() ? 0 : (1 + min(5,proposeeItemsToTrade.size())) * lineWidth);
		proposeePanel.height = height - 50 - (proposeeItemsToTrade.isEmpty() ? 0 : (1 + min(5, proposeeItemsToTrade.size())) * lineWidth);
		proposeePanel.width = width / 2 - 10;

		proposerTradingPanel.x = x;
		proposerTradingPanel.y = y;
		proposerTradingPanel.height = 50 + (proposerItemsToTrade.isEmpty() ? 0 : (1 + min(5, proposerItemsToTrade.size())) * lineWidth);
		proposerTradingPanel.width = width / 2 - 10;

		proposeeTradingPanel.x = x + width / 2 + 10;
		proposeeTradingPanel.y = y;
		proposeeTradingPanel.height = 50 + (proposeeItemsToTrade.isEmpty() ? 0 : (1 + min(5, proposeeItemsToTrade.size())) * lineWidth);
		proposeeTradingPanel.width = width / 2 - 10;

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

		if (tradeButtonClickable()) {
			tradeButton.click();
		}
	}


	@Override
	public boolean scrolled(int amount) {
		return proposerTradingPanel.scrolled(amount) || proposeeTradingPanel.scrolled(amount) || proposerPanel.scrolled(amount) || proposeePanel.scrolled(amount);
	}


	@Override
	public void leftClickReleased() {
		proposerPanel.leftClickReleased();
		proposeePanel.leftClickReleased();
		proposerTradingPanel.leftClickReleased();
		proposeeTradingPanel.leftClickReleased();
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
			if (Domain.getSelectedIndividuals().contains(proposer)) {
				ClientServerInterface.SendRequest.sendClearAITaskRequest(((Individual)proposer).getId().getId());
			}
			if (proposee instanceof Individual && Domain.getSelectedIndividuals().contains(proposee)) {
				ClientServerInterface.SendRequest.sendClearAITaskRequest(((Individual)proposee).getId().getId());
			}
		}
	}


	@Override
	public Object getUniqueIdentifier() {
		return "tradeWindow" + (proposer.hashCode() + proposee.hashCode());
	}
}