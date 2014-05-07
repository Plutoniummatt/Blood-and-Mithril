package bloodandmithril.ui.components.window;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.List;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.Construct;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.prop.building.Construction;
import bloodandmithril.prop.crafting.CraftingStation;
import bloodandmithril.ui.Refreshable;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.RequiredMaterialsPanel;
import bloodandmithril.util.Util.Colors;

import com.badlogic.gdx.graphics.Color;

/**
 * An extension of {@link TradeWindow} for construction
 *
 * @author Matt
 */
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
			beginConstruction();
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
	public ConstructionWindow(int x, int y, String title, boolean active, Individual individual, Construction construction) {
		super(x, y, 600, 300, title, active, 600, 300, true, true);
		this.individual = individual;
		this.construction = construction;

		this.requiredMaterialsPanel = new RequiredMaterialsPanel(
			this,
			individual,
			construction.getRequiredMaterials()
		);

		refresh();
	}


	private void beginConstruction() {
		refresh();
		if (enoughMaterialsToCraft || construction.getConstructionProgress() != 0f) {
			if (ClientServerInterface.isServer()) {
				individual.getAI().setCurrentTask(new Construct(individual, construction));
			} else {
				ClientServerInterface.SendRequest.sendConstructRequest(individual.getId().getId(), construction.id);
			}
		}
	}


	@Override
	protected void internalWindowRender() {
		if (individual.getState().position.cpy().sub(construction.position.cpy()).len() > 64) {
			setClosing(true);
		}

		if (construction.getConstructionProgress() >= 1f) {
			setClosing(true);
		}

		requiredMaterialsPanel.x = x + 9;
		requiredMaterialsPanel.y = y - 110;
		requiredMaterialsPanel.width = width - 13;
		requiredMaterialsPanel.height = height - 110;
		requiredMaterialsPanel.render();

		constructButton.render(x + 60, y - 45, isActive() && (enoughMaterialsToCraft || construction.getConstructionProgress() != 0f), isActive() ? getAlpha() : getAlpha() * 0.6f);

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