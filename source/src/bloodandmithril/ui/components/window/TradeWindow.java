package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.item.Container;
import bloodandmithril.item.Item;
import bloodandmithril.item.TradeProposalEvaluator;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Task;

import com.badlogic.gdx.graphics.Color;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Trade window, used when transferring items between {@link Container}s
 *
 * @author Matt
 */
public class TradeWindow extends Window {

	private ScrollableListingPanel buyerPanel;
	private ScrollableListingPanel sellerPanel;

	private final HashMap<ListingMenuItem, Integer> proposerItemsToTrade = Maps.newHashMap();
	private final HashMap<ListingMenuItem, Integer> proposerItemsNotToTrade = Maps.newHashMap();

	private final HashMap<ListingMenuItem, Integer> proposeeItemsToTrade = Maps.newHashMap();
	private final HashMap<ListingMenuItem, Integer> proposeeItemsNotToTrade = Maps.newHashMap();

	private final Container proposer, proposee;

	private boolean rejected = false;

	private float tradeRejectionTimer = 1f;

	private final Button tradeButton = new Button(
		"Propose Trade",
		defaultFont,
		0,
		0,
		130,
		16,
		new Task() {
			@Override
			public void execute() {
				proposeTrade();
			}
		},
		Color.GREEN,
		Color.ORANGE,
		Color.WHITE,
		UIRef.BL
	);

	/**
	 * Constructor
	 */
	public TradeWindow(int x, int y, int length, int height, String title, boolean active, int minLength, int minHeight, boolean minimizable, Container proposer, Container proposee) {
		super(x, y, length, height, title, active, minLength, minHeight, minimizable);

		this.proposer = proposer;
		this.proposee = proposee;

		populate(proposerItemsToTrade, proposerItemsNotToTrade, proposer.getInventory());
		populate(proposeeItemsToTrade, proposeeItemsNotToTrade, proposee.getInventory());

		createPanels();
	}


	/**
	 * Called when the "Propose Trade" button is clicked.
	 */
	private void proposeTrade() {

		boolean proposalAccepted = false;

		if (proposer instanceof Individual && proposee instanceof Individual) {
			proposalAccepted = TradeProposalEvaluator.evaluate(
				(Individual)proposer,
				Lists.transform(Lists.newArrayList(proposerItemsToTrade.entrySet()), new Function<Entry<ListingMenuItem, Integer>, Item>() {
					@Override
					public Item apply(Entry<ListingMenuItem, Integer> input) {
						return input.getKey().item;
					}
				}),
				(Individual)proposee,
				Lists.transform(Lists.newArrayList(proposeeItemsToTrade.entrySet()), new Function<Entry<ListingMenuItem, Integer>, Item>() {
					@Override
					public Item apply(Entry<ListingMenuItem, Integer> input) {
						return input.getKey().item;
					}
				})
			);
		}

		if (proposalAccepted) {
			finalizeTrade();
			refresh();
		} else {
			rejectTradeProposal();
		}
	}


	/**
	 * Refreshes this window and syncs it with the trader inventories
	 */
	private void refresh() {
		proposerItemsToTrade.clear();
		proposeeItemsToTrade.clear();
		proposerItemsNotToTrade.clear();
		proposeeItemsNotToTrade.clear();

		populate(proposerItemsToTrade, proposerItemsNotToTrade, proposer.getInventory());
		populate(proposeeItemsToTrade, proposeeItemsNotToTrade, proposee.getInventory());
	}


	/**
	 * The proposee has rejected the proposers proposal.
	 */
	private void rejectTradeProposal() {
		rejected = true;
	}


	/**
	 * The trade proposal was accepted by the proposee, this method transfers the {@link Item}s and finalizes the trade
	 */
	private void finalizeTrade() {
		for (Entry<ListingMenuItem, Integer> proposerToTradeItem : proposerItemsToTrade.entrySet()) {
			proposer.takeItem(proposerToTradeItem.getKey().item, proposerToTradeItem.getValue());
			proposee.giveItem(proposerToTradeItem.getKey().item, proposerToTradeItem.getValue());
		}
		for (Entry<ListingMenuItem, Integer> proposeeToTradeItem : proposeeItemsToTrade.entrySet()) {
			proposee.takeItem(proposeeToTradeItem.getKey().item, proposeeToTradeItem.getValue());
			proposer.giveItem(proposeeToTradeItem.getKey().item, proposeeToTradeItem.getValue());
		}
	}


	private void createPanels() {
		buyerPanel = new ScrollableListingPanel(this) {

			@Override
			protected void onSetup(List<HashMap<ListingMenuItem, Integer>> listings) {
				listings.add(proposerItemsToTrade);
				listings.add(proposerItemsNotToTrade);
			}

			@Override
			protected int getExtraStringOffset() {
				return 80;
			}

			@Override
			protected String getExtraString(Entry<ListingMenuItem, Integer> item) {
				return Integer.toString(item.getValue());
			}
		};

		sellerPanel = new ScrollableListingPanel(this) {

			@Override
			protected void onSetup(List<HashMap<ListingMenuItem, Integer>> listings) {
				listings.add(proposeeItemsToTrade);
				listings.add(proposeeItemsNotToTrade);
			}

			@Override
			protected int getExtraStringOffset() {
				return 80;
			}

			@Override
			protected String getExtraString(Entry<ListingMenuItem, Integer> item) {
				return Integer.toString(item.getValue());
			}
		};
	}


	/**
	 * Populates a listing
	 */
	private void populate(final HashMap<ListingMenuItem, Integer> trading, final HashMap<ListingMenuItem, Integer> notTrading, HashMap<Item, Integer> toPopulateFrom) {
		for (final Entry<Item, Integer> entry : toPopulateFrom.entrySet()) {

			final ListingMenuItem listingMenuItem = new ListingMenuItem(
				entry.getKey(),
				new Button(
					entry.getKey().getSingular(true),
					defaultFont,
					0,
					0,
					entry.getKey().getSingular(true).length() * 10,
					16,
					new Task() {
						@Override
						public void execute() {
							changeList(entry.getKey(), trading, notTrading, false);
						}
					},
					new Color(0.8f, 0.8f, 0.8f, 1f),
					Color.GREEN,
					Color.WHITE,
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


	private void changeList(final Item key, final HashMap<ListingMenuItem, Integer> transferTo, final HashMap<ListingMenuItem, Integer> transferFrom, final boolean toTrade) {
		final ListingMenuItem listingMenuItem = new ListingMenuItem(
			key,
			new Button(
				key.getSingular(true),
				defaultFont,
				0,
				0,
				key.getSingular(true).length() * 10,
				16,
				new Task() {
					@Override
					public void execute() {
						changeList(key, transferFrom, transferTo, !toTrade);
					}
				},
				toTrade ? new Color(0.8f, 0.8f, 0.8f, 1f) : new Color(0.8f, 0.6f, 0.0f, 1f),
				toTrade ? Color.GREEN : Color.ORANGE,
				Color.WHITE,
				UIRef.BL
			),
			null
		);

		for (Entry<ListingMenuItem, Integer> entry : Lists.newArrayList(transferFrom.entrySet())) {
			if (entry.getKey().item.sameAs(key)) {
				if (transferFrom.get(entry.getKey()) == 1) {
					transferFrom.remove(entry.getKey());
				} else {
					transferFrom.put(entry.getKey(), transferFrom.get(entry.getKey()) - 1);
				}

				boolean found = false;
				for (Entry<ListingMenuItem, Integer> innerEntry : Lists.newArrayList(transferTo.entrySet())) {
					if (innerEntry.getKey().item.sameAs(key)) {
						transferTo.remove(innerEntry.getKey());
						transferTo.put(listingMenuItem, innerEntry.getValue() + 1);
						found = true;
						break;
					}
				}
				if (!found) {
					transferTo.put(listingMenuItem, 1);
				}

				break;
			}
		}
	}


	@Override
	protected void internalWindowRender() {
		buyerPanel.x = x;
		buyerPanel.y = y;
		buyerPanel.height = height - 50;
		buyerPanel.width = length / 2 - 10;

		sellerPanel.x = x + length / 2 + 10;
		sellerPanel.y = y;
		sellerPanel.height = height - 50;
		sellerPanel.width = length / 2 - 10;

		buyerPanel.render();
		sellerPanel.render();

		if (rejected) {
			if (tradeRejectionTimer > 0f) {
				tradeRejectionTimer = tradeRejectionTimer - 0.01f;
				tradeButton.text = "Trade Rejected";
				tradeButton.setIdleColor(Color.RED);
			} else {
				tradeButton.text = "Propose Trade";
				tradeRejectionTimer = 1f;
				rejected = false;
				tradeButton.setIdleColor(Color.GREEN);
			}
		}

		boolean isTradeButtonClickable = active && !rejected && (!proposeeItemsToTrade.isEmpty() || !proposerItemsToTrade.isEmpty());

		tradeButton.render(
			x + length/2,
			y - height + 40,
			isTradeButtonClickable,
			alpha
		);
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		buyerPanel.leftClick(copy, windowsCopy);
		sellerPanel.leftClick(copy, windowsCopy);
		tradeButton.click();
	}


	@Override
	public void leftClickReleased() {
		buyerPanel.leftClickReleased();
		sellerPanel.leftClickReleased();
	}


	@Override
	protected void uponClose() {
		if (proposer instanceof Individual) {
			((Individual) proposer).ai.setCurrentTask(new Idle());
		}
		if (proposee instanceof Individual) {
			((Individual) proposee).ai.setCurrentTask(new Idle());
		}
	}
}