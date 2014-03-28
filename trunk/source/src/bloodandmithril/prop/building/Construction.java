package bloodandmithril.prop.building;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.item.Container;
import bloodandmithril.item.Item;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.ContextMenuItem;
import bloodandmithril.ui.components.window.ScrollableListingWindow;
import bloodandmithril.world.Domain.Depth;

/**
 * A Construction
 *
 * @author Matt
 */
public abstract class Construction extends Prop {

	/** Dimensions of this {@link Construction} */
	protected final int width, height;

	/** Current construction progress, 1f means fully constructed */
	private float constructionProgress;
	
	/** The rate at which this {@link Construction} is constructed, in units of /s */
	private float constructionRate;
	
	/** The container used to store construction materials during the construction stage */
	private Container materialContainer = new Container(1000, true);
	
	/**
	 * Constructor
	 */
	protected Construction(float x, float y, int width, int height, boolean grounded) {
		super(x, y, grounded, Depth.MIDDLEGROUND);
		this.width = width;
		this.height = height;
	}


	@Override
	public boolean isMouseOver() {
		float mx = BloodAndMithrilClient.getMouseWorldX();
		float my = BloodAndMithrilClient.getMouseWorldY();

		return mx > position.x - width/2 && mx < position.x + width/2 && my > position.y && my < position.y + height;
	}


	@Override
	public void render() {
		internalRender(constructionProgress);
	}
	
	
	/**
	 * Progresses the construction of this {@link Construction}, in time units measured in seconds
	 */
	public synchronized void construct(float time) {
		if (constructionProgress >= 1f) {
			finishConstruction();
		} else {
			constructionProgress += time * constructionRate;
		}
	}
	
	
	/**
	 * Finalise the construction
	 */
	private void finishConstruction() {
		constructionProgress = 1f;
	}


	@Override
	public boolean leftClick() {
		if (!isMouseOver()) {
			return false;
		}
		return true;
	}


	@Override
	public boolean rightClick() {
		if (!isMouseOver()) {
			return false;
		}
		return true;
	}
	
	
	@Override
	public ContextMenu getContextMenu() {
		if (constructionProgress == 1f) {
			return getCompletedContextMenu();
		} else {
			ContextMenu menu = new ContextMenu(0, 0);
			
			final Map<Item, String> reqMaterials = getConstructionMaterialStatus();
			
			menu.addMenuItem(
				new ContextMenuItem(
					"Required materials", 
					() -> {
						UserInterface.addLayeredComponent(new ScrollableListingWindow<Item, String>(
							BloodAndMithrilClient.WIDTH / 2 - 250, 
							BloodAndMithrilClient.HEIGHT / 2 + 250, 
							500, 
							500, 
							"Required materials", 
							true, 
							500, 
							300, 
							true, 
							true, 
							reqMaterials
						));
					}, 
					Color.WHITE,
					Color.GREEN,
					Color.GRAY,
					null
				)
			);
			
			return menu;
		}
	}
	

	/**
	 * See {@link #constructionStage}
	 */
	public float getConstructionProgress() {
		return constructionProgress;
	}


	/**
	 * See {@link #constructionStage}
	 */
	public void setConstructionProgress(float constructionProgress) {
		this.constructionProgress = constructionProgress;
	}
	

	/**
	 * @return the amount of materials currently assigned to this construction, as well as the total required.
	 */
	private Map<Item, String> getConstructionMaterialStatus() {
		Map<Item, String> map = newHashMap();
		
		for (Entry<Item, Integer> entry : getRequiredMaterials().entrySet()) {
			Integer numberOfItemsInMaterialContainer = materialContainer.getInventory().get(entry.getKey());
			map.put(entry.getKey(), (numberOfItemsInMaterialContainer == null ? "0" : numberOfItemsInMaterialContainer.toString()) + "/" + entry.getValue().toString());
		}
		
		return map;
	}
	
	
	@Override
	public String getContextMenuItemLabel() {
		return getClass().getSimpleName() + (constructionProgress == 1f ? "" : " - Under construction (" + String.format("%.1f", constructionProgress * 100) + "%)");
	}
	
	
	/** Renders this {@link Construction} based on {@link #constructionProgress} */
	protected abstract void internalRender(float constructionProgress);
	
	/** Get the required items to construct this {@link Construction} */
	protected abstract Map<Item, Integer> getRequiredMaterials();
	
	/** Get the context menu that will be displayed once this {@link Construction} has finished being constructing */
	protected abstract ContextMenu getCompletedContextMenu();
}