package bloodandmithril.prop.building;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.badlogic.gdx.graphics.Color;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.Container;
import bloodandmithril.item.ContainerImpl;
import bloodandmithril.item.Item;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.ContextMenuItem;
import bloodandmithril.ui.components.window.RequiredMaterialsWindow;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Domain.Depth;

/**
 * A Construction
 *
 * @author Matt
 */
public abstract class Construction extends Prop implements Container {

	/** Dimensions of this {@link Construction} */
	protected final int width, height;

	/** Current construction progress, 1f means fully constructed */
	private float constructionProgress;
	
	/** The rate at which this {@link Construction} is constructed, in units of /s */
	private float constructionRate;
	
	/** The container used to store construction materials during the construction stage */
	private ContainerImpl materialContainer = new ContainerImpl(1000, true);
	
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
			
			menu.addMenuItem(
				new ContextMenuItem(
					"Required materials", 
					() -> {
						UserInterface.addLayeredComponent(new RequiredMaterialsWindow(
							BloodAndMithrilClient.WIDTH / 2 - 250, 
							BloodAndMithrilClient.HEIGHT / 2 + 250, 
							500, 
							500, 
							"Required materials", 
							true, 
							500, 
							300,
							materialContainer,
							getRequiredMaterials()
						));
					}, 
					Color.WHITE,
					Color.GREEN,
					Color.GRAY,
					null
				)
			);
			
			
			if (Domain.getSelectedIndividuals().size() == 1) {
				final Individual selected = Domain.getSelectedIndividuals().iterator().next();
				ContextMenuItem openChestMenuItem = new ContextMenuItem(
					"Transfer materials for construction",
					() -> {
						if (ClientServerInterface.isServer()) {
							selected.getAI().setCurrentTask(
								new TradeWith(selected, this)
							);
						} else {
							ClientServerInterface.SendRequest.sendTradeWithPropRequest(selected, id);
						}
					},
					Color.WHITE,
					Color.GREEN,
					Color.GRAY,
					null
				);

				menu.addMenuItem(openChestMenuItem);
			}
			
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
	
	
	/**
	 * Whether or not construction of this {@link Construction} can take place.
	 */
	public boolean canConstruct() {
		final Map<Item, Integer> requiredMaterials = getRequiredMaterials();
		
		newHashMap(requiredMaterials).entrySet().stream().forEach(new Consumer<Entry<Item, Integer>>() {
			@Override
			public void accept(Entry<Item, Integer> arg0) {
				Entry<Item, Integer> existing = Iterables.tryFind(materialContainer.getInventory().entrySet(), new Predicate<Entry<Item, Integer>>() {
					@Override
					public boolean apply(Entry<Item, Integer> input) {
						return input.getKey().sameAs(arg0.getKey());
					}
				}).orNull();
				
				if (existing != null) {
					if (arg0.getValue() - existing.getValue() <= 0) {
						requiredMaterials.remove(arg0.getKey());
					}
				}
			}
		});
		
		return requiredMaterials.isEmpty();
	}
	
	
	@Override
	public void synchronizeContainer(Container other) {
		materialContainer.synchronizeContainer(other);
	}


	@Override
	public void giveItem(Item item) {
		materialContainer.giveItem(item);
		if (ClientServerInterface.isClient()) {
			UserInterface.layeredComponents.stream().filter((component) -> {
				return component instanceof RequiredMaterialsWindow;
			}).forEach((component) -> {
				((RequiredMaterialsWindow) component).refresh();
			});
		} else {
			ClientServerInterface.SendNotification.notifyRefreshItemWindows();
		}
	}


	@Override
	public int takeItem(Item item) {
		return materialContainer.takeItem(item);
	}


	@Override
	public Map<Item, Integer> getInventory() {
		return materialContainer.getInventory();
	}


	@Override
	public float getMaxCapacity() {
		return materialContainer.getMaxCapacity();
	}


	@Override
	public float getCurrentLoad() {
		return materialContainer.getCurrentLoad();
	}
	
	
	@Override
	public boolean canExceedCapacity() {
		return materialContainer.canExceedCapacity();
	}
}