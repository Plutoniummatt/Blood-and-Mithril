package bloodandmithril.control.keydown;

import com.google.common.base.Function;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.control.KeyPressedHandler;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.window.AIRoutinesWindow;
import bloodandmithril.ui.components.window.BuildWindow;
import bloodandmithril.ui.components.window.InventoryWindow;

/**
 * {@link KeyPressedHandler} to open individual {@link InventoryWindow}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class IndividualUIKeyPressedHandler implements KeyPressedHandler {

	@Inject
	private BloodAndMithrilClientInputProcessor inputProcessor;

	@Inject
	private GameClientStateTracker gameClientStateTracker;

	@Override
	public boolean handle(final int keycode) {
		if (keycode == inputProcessor.getKeyMappings().openInventory.keyCode) {
			if (gameClientStateTracker.getSelectedIndividuals().size() == 1) {
				final Individual individual = gameClientStateTracker.getSelectedIndividuals().iterator().next();
				final String simpleName = individual.getId().getSimpleName();

				UserInterface.addLayeredComponentUnique(
					new InventoryWindow(
						individual,
						simpleName + " - Inventory",
						true
					)
				);
			}
		}

		if (keycode == inputProcessor.getKeyMappings().openAIRoutines.keyCode) {
			if (gameClientStateTracker.getSelectedIndividuals().size() == 1) {
				final Individual individual = gameClientStateTracker.getSelectedIndividuals().iterator().next();
				UserInterface.addLayeredComponentUnique(
					new AIRoutinesWindow(
						individual
					)
				);
			}
		}

		if (keycode == inputProcessor.getKeyMappings().openBuildWindow.keyCode) {
			if (gameClientStateTracker.getSelectedIndividuals().size() == 1) {
				final Individual individual = gameClientStateTracker.getSelectedIndividuals().iterator().next();

				UserInterface.addLayeredComponentUnique(
					new BuildWindow(
						individual,
						new Function<Construction, String>() {
							@Override
							public String apply(final Construction input) {
								return input.getTitle();
							}
						},
						(c1, c2) -> {
							return c1.getTitle().compareTo(c2.getTitle());
						}
					)
				);
			}
		}

		return false;
	}
}