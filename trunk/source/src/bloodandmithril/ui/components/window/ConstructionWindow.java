package bloodandmithril.ui.components.window;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.List;

import bloodandmithril.character.ai.task.Construct;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.prop.construction.craftingstation.CraftingStation;
import bloodandmithril.ui.Refreshable;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.RequiredMaterialsPanel;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;

/**
 * A {@link Window} for construction
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class ConstructionWindow extends Window implements Refreshable {

	/** The construction and individual this window represents */
	private final Construction construction;
	private final Individual individual;
	private boolean enoughMaterialsToCraft;

	/** Button that ignites the furnace */
	private final Button constructButton = new Button(
		"Construct",
		defaultFont,
		0,
		0,
		90,
		16,
		() -> {
			beginConstruction(false);
		},
		Color.ORANGE,
		Color.WHITE,
		Color.GREEN,
		UIRef.BL
	);

	private Button deconstructButton= new Button(
		"Deconstruct",
		defaultFont,
		0,
		0,
		110,
		16,
		() -> {
			beginConstruction(true);
		},
		Color.ORANGE,
		Color.WHITE,
		Color.GREEN,
		UIRef.BL
	);

	private RequiredMaterialsPanel requiredMaterialsPanel;

	/**
	 * Constructor
	 */
	public ConstructionWindow(String title, boolean active, Individual individual, Construction construction) {
		super(600, 300, title, active, 600, 300, true, true, true);
		this.individual = individual;
		this.construction = construction;

		this.requiredMaterialsPanel = new RequiredMaterialsPanel(
			this,
			individual,
			construction.getRequiredMaterials()
		);

		refresh();
	}


	private void beginConstruction(boolean deconstruct) {
		refresh();
		if (ClientServerInterface.isServer()) {
			individual.getAI().setCurrentTask(new Construct(individual, construction, deconstruct));
		} else {
			ClientServerInterface.SendRequest.sendConstructRequest(individual.getId().getId(), construction.id, deconstruct);
		}
	}


	@Override
	protected void internalWindowRender() {
		if (individual.getState().position.cpy().sub(construction.position.cpy()).len() > 64) {
			setClosing(true);
		}

		if (!Domain.getWorld(construction.getWorldId()).props().hasProp(construction.id)) {
			setClosing(true);
		}

		requiredMaterialsPanel.x = x + 9;
		requiredMaterialsPanel.y = y - 110;
		requiredMaterialsPanel.width = width - 13;
		requiredMaterialsPanel.height = height - 110;
		requiredMaterialsPanel.render();

		constructButton.render(x + 60, y - 55, isActive() && (enoughMaterialsToCraft || construction.getConstructionProgress() != 0f) && construction.getConstructionProgress() != 1f, isActive() ? getAlpha() : getAlpha() * 0.6f);
		deconstructButton.render(x + 70, y - 74, isActive() && construction.canDeconstruct(), isActive() ? getAlpha() : getAlpha() * 0.6f);

		defaultFont.setColor(isActive() ? Colors.modulateAlpha(Color.GREEN, getAlpha()) : Colors.modulateAlpha(Color.GREEN, 0.5f * getAlpha()));
		String progress = "(" + String.format("%.1f", 100f * construction.getConstructionProgress()) + "%)";

		defaultFont.draw(spriteBatch, "Constructing: " + construction.getTitle() + " " + progress, x + 15, y - 35);
		defaultFont.draw(spriteBatch, "Required materials:", x + 15, y - 115);
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
		if (enoughMaterialsToCraft || construction.getConstructionProgress() != 0f) {
			constructButton.click();
		}
		if (Domain.getWorld(construction.getWorldId()).props().hasProp(construction.id) && construction.canDeconstruct()) {
			deconstructButton.click();
		}

		requiredMaterialsPanel.leftClick(copy, windowsCopy);
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public Object getUniqueIdentifier() {
		return "ConstructionWindow" + (individual.hashCode() + construction.hashCode());
	}


	@Override
	public void leftClickReleased() {
		requiredMaterialsPanel.leftClickReleased();
	}


	@Override
	public boolean scrolled(int amount) {
		return requiredMaterialsPanel.scrolled(amount);
	}


	@Override
	public void refresh() {
		enoughMaterialsToCraft = CraftingStation.enoughMaterialsToCraft(individual, construction.getRequiredMaterials());
		requiredMaterialsPanel.refresh();
	}
}