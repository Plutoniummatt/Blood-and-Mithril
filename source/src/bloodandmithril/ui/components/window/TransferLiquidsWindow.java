package bloodandmithril.ui.components.window;

import java.util.Deque;
import java.util.List;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.prop.furniture.LiquidContainerProp;
import bloodandmithril.prop.furniture.LiquidContainerProp.PropLiquidContainerItem;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;

/**
 * Window for transferring liquids with {@link PropLiquidContainerItem}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class TransferLiquidsWindow extends Window {

	private Individual individual;
	private LiquidContainerProp container;

	/**
	 * Constructor
	 */
	public TransferLiquidsWindow(Individual individual, LiquidContainerProp container) {
		super(
			500,
			350,
			individual.getId().getSimpleName() + " interacting with " + container.getClass().getSimpleName() + container.id,
			true,
			500,
			350,
			true,
			true,
			true
		);
		this.individual = individual;
		this.container = container;
	}


	@Override
	protected void internalWindowRender(Graphics graphics) {
		if (!individual.isAlive()) {
			setClosing(true);
		}
	}


	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
	}


	@Override
	protected void uponClose() {
	}


	@Override
	public Object getUniqueIdentifier() {
		return individual.getId().getSimpleName() + individual.getId().getId() + container.getClass().getSimpleName() + container.id;
	}


	@Override
	public void leftClickReleased() {
	}
}