package bloodandmithril.ui.components.window;

import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.Container;
import bloodandmithril.item.Item;
import bloodandmithril.item.TradeService;
import bloodandmithril.prop.building.ConstructionWithContainer.ConstructionContainer;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Task;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.GameWorld;

import com.badlogic.gdx.graphics.Color;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Trade window, used when transferring items between {@link Container}s
 *
 * @author Matt
 */
public class TradeWindow extends Window {

	/** Panels of involved traders */
	protected ScrollableListingPanel<Item> proposerPanel;
	protected ScrollableListingPanel<Item> proposeePanel;

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
	public TradeWindow(int x, int y, int length, int height, String title, boolean active, int minLength, int minHeight, Container proposer, Container proposee) {
		super(x, y, length, height, title, active, minLength, minHeight, false, true);

		this.proposer = proposer;
		this.proposee = proposee;

		populate(proposerItemsToTrade, proposerItemsNotToTrade, proposer.getInventory());
		populate(proposeeItemsToTrade, proposeeItemsNotToTrade, proposee.getInventory());

		if (proposee instanceof ConstructionContainer) {
			tradeButton.text = "Transfer Items";
		}

		createPanels();
	}


	/**
	 * Called when the "Propose Trade" button is clicked.
	 */
	private void proposeTrade() {

		boolean proposalAccepted = false;

		if (proposee instanceof ConstructionContainer) {
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
				for (Component component : UserInterface.layeredComponents) {
					if (component instanceof InventoryWindow) {
						((InventoryWindow) component).refresh();
					} else if (component instanceof TradeWindow) {
						((TradeWindow) component).refresh();
					}
				}
			}

			refresh();
		} else {
			rejectTradeProposal();
		}
	}


	/**
	 * Refreshes this window and syncs it with the trader inventories
	 */
	@SuppressWarnings("unchecked")
	public synchronized void refresh() {
		proposer = GameWorld.individuals.get(((Individual) proposer).getId().getId());

		proposerItemsToTrade.clear();
		proposeeItemsToTrade.clear();
		proposerItemsNotToTrade.clear();
		proposeeItemsNotToTrade.clear();

		populate(proposerItemsToTrade, proposerItemsNotToTrade, proposer.getInventory());
		populate(proposeeItemsToTrade, proposeeItemsNotToTrade, proposee.getInventory());

		proposerPanel.refresh(Lists.newArrayList(proposerItemsToTrade, proposerItemsNotToTrade));
		proposeePanel.refresh(Lists.newArrayList(proposeeItemsToTrade, proposeeItemsNotToTrade));
	}


	/**
	 * The proposee has rejected the proposers proposal.
	 */
	private void rejectTradeProposal() {
		rejected = true;
	}


	private void createPanels() {
		proposerPanel = new ScrollableListingPanel<Item>(this) {

			@Override
			protected void onSetup(List<HashMap<ListingMenuItem<Item>, Integer>> listings) {
				listings.add(proposerItemsToTrade);
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

		proposeePanel = new ScrollableListingPanel<Item>(this) {

			@Override
			protected void onSetup(List<HashMap<ListingMenuItem<Item>, Integer>> listings) {
				listings.add(proposeeItemsToTrade);
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
	}


	/**
	 * Populates a listing
	 */
	private void populate(final HashMap<ListingMenuItem<Item>, Integer> trading, final HashMap<ListingMenuItem<Item>, Integer> notTrading, HashMap<Item, Integer> toPopulateFrom) {
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
					new Task() {
						@Override
						public void execute() {
							changeList(entry.getKey(), trading, notTrading, false);
						}
					},
					Colors.UI_GRAY,
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


	private void changeList(final Item key, final HashMap<ListingMenuItem<Item>, Integer> transferTo, final HashMap<ListingMenuItem<Item>, Integer> transferFrom, final boolean toTrade) {
		final ListingMenuItem<Item> listingMenuItem = new ListingMenuItem<Item>(
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
				toTrade ? Colors.UI_GRAY : Colors.UI_DARK_ORANGE,
				toTrade ? Color.GREEN : Color.ORANGE,
				Color.WHITE,
				UIRef.BL
			),
			null
		);

		for (Entry<ListingMenuItem<Item>, Integer> entry : Lists.newArrayList(transferFrom.entrySet())) {
			if (entry.getKey().t.sameAs(key)) {
				if (transferFrom.get(entry.getKey()) == 1) {
					transferFrom.remove(entry.getKey());
				} else {
					transferFrom.put(entry.getKey(), transferFrom.get(entry.getKey()) - 1);
				}

				boolean found = false;
				for (Entry<ListingMenuItem<Item>, Integer> innerEntry : Lists.newArrayList(transferTo.entrySet())) {
					if (innerEntry.getKey().t.sameAs(key)) {
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

		if (proposer instanceof Individual) {
			if (proposee instanceof Individual) {
				if (((Individual) proposee).getState().position.cpy().sub(((Individual) proposer).getState().position.cpy()).len() > 64) {
					setClosing(true);
				}
			} else if (proposee instanceof ConstructionContainer) {
				if (((ConstructionContainer) proposee).getPositionOfChest().cpy().sub(((Individual) proposer).getState().position.cpy()).len() > 64) {
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
				if (proposee instanceof ConstructionContainer) {
					tradeButton.text = "Transfer";
				} else {
					tradeButton.text = "Propose Trade";
				}
				tradeRejectionTimer = 1f;
				rejected = false;
				tradeButton.setIdleColor(Color.GREEN);
			}
		}

		boolean isTradeButtonClickable = isActive() && !rejected && (!proposeeItemsToTrade.isEmpty() || !proposerItemsToTrade.isEmpty());

		tradeButton.render(
			x + width/2,
			y - height + 40,
			isTradeButtonClickable,
			getAlpha()
		);
	}


	protected boolean isProposeeItemsEmpty() {
		return proposeeItemsToTrade.isEmpty() && !proposeeItemsNotToTrade.isEmpty();
	}


	/**
	 * Renders the listing panels
	 */
	protected void renderListingPanels() {
		proposerPanel.x = x;
		proposerPanel.y = y;
		proposerPanel.height = height - 50;
		proposerPanel.width = width / 2 - 10;

		proposeePanel.x = x + width / 2 + 10;
		proposeePanel.y = y;
		proposeePanel.height = height - 50;
		proposeePanel.width = width / 2 - 10;

		proposerPanel.render();
		proposeePanel.render();
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		proposerPanel.leftClick(copy, windowsCopy);
		proposeePanel.leftClick(copy, windowsCopy);
		tradeButton.click();
	}


	@Override
	public void leftClickReleased() {
		proposerPanel.leftClickReleased();
		proposeePanel.leftClickReleased();
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
			if (GameWorld.selectedIndividuals.contains(proposer)) {
				ClientServerInterface.SendRequest.sendClearAITaskRequest(((Individual)proposer).getId().getId());
			}
			if (proposee instanceof Individual && GameWorld.selectedIndividuals.contains(proposee)) {
				ClientServerInterface.SendRequest.sendClearAITaskRequest(((Individual)proposee).getId().getId());
			}
		}
	}


	@Override
	public boolean keyPressed(int keyCode) {
		return false;
	}
}