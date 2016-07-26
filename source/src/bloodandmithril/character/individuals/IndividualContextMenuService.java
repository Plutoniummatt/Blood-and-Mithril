package bloodandmithril.character.individuals;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;

import com.badlogic.gdx.graphics.Color;
import com.google.common.base.Function;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.faction.FactionControlService;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.playerinteraction.individual.api.IndividualAISupressionService;
import bloodandmithril.playerinteraction.individual.api.IndividualAttackOtherService;
import bloodandmithril.playerinteraction.individual.api.IndividualChangeNicknameService;
import bloodandmithril.playerinteraction.individual.api.IndividualFollowOtherService;
import bloodandmithril.playerinteraction.individual.api.IndividualSelectionService;
import bloodandmithril.playerinteraction.individual.api.IndividualToggleSpeakingService;
import bloodandmithril.playerinteraction.individual.api.IndividualTradeWithOtherService;
import bloodandmithril.playerinteraction.individual.api.IndividualUpdateDescriptionService;
import bloodandmithril.playerinteraction.individual.api.IndividualWalkRunToggleService;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.AIRoutinesWindow;
import bloodandmithril.ui.components.window.BuildWindow;
import bloodandmithril.ui.components.window.IndividualInfoWindow;
import bloodandmithril.ui.components.window.IndividualStatusWindow;
import bloodandmithril.ui.components.window.InventoryWindow;
import bloodandmithril.ui.components.window.ProficienciesWindow;
import bloodandmithril.ui.components.window.TextInputWindow;
import bloodandmithril.util.Util.Colors;

/**
 * Service for providing context menus for individuals
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2015")
public class IndividualContextMenuService {

	@Inject	private IndividualSelectionService individualSelectionService;
	@Inject private IndividualAISupressionService individualAISupressionService;
	@Inject private IndividualWalkRunToggleService individualWalkRunToggleService;
	@Inject private IndividualToggleSpeakingService individualToggleSpeakingService;
	@Inject private IndividualTradeWithOtherService individualTradeWithOtherService;
	@Inject private IndividualFollowOtherService individualFollowOtherService;
	@Inject private IndividualAttackOtherService individualAttackOtherService;
	@Inject private IndividualChangeNicknameService individualChangeNicknameService;
	@Inject private IndividualUpdateDescriptionService individualUpdateDescriptionService;
	@Inject private FactionControlService factionControlService;
	@Inject private GameClientStateTracker gameClientStateTracker;
	@Inject private UserInterface userInterface;

	public ContextMenu getContextMenu(final Individual indi) {
		final MenuItem camFollow = camFollow(indi);
		final MenuItem showInfoMenuItem = showInfo(indi);
		final MenuItem showStatusWindowItem = showStatus(indi);

		final ContextMenu actionMenu = actions(indi);
		final MenuItem actions = new MenuItem(
			"Actions",
			() -> {
				actionMenu.x = getMouseScreenX();
				actionMenu.y = getMouseScreenY();
			},
			Color.ORANGE,
			indi.getToolTipTextColor(),
			Color.GRAY,
			() -> { return actionMenu; }
		);

		final ContextMenu interactMenu = interactMenu(indi);
		final MenuItem interact = new MenuItem(
			"Interact",
			() -> {
				interactMenu.x = getMouseScreenX();
				interactMenu.y = getMouseScreenY();
			},
			Color.GREEN,
			Colors.UI_DARK_GREEN,
			Color.GRAY,
			() -> { return interactMenu; }
		);

		final ContextMenu editMenu = editSubMenu(indi);
		final MenuItem edit = new MenuItem(
			"Edit",
			() -> {
				editMenu.x = getMouseScreenX();
				editMenu.y = getMouseScreenY();
			},
			Color.ORANGE,
			indi.getToolTipTextColor(),
			Color.GRAY,
			() -> { return editMenu; }
		);

		final ContextMenu contextMenuToReturn = new ContextMenu(0, 0, true);
		if (!gameClientStateTracker.getSelectedIndividuals().isEmpty() && !(gameClientStateTracker.getSelectedIndividuals().size() == 1 && gameClientStateTracker.isIndividualSelected(indi))) {
			contextMenuToReturn.addMenuItem(interact);
		}

		contextMenuToReturn.addMenuItem(camFollow);
		contextMenuToReturn.addMenuItem(showInfoMenuItem);
		contextMenuToReturn.addMenuItem(showStatusWindowItem);

		if (factionControlService.isControllable(indi) && indi.isAlive()) {
			contextMenuToReturn.addMenuItem(inventory(indi));
			contextMenuToReturn.addMenuItem(skills(indi));
			contextMenuToReturn.addMenuItem(actions);
			contextMenuToReturn.addMenuItem(edit);
		}

		return contextMenuToReturn;
	}


	private ContextMenu actions(final Individual indi) {
		return new ContextMenu(0, 0,
			true,
			selectDeselect(indi),
			aiRoutines(indi),
			suppressAI(indi),
			walkRun(indi),
			shutUpSpeak(indi),
			build(indi)
		);
	}


	private MenuItem walkRun(final Individual indi) {
		return new MenuItem(
			indi.isWalking() ? "Run" : "Walk",
			() -> {
				individualWalkRunToggleService.setWalking(indi, !indi.isWalking());
			},
			Color.WHITE,
			indi.getToolTipTextColor(),
			Color.GRAY,
			null
		);
	}


	private ContextMenu interactMenu(final Individual indi) {
		if (indi.isAlive()) {
			return new ContextMenu(0, 0,
				true,
				trade(indi),
				follow(indi),
				attackMenuItem(indi)
			);
		} else {
			return new ContextMenu(0, 0,
				true,
				trade(indi)
			);
		}
	}


	private MenuItem shutUpSpeak(final Individual indi) {
		return new MenuItem(
			indi.isShutUp() ? "Speak" : "Shut up",
			() -> {
				individualToggleSpeakingService.setSpeaking(indi, !indi.isShutUp());
			},
			Color.WHITE,
			indi.getToolTipTextColor(),
			Color.GRAY,
			null
		);
	}



	private MenuItem skills(final Individual indi) {
		return new MenuItem(
			"Proficiencies",
			() -> {
				userInterface.addLayeredComponentUnique(
					new ProficienciesWindow(indi)
				);
			},
			Color.WHITE,
			indi.getToolTipTextColor(),
			Color.GRAY,
			null
		);
	}


	private MenuItem aiRoutines(final Individual indi) {
		return new MenuItem(
			"AI Routines",
			() -> {
				userInterface.addLayeredComponentUnique(
					new AIRoutinesWindow(indi)
				);
			},
			Color.WHITE,
			indi.getToolTipTextColor(),
			Color.GRAY,
			null
		);
	}


	private MenuItem suppressAI(final Individual indi) {
		return new MenuItem(
				indi.isAISuppressed() ? "Enable AI" : "Disable AI",
			() -> {
				individualAISupressionService.setAIsupression(indi, !indi.isAISuppressed());
			},
			Color.WHITE,
			indi.getToolTipTextColor(),
			Color.GRAY,
			null
		);
	}


	private MenuItem showStatus(final Individual indi) {
		return new MenuItem(
			"Show status",
			() -> {
				userInterface.addLayeredComponentUnique(
					new IndividualStatusWindow(
						indi,
						400,
						400,
						indi.getId().getSimpleName() + " - Status",
						true
					)
				);
			},
			Color.WHITE,
			indi.getToolTipTextColor(),
			Color.GRAY,
			null
		);
	}


	private MenuItem follow(final Individual individual) {
		return new MenuItem(
			"Follow",
			() -> {
				for (final Individual indi : gameClientStateTracker.getSelectedIndividuals()) {
					if (indi != individual) {
						individualFollowOtherService.follow(indi, individual, 10, null);
					}
				}
			},
			Color.WHITE,
			individual.getToolTipTextColor(),
			Color.GRAY,
			null
		);
	}


	private MenuItem attackMenuItem(final Individual individual) {
		return new MenuItem(
			"Attack",
				() -> {
					for (final Individual indi : gameClientStateTracker.getSelectedIndividuals()) {
						if (indi != individual) {
							individualAttackOtherService.attack(indi, individual);
						}
					}
				},
			Color.RED,
			individual.getToolTipTextColor(),
			Colors.UI_DARK_ORANGE,
			null
		);
	}


	private MenuItem trade(final Individual individual) {
		return new MenuItem(
			individual.isAlive() ? "Trade with" : "Loot",
			() -> {
				if (gameClientStateTracker.getSelectedIndividuals().size() > 1) {
					return;
				}

				for (final Individual selected : gameClientStateTracker.getSelectedIndividuals()) {
					if (selected != individual) {
						individualTradeWithOtherService.tradeWith(selected, individual);
					}
				}
			},
			Color.WHITE,
			individual.getToolTipTextColor(),
			Color.GRAY,
			() -> { return new ContextMenu(0, 0,
				true,
				new MenuItem(
					"You have multiple individuals selected",
					() -> {},
					Colors.UI_GRAY,
					Colors.UI_GRAY,
					Colors.UI_GRAY,
					null
				)
			);},
			() -> {
				return gameClientStateTracker.getSelectedIndividuals().size() > 1;
			}
		);
	}


	private MenuItem inventory(final Individual indi) {
		return new MenuItem(
			"Inventory",
			() -> {
				final InventoryWindow inventoryWindow = new InventoryWindow(
					indi,
					indi.getId().getSimpleName() + " - Inventory",
					true
				);
				userInterface.addLayeredComponentUnique(inventoryWindow);
			},
			Color.WHITE,
			indi.getToolTipTextColor(),
			Color.GRAY,
			null
		);
	}


	private MenuItem build(final Individual indi) {
		return new MenuItem(
			"Build",
			() -> {
				final BuildWindow window = new BuildWindow(
					indi,
					new Function<Construction, String>() {
						@Override
						public String apply(final Construction input) {
							return input.getTitle();
						}
					},
					(c1, c2) -> {
						return c1.getTitle().compareTo(c2.getTitle());
					}
				);

				userInterface.addLayeredComponentUnique(window);
			},
			Color.WHITE,
			indi.getToolTipTextColor(),
			Color.GRAY,
			null
		);
	}


	private ContextMenu editSubMenu(final Individual indi) {
		return new ContextMenu(0, 0,
			true,
			new MenuItem(
				"Change nickname",
				() -> {
					userInterface.addLayeredComponentUnique(
						new TextInputWindow(
							"changeNickName",
							250,
							100,
							"Change nickname",
							250,
							100,
							args -> {
								individualChangeNicknameService.changeNickname(indi, args[0].toString());
							},
							"Confirm",
							true,
							indi.getId().getNickName()
						)
					);
				},
				Color.WHITE,
				indi.getToolTipTextColor(),
				Color.GRAY,
				null
			),
			new MenuItem(
				"Update biography",
				() -> {
					userInterface.addLayeredComponentUnique(
						new TextInputWindow(
							"updateBiography",
							250,
							100,
							"Change biography",
							250,
							100,
							args -> {
								individualUpdateDescriptionService.updateDescription(indi, args[0].toString());
							},
							"Confirm",
							true,
							indi.getDescription()
						)
					);
				},
				Color.WHITE,
				indi.getToolTipTextColor(),
				Color.GRAY,
				null
			)
		);
	}


	/**
	 * @return The show info {@link MenuItem} for this individual
	 */
	private MenuItem camFollow(final Individual indi) {
		return new MenuItem(
			"Follow cam",
			() -> {
				indi.followCam();
			},
			Color.WHITE,
			indi.getToolTipTextColor(),
			Color.GRAY,
			null
		);
	}


	/**
	 * @return The show info {@link MenuItem} for this individual
	 */
	private MenuItem showInfo(final Individual indi) {
		return new MenuItem(
			"Show info",
			() -> {
				final IndividualInfoWindow individualInfoWindow = new IndividualInfoWindow(
					indi,
					300,
					320,
					indi.getId().getSimpleName() + " - Info",
					true,
					250, 200
				);
				userInterface.addLayeredComponentUnique(individualInfoWindow);
			},
			Color.WHITE,
			indi.getToolTipTextColor(),
			Color.GRAY,
			null
		);
	}


	/**
	 * @return The {@link MenuItem} to select/deselect this individual
	 */
	private MenuItem selectDeselect(final Individual indi) {
		return gameClientStateTracker.isIndividualSelected(indi) ?
		new MenuItem(
			"Deselect",
			() -> {
				individualSelectionService.deselect(indi);
			},
			Color.WHITE,
			indi.getToolTipTextColor(),
			Color.GRAY,
			null
		) :

		new MenuItem(
			"Select",
			() -> {
				individualSelectionService.select(indi, ClientServerInterface.getClientID());
			},
			Color.WHITE,
			indi.getToolTipTextColor(),
			Color.GRAY,
			null
		);
	}
}