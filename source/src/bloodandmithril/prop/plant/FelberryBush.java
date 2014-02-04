package bloodandmithril.prop.plant;

import bloodandmithril.item.Item;
import bloodandmithril.item.material.plant.Felberry;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.components.ContextMenu;

/**
 * The {@link Prop} that grows {@link Felberry}
 *
 * @author Matt
 */
public class FelberryBush extends Plant {

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	protected FelberryBush(float x, float y, int width, int height) {
		super(x, y, width, height);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see bloodandmithril.prop.Harvestable#harvest()
	 */
	@Override
	public Item harvest() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see bloodandmithril.prop.Harvestable#destroyUponHarvest()
	 */
	@Override
	public boolean destroyUponHarvest() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see bloodandmithril.prop.Prop#render()
	 */
	@Override
	public void render() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see bloodandmithril.prop.Prop#synchronize(bloodandmithril.prop.Prop)
	 */
	@Override
	public void synchronize(Prop other) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see bloodandmithril.prop.Prop#getContextMenu()
	 */
	@Override
	public ContextMenu getContextMenu() {
		// TODO Auto-generated method stub
		return null;
	}

}
