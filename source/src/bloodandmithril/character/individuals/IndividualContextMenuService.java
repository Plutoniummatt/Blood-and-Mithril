package bloodandmithril.character.individuals;

import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;

import com.badlogic.gdx.graphics.Color;
import com.google.common.base.Function;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
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
import bloodandmithril.world.Domain;

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

	public ContextMenu getContextMenu(Individual indi) {
		MenuItem showInfoMenuItem = showInfo(indi);
		MenuItem showStatusWindowItem = showStatus(indi);

		final ContextMenu actionMenu = actions(indi);
		MenuItem actions = new MenuItem(
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
		MenuItem interact = new MenuItem(
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
		MenuItem edit = new MenuItem(
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

		ContextMenu contextMenuToReturn = new ContextMenu(0, 0, true);
		if (!Domain.getSelectedIndividuals().isEmpty() && !(Domain.getSelectedIndividuals().size() == 1 && Domain.isIndividualSelected(indi))) {
			contextMenuToReturn.addMenuItem(interact);
		}

		contextMenuToReturn.addMenuItem(showInfoMenuItem);
		contextMenuToReturn.addMenuItem(showStatusWindowItem);

		if (indi.isControllable() && indi.isAlive()) {
			contextMenuToReturn.addMenuItem(inventory(indi));
			contextMenuToReturn.addMenuItem(skills(indi));
			contextMenuToReturn.addMenuItem(actions);
			contextMenuToReturn.addMenuItem(edit);
		}

		return contextMenuToReturn;
	}


	private ContextMenu actions(Individual indi) {
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


	private MenuItem walkRun(Individual indi) {
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


	private ContextMenu interactMenu(Individual indi) {
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


	private MenuItem shutUpSpeak(Individual indi) {
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



	private MenuItem skills(Individual indi) {
		return new MenuItem(
			"Proficiencies",
			() -> {
				UserInterface.addLayeredComponentUnique(
					new ProficienciesWindow(indi)
				);
			},
			Color.WHITE,
			indi.getToolTipTextColor(),
			Color.GRAY,
			null
		);
	}


	private MenuItem aiRoutines(Individual indi) {
		return new MenuItem(
			"AI Routines",
			() -> {
				UserInterface.addLayeredComponentUnique(
					new AIRoutinesWindow(indi)
				);
			},
			Color.WHITE,
			indi.getToolTipTextColor(),
			Color.GRAY,
			null
		);
	}


	private MenuItem suppressAI(Individual indi) {
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


	private MenuItem showStatus(Individual indi) {
		return new MenuItem(
			"Show status",
			() -> {
				UserInterface.addLayeredComponentUnique(
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
				for (Individual indi : Domain.getSelectedIndividuals()) {
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
					for (Individual indi : Domain.getSelectedIndividuals()) {
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
				if (Domain.getSelectedIndividuals().size() > 1) {
					return;
				}

				for (Individual selected : Domain.getSelectedIndividuals()) {
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
				return Domain.getSelectedIndividuals().size() > 1;
			}
		);
	}


	private MenuItem inventory(Individual indi) {
		return new MenuItem(
			"Inventory",
			() -> {
				InventoryWindow inventoryWindow = new InventoryWindow(
					indi,
					indi.getId().getSimpleName() + " - Inventory",
					true
				);
				UserInterface.addLayeredComponentUnique(inventoryWindow);
			},
			Color.WHITE,
			indi.getToolTipTextColor(),
			Color.GRAY,
			null
		);
	}


	private MenuItem build(Individual indi) {
		return new MenuItem(
			"Build",
			() -> {
				BuildWindow window = new BuildWindow(
					indi,
					new Function<Construction, String>() {
						@Override
						public String apply(Construction input) {
							return input.getTitle();
						}
					},
					(c1, c2) -> {
						return c1.getTitle().compareTo(c2.getTitle());
					}
				);

				UserInterface.addLayeredComponentUnique(window);
			},
			Color.WHITE,
			indi.getToolTipTextColor(),
			Color.GRAY,
			null
		);
	}


	private ContextMenu editSubMenu(Individual indi) {
		return new ContextMenu(0, 0,
			true,
			new MenuItem(
				"Change nickname",
				() -> {
					UserInterface.addLayeredComponent(
						new TextInputWindow(
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
					UserInterface.addLayeredComponent(
						new TextInputWindow(
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
	private MenuItem showInfo(Individual indi) {
		return new MenuItem(
			"Show info",
			() -> {
				IndividualInfoWindow individualInfoWindow = new IndividualInfoWindow(
					indi,
					300,
					320,
					indi.getId().getSimpleName() + " - Info",
					true,
					250, 200
				);
				UserInterface.addLayeredComponentUnique(individualInfoWindow);
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
		return Domain.isIndividualSelected(indi) ?
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
				individualSelectionService.select(indi);
			},
			Color.WHITE,
			indi.getToolTipTextColor(),
			Color.GRAY,
			null
		);
	}
}