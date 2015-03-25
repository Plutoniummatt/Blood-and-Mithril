package bloodandmithril.item.items.material;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.material.Material;
import bloodandmithril.item.material.wood.Wood;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A log made from a {@link Wood}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class LogItem extends bloodandmithril.item.items.material.MaterialItem {
	private static final long serialVersionUID = 8519886397429197864L;

	private Class<? extends Wood> wood;
	public static TextureRegion ICON;

	/**
	 * Constructor
	 */
	private LogItem(Class<? extends Wood> wood) {
		super(5f, 25, false);
		this.wood = wood;
		setValue(Material.getMaterial(wood).getLogValue());
	}


	/**
	 * Static instance getter
	 */
	public static LogItem log(Class<? extends Wood> wood) {
		return new LogItem(wood);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return Material.getMaterial(wood).getName() + " Log";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return Material.getMaterial(wood).getName() + " Logs";
	}


	@Override
	public String getDescription() {
		return "A log, made from " + Material.getMaterial(wood).getName() + ".";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		if (other instanceof LogItem) {
			return wood.equals(((LogItem) other).wood);
		}

		return false;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return Material.getMaterial(wood).getLogTextureRegion();
	}


	@Override
	protected Item internalCopy() {
		return log(wood);
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		return ICON;
	}
}