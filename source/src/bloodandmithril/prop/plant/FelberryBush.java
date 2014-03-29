package bloodandmithril.prop.plant;

import bloodandmithril.item.Item;
import bloodandmithril.item.material.plant.Felberries;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.components.ContextMenu;

/**
 * The {@link Prop} that grows {@link Felberries}
 *
 * @author Matt
 */
public class FelberryBush extends Plant {
	private static final long serialVersionUID = -3584950382266050693L;
	
	private int berries = 10;

	/**
	 * Constructor
	 */
	public FelberryBush(float x, float y) {
		super(x, y, 48, 32);
	}

	
	@Override
	public Item harvest() {
		if (berries > 0) {
			berries--;
			return new Felberries();
		} else {
			return null;
		}
	}

	
	@Override
	public boolean destroyUponHarvest() {
		return false;
	}

	
	@Override
	public void render() {
	}

	
	@Override
	public void synchronizeProp(Prop other) {
	}

	
	@Override
	public ContextMenu getContextMenu() {
		return null;
	}


	@Override
	public void update(float delta) {
	}
}