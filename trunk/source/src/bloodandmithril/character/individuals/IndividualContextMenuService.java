package bloodandmithril.character.individuals;

import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.networking.ClientServerInterface.isServer;
import bloodandmithril.character.ai.task.Attack;
import bloodandmithril.character.ai.task.Follow;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.networking.ClientServerInterface;
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

import com.badlogic.gdx.graphics.Color;
import com.google.common.base.Function;

public class IndividualContextMenuService {

	public static ContextMenu getContextMenu(Individual indi) {
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
			actionMenu
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
			interactMenu
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
			editMenu
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

		for (MenuItem item : indi.internalGetContextMenuItems()) {
			contextMenuToReturn.addMenuItem(item);
		}

		return contextMenuToReturn;
	}


	private static ContextMenu actions(Individual indi) {
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


	private static MenuItem walkRun(Individual indi) {
		return new MenuItem(
			indi.isWalking() ? "Run" : "Walk",
			() -> {
				if (ClientServerInterface.isServer()) {
					indi.setWalking(!indi.isWalking());
				} else {
					ClientServerInterface.SendRequest.sendRunWalkRequest(indi.getId().getId(), !indi.isWalking());
				}
			},
			Color.WHITE,
			indi.getToolTipTextColor(),
			Color.GRAY,
			null
		);
	}


	private static ContextMenu interactMenu(Individual indi) {
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


	private static MenuItem shutUpSpeak(Individual indi) {
		return new MenuItem(
			indi.isShutUp() ? "Speak" : "Shut up",
			() -> {
				if (isServer()) {
					if (!indi.isShutUp()) {
						indi.speak("Fine...", 1000);
						indi.setShutUp(true);
					} else {
						indi.setShutUp(false);
						indi.speak("Yay!", 1000);
					}
				} else {
					ClientServerInterface.SendRequest.sendIndividualSpeakRequest(indi, !indi.isShutUp());
				}
			},
			Color.WHITE,
			indi.getToolTipTextColor(),
			Color.GRAY,
			null
		);
	}



	private static MenuItem skills(Individual indi) {
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


	private static MenuItem aiRoutines(Individual indi) {
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


	private static MenuItem suppressAI(Individual indi) {
		return new MenuItem(
				indi.isAISuppressed() ? "Enable AI" : "Disable AI",
			() -> {
				if (ClientServerInterface.isServer()) {
					indi.setAISuppression(!indi.isAISuppressed());
				} else {
					ClientServerInterface.SendRequest.sendAISuppressionRequest(indi, !indi.isAISuppressed());
				}
			},
			Color.WHITE,
			indi.getToolTipTextColor(),
			Color.GRAY,
			null
		);
	}


	private static MenuItem showStatus(Individual indi) {
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


	private static MenuItem follow(final Individual individual) {
		return new MenuItem(
			"Follow",
			() -> {
				for (Individual indi : Domain.getSelectedIndividuals()) {
					if (indi != individual) {
						if (isServer()) {
							indi.getAI().setCurrentTask(
								new Follow(indi, individual, 10, null)
							);
						} else {
							ClientServerInterface.SendRequest.sendFollowRequest(indi, individual);
						}
					}
				}
			},
			Color.WHITE,
			individual.getToolTipTextColor(),
			Color.GRAY,
			null
		);
	}


	private static MenuItem attackMenuItem(final Individual individual) {
		return new MenuItem(
			"Attack",
				() -> {
					for (Individual indi : Domain.getSelectedIndividuals()) {
						if (indi != individual) {
							if (isServer()) {
								indi.getAI().setCurrentTask(
									new Attack(indi, individual)
								);
							} else {
								ClientServerInterface.SendRequest.sendRequestAttack(indi, individual);
							}
						}
					}
				},
			Color.RED,
			individual.getToolTipTextColor(),
			Colors.UI_DARK_ORANGE,
			null
		);
	}


	private static MenuItem trade(final Individual individual) {
		return new MenuItem(
			individual.isAlive() ? "Trade with" : "Loot",
			() -> {
				if (Domain.getSelectedIndividuals().size() > 1) {
					return;
				}

				for (Individual indi : Domain.getSelectedIndividuals()) {
					if (isServer()) {
						if (indi != individual) {
							indi.getAI().setCurrentTask(
								new TradeWith(indi, individual)
							);
						}
					} else {
						ClientServerInterface.SendRequest.sendTradeWithIndividualRequest(indi, individual);
					}
				}
			},
			Color.WHITE,
			individual.getToolTipTextColor(),
			Color.GRAY,
			new ContextMenu(0, 0,
				true,
				new MenuItem(
					"You have multiple individuals selected",
					() -> {},
					Colors.UI_GRAY,
					Colors.UI_GRAY,
					Colors.UI_GRAY,
					null
				)
			),
			() -> {
				return Domain.getSelectedIndividuals().size() > 1;
			}
		);
	}


	private static MenuItem inventory(Individual indi) {
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


	private static MenuItem build(Individual indi) {
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


	private static ContextMenu editSubMenu(Individual indi) {
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
								if (isServer()) {
									indi.getId().setNickName(args[0].toString());
								} else {
									ClientServerInterface.SendRequest.sendChangeNickNameRequest(indi.getId().getId(), args[0].toString());
								}
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
								if (isServer()) {
									indi.updateDescription(args[0].toString());
								} else {
									ClientServerInterface.SendRequest.sendUpdateBiographyRequest(indi, args[0].toString());
								}
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
	private static MenuItem showInfo(Individual indi) {
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
	private static MenuItem selectDeselect(final Individual indi) {
		return Domain.isIndividualSelected(indi) ?
		new MenuItem(
			"Deselect",
			() -> {
				if (isServer()) {
					indi.deselect(false, 0);
					Domain.removeSelectedIndividual(indi);
					indi.clearCommands();
				} else {
					ClientServerInterface.SendRequest.sendIndividualSelectionRequest(indi.getId().getId(), false);
				}
			},
			Color.WHITE,
			indi.getToolTipTextColor(),
			Color.GRAY,
			null
		) :

		new MenuItem(
			"Select",
			() -> {
				if (isServer()) {
					indi.select(0);
				} else {
					ClientServerInterface.SendRequest.sendIndividualSelectionRequest(indi.getId().getId(), true);
				}
			},
			Color.WHITE,
			indi.getToolTipTextColor(),
			Color.GRAY,
			null
		);
	}
}